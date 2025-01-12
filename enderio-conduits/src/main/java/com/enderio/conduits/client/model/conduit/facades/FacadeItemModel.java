package com.enderio.conduits.client.model.conduit.facades;

import static com.enderio.conduits.client.ConduitClientSetup.modelOf;

import com.enderio.base.common.init.EIODataComponents;
import com.enderio.conduits.client.ConduitClientSetup;
import com.enderio.core.data.model.ModelHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class FacadeItemModel implements IDynamicBakedModel {

    private final Map<Block, List<BakedModel>> itemRenderCache = new HashMap<>();
    private final Block facade;
    private final BakedModel facadeModel;

    public FacadeItemModel(BakedModel facadeModel) {
        this.facade = null;
        this.facadeModel = facadeModel;
    }

    private FacadeItemModel(BakedModel facadeModel, Block facade) {
        this.facade = facade;
        this.facadeModel = facadeModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData extraData, @Nullable RenderType renderType) {
        List<BakedQuad> bakedQuads = new ArrayList<>();
        if (facade != null) {
            bakedQuads.addAll(getItemModel().getQuads(facade.defaultBlockState(), side, rand, extraData, renderType));
            bakedQuads.addAll(modelOf(ConduitClientSetup.CONDUIT_FACADE_OVERLAY).getQuads(null, side, rand, extraData,
                    renderType));
        } else {
            bakedQuads.addAll(facadeModel.getQuads(null, side, rand, extraData, renderType));
        }
        return bakedQuads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return ModelHelper.getMissingTexture();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return modelOf(ConduitClientSetup.CONDUIT_FACADE_OVERLAY).getTransforms();
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        if (!itemStack.has(EIODataComponents.BLOCK_PAINT)) {
            return List.of(RenderType.solid());
        }

        var paintData = itemStack.get(EIODataComponents.BLOCK_PAINT);
        if (paintData == null) {
            return List.of(RenderType.cutout());
        }

        var paintStack = paintData.paint().asItem().getDefaultInstance();
        return Minecraft.getInstance()
                .getItemRenderer()
                .getModel(paintStack, null, null, 0)
                .getRenderTypes(paintStack, fabulous);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        if (!itemStack.has(EIODataComponents.BLOCK_PAINT)) {
            return List.of(this);
        }

        var paintData = itemStack.get(EIODataComponents.BLOCK_PAINT);
        return itemRenderCache.computeIfAbsent(paintData.paint(),
                paintKey -> List.of(new FacadeItemModel(facadeModel, paintData.paint())));
    }

    /**
     * Get the reference block's item model.
     */
    private BakedModel getItemModel() {
        return Minecraft.getInstance().getItemRenderer().getModel(facade.asItem().getDefaultInstance(), null, null, 0);
    }

}
