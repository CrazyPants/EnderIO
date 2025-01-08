package com.enderio.conduits.client.model.conduit;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.common.init.ConduitComponents;
import com.enderio.core.client.RenderUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IModelBuilder;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class ConduitItemModel extends BakedModelWrapper<BakedModel> {

    private final ConduitItemOverrides itemOverrides = new ConduitItemOverrides();

    public ConduitItemModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public ItemOverrides getOverrides() {
        return itemOverrides;
    }

    public static class ConduitItemOverrides extends ItemOverrides {

        private final Map<Holder<Conduit<?, ?>>, BakedModel> CACHE = new HashMap<>();

        @Nullable
        @Override
        public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel,
                @Nullable LivingEntity pEntity, int pSeed) {
            Holder<Conduit<?, ?>> conduit = pStack.get(ConduitComponents.CONDUIT);
            return CACHE.computeIfAbsent(conduit, t -> createBakedModel(t, pModel));
        }

        private BakedModel createBakedModel(@Nullable Holder<Conduit<?, ?>> conduit, BakedModel model) {
            ResourceLocation conduitTexture = MissingTextureAtlasSprite.getLocation();
            if (conduit != null) {
                conduitTexture = conduit.value().texture();
            }

            // Get the replacement texture.
            TextureAtlasSprite newTexture = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(conduitTexture);

            // Construct a new baked model, replacing the texture of each quad with the new
            // texture.
            IModelBuilder<?> builder = IModelBuilder.of(model.useAmbientOcclusion(), model.usesBlockLight(),
                    model.isGui3d(), model.getTransforms(), model.getOverrides(), newTexture, RenderTypeGroup.EMPTY);

            var random = new SingleThreadedRandomSource(42L);

            var unculledQuads = model.getQuads(null, null, random, ModelData.EMPTY, null);
            for (BakedQuad quad : unculledQuads) {
                builder.addUnculledFace(paintQuad(quad, newTexture));
            }

            for (Direction side : Direction.values()) {
                random.setSeed(42L);
                var culledQuads = model.getQuads(null, side, random, ModelData.EMPTY, null);
                for (BakedQuad quad : culledQuads) {
                    builder.addCulledFace(side, paintQuad(quad, newTexture));
                }
            }

            return builder.build();
        }

        // TODO: This is stolen from painted blocks.
        // This should be moved into a common utility.
        protected BakedQuad paintQuad(BakedQuad toCopy, TextureAtlasSprite sprite) {
            BakedQuad copied = new BakedQuad(Arrays.copyOf(toCopy.getVertices(), 32), -1, toCopy.getDirection(), sprite,
                    toCopy.isShade());

            for (int i = 0; i < 4; i++) {
                float[] uv0 = RenderUtil.unpackVertices(copied.getVertices(), i, IQuadTransformer.UV0, 2);
                uv0[0] = (uv0[0] - toCopy.getSprite().getU0()) * sprite.contents().width()
                        / toCopy.getSprite().contents().width() + sprite.getU0();
                uv0[1] = (uv0[1] - toCopy.getSprite().getV0()) * sprite.contents().height()
                        / toCopy.getSprite().contents().height() + sprite.getV0();
                int[] packedTextureData = RenderUtil.packUV(uv0[0], uv0[1]);
                copied.getVertices()[IQuadTransformer.UV0 + i * IQuadTransformer.STRIDE] = packedTextureData[0];
                copied.getVertices()[IQuadTransformer.UV0 + 1 + i * IQuadTransformer.STRIDE] = packedTextureData[1];
            }
            return copied;
        }
    }
}
