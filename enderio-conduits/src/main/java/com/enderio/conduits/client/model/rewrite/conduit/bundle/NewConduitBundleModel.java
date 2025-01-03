package com.enderio.conduits.client.model.rewrite.conduit.bundle;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.model.ConduitCoreModelModifier;
import com.enderio.conduits.client.ConduitFacadeColor;
import com.enderio.conduits.client.model.BoxTextureQuadTransformer;
import com.enderio.conduits.client.model.ColorQuadTransformer;
import com.enderio.conduits.client.model.ConduitTextureEmissiveQuadTransformer;
import com.enderio.conduits.client.model.conduit.facades.FacadeHelper;
import com.enderio.conduits.client.model.conduit.modifier.ConduitCoreModelModifiers;
import com.enderio.conduits.common.Area;
import com.enderio.conduits.common.conduit.ConduitBundle;
import com.enderio.conduits.common.conduit.OffsetHelper;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlockEntity;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import com.enderio.core.data.model.ModelHelper;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

import static com.enderio.conduits.client.ConduitClientSetup.*;

public class NewConduitBundleModel implements IDynamicBakedModel {

    public static final ModelProperty<ModelData> FACADE_MODEL_DATA = new ModelProperty<>();

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData extraData, @Nullable RenderType renderType) {

        List<BakedQuad> quads = new ArrayList<>();
        ModelData data = extraData.get(FACADE_MODEL_DATA);

        ConduitBundleRenderState bundleState = extraData.get(ConduitBundleRenderState.PROPERTY);

        if (bundleState != null) {
            if (FacadeHelper.areFacadesVisible()) {
                IQuadTransformer transformer = quad -> quad.tintIndex = ConduitFacadeColor
                        .moveTintIndex(quad.getTintIndex());

                if (bundleState.hasFacade()) {
                    BlockState facade = bundleState.facade();
                    var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(facade);
                    var facadeQuads = model.getQuads(facade, side, rand, data, renderType);

                    if (renderType != null && model.getRenderTypes(facade, rand, data).contains(renderType)) {
                        quads.addAll(transformer.process(facadeQuads));
                    }
                }

                // If the facade should hide the conduits, escape early.
                if (bundleState.hasFacade()) {
                    if (bundleState.doesFacadeHideConduits()) {
                        return quads;
                    }
                }
            }

            Direction.Axis axis = bundleState.mainAxis();
            Map<Holder<Conduit<?>>, List<Vec3i>> offsets = new HashMap<>();

            for (Direction direction : Direction.values()) {
                boolean isEnd = bundleState.isConnectionEndpoint(direction);
                Direction preRotation = rotateDirection(direction, side);
                IQuadTransformer rotation = QuadTransformers.applying(rotateTransformation(direction));

                if (isEnd) {
                    quads.addAll(rotation.process(
                            modelOf(CONDUIT_CONNECTOR).getQuads(state, preRotation, rand, extraData, renderType)));
                }

                var connectedTypes = bundleState.getConnectedConduits(direction);
                for (int i = 0; i < connectedTypes.size(); i++) {
                    Holder<Conduit<?>> conduit = connectedTypes.get(i);
                    ConduitGraphObject node = bundleState.getNode(conduit);

                    Vec3i offset = OffsetHelper.translationFor(direction.getAxis(),
                            OffsetHelper.offsetConduit(i, connectedTypes.size()));
                    offsets.computeIfAbsent(conduit, ignored -> new ArrayList<>()).add(offset);
                    IQuadTransformer rotationTranslation = rotation
                            .andThen(QuadTransformers.applying(translateTransformation(offset)));
                    quads.addAll(new ConduitTextureEmissiveQuadTransformer(sprite(bundleState.getTexture(conduit)), 0)
                            .andThen(rotationTranslation)
                            .process(modelOf(CONDUIT_CONNECTION).getQuads(state, preRotation, rand, extraData,
                                    renderType)));

                    ConduitCoreModelModifier conduitCoreModifier = ConduitCoreModelModifiers
                            .getModifier(conduit.value().type());
                    if (conduitCoreModifier != null) {
                        quads.addAll(rotationTranslation.process(conduitCoreModifier.createConnectionQuads(conduit,
                                node, side, direction, rand, renderType)));
                    }

                    if (isEnd) {
                        quads.addAll(rotationTranslation.process(modelOf(CONDUIT_CONNECTION_BOX).getQuads(state,
                                preRotation, rand, extraData, renderType)));

                        var connectionState = bundleState.getConnectionState(direction, conduit);
                        if (connectionState != null) {
                            IQuadTransformer color = rotationTranslation
                                    .andThen(new ColorQuadTransformer(connectionState.inputChannel(), connectionState.outputChannel()));

                            BakedModel model = switch (connectionState.mode()) {
                                case IN -> modelOf(CONDUIT_IO_IN);
                                case OUT -> modelOf(CONDUIT_IO_OUT);
                                case BOTH -> modelOf(CONDUIT_IO_IN_OUT);
                            };

                            if (model != null) {
                                quads.addAll(
                                        color.process(model.getQuads(state, preRotation, rand, extraData, renderType)));
                            }

                            if (connectionState.redstoneControl() == RedstoneControl.ACTIVE_WITH_SIGNAL
                                    || connectionState.redstoneControl() == RedstoneControl.ACTIVE_WITHOUT_SIGNAL) {
                                quads.addAll(rotationTranslation
                                        .andThen(new ColorQuadTransformer(null, connectionState.redstoneChannel()))
                                        .process(modelOf(CONDUIT_IO_REDSTONE).getQuads(state, preRotation, rand,
                                                extraData, renderType)));
                            }
                        }
                    }
                }
            }

            var allTypes = bundleState.conduits();
            @Nullable
            Area box = null;
            Map<Holder<Conduit<?>>, Integer> notRendered = new HashMap<>();
            List<Holder<Conduit<?>>> rendered = new ArrayList<>();
            for (int i = 0; i < allTypes.size(); i++) {
                var type = allTypes.get(i);
                @Nullable
                List<Vec3i> offsetsForType = offsets.get(type);
                if (offsetsForType != null) {
                    // all are pointing to the same xyz reference meaning that we can draw the core
                    if (offsetsForType.stream().distinct().count() == 1) {
                        rendered.add(type);
                    } else {
                        if (box == null) {
                            box = new Area(offsetsForType.toArray(new Vec3i[0]));
                        } else {
                            offsetsForType.forEach(box::makeContain);
                        }
                    }
                } else {
                    notRendered.put(type, i);
                }
            }

            Set<Vec3i> duplicateFinder = new HashSet<>();
            // rendered have only one distinct pos, so I can safely assume get(0) is valid
            List<Vec3i> duplicatePositions = rendered.stream()
                    .map(offsets::get)
                    .map(List::getFirst)
                    .filter(n -> !duplicateFinder.add(n))
                    .toList();
            for (Vec3i duplicatePosition : duplicatePositions) {
                if (box == null) {
                    box = new Area(duplicatePosition);
                } else {
                    box.makeContain(duplicatePosition);
                }
            }
            for (Holder<Conduit<?>> toRender : rendered) {
                List<Vec3i> offsetsForType = offsets.get(toRender);
                if (box == null || !box.contains(offsetsForType.getFirst())) {
                    quads.addAll(new ConduitTextureEmissiveQuadTransformer(sprite(bundleState.getTexture(toRender)), 0)
                            .andThen(QuadTransformers.applying(translateTransformation(offsetsForType.getFirst())))
                            .process(modelOf(CONDUIT_CORE).getQuads(state, side, rand, extraData, renderType)));
                }
            }

            if (box != null) {
                for (Map.Entry<Holder<Conduit<?>>, Integer> notRenderedEntry : notRendered.entrySet()) {
                    Vec3i offset = OffsetHelper.translationFor(axis,
                            OffsetHelper.offsetConduit(notRenderedEntry.getValue(), allTypes.size()));
                    if (!box.contains(offset)) {
                        quads.addAll(new ConduitTextureEmissiveQuadTransformer(
                                sprite(bundleState.getTexture(notRenderedEntry.getKey())), 0)
                                        .andThen(QuadTransformers.applying(translateTransformation(offset)))
                                        .process(modelOf(CONDUIT_CORE).getQuads(state, side, rand, extraData,
                                                renderType)));
                    }
                }

                quads.addAll(new BoxTextureQuadTransformer(box.size())
                        .andThen(QuadTransformers.applying(translateTransformation(box.getMin())))
                        .process(modelOf(BOX).getQuads(state, side, rand, extraData, renderType)));
            } else {
                for (Map.Entry<Holder<Conduit<?>>, Integer> notRenderedEntry : notRendered.entrySet()) {
                    quads.addAll(new ConduitTextureEmissiveQuadTransformer(
                            sprite(bundleState.getTexture(notRenderedEntry.getKey())), 0).andThen(
                                    QuadTransformers.applying(translateTransformation(OffsetHelper.translationFor(axis,
                                            OffsetHelper.offsetConduit(notRenderedEntry.getValue(), allTypes.size())))))
                                    .process(modelOf(CONDUIT_CORE).getQuads(state, side, rand, extraData, renderType)));
                }
            }
        }

        return quads;
    }

    /**
     * @param toDirection the Direction you want to transform to from the Bottom as base
     * @param toTransform the Direction to follow the same Transformation as bottom -> toDirection
     * @return the Direction toTransform was transformed to
     */
    @Nullable
    public static Direction rotateDirection(Direction toDirection, @Nullable Direction toTransform) {
        if (toTransform == null) {
            return null;
        }

        return switch (toDirection) {
        case DOWN -> toTransform;
        case UP -> toTransform.getClockWise(Direction.Axis.Z).getClockWise(Direction.Axis.Z);
        case NORTH -> toTransform.getCounterClockWise(Direction.Axis.X);
        case SOUTH -> toTransform.getClockWise(Direction.Axis.X);
        case WEST -> toTransform.getCounterClockWise(Direction.Axis.Z);
        case EAST -> toTransform.getClockWise(Direction.Axis.Z);
        };
    }

    public static Transformation rotateTransformation(Direction toDirection) {
        Quaternionf quaternion = new Quaternionf();
        switch (toDirection) {
        case UP -> quaternion.mul(Axis.ZP.rotationDegrees(180));
        case NORTH -> quaternion.mul(Axis.XP.rotationDegrees(90));
        case SOUTH -> quaternion.mul(Axis.XN.rotationDegrees(90));
        case WEST -> quaternion.mul(Axis.ZN.rotationDegrees(90));
        case EAST -> quaternion.mul(Axis.ZP.rotationDegrees(90));
        default -> {
        }
        }
        Transformation transformation = new Transformation(null, quaternion, null, null);
        return transformation.applyOrigin(new Vector3f(.5f, .5f, .5f));
    }

    private static Transformation translateTransformation(Vec3i offset) {
        return new Transformation(scale(offset, 3 / 16f), null, null, null);
    }

    private static Vector3f scale(Vec3i vector, float scaler) {
        return new Vector3f(vector.getX() * scaler, vector.getY() * scaler, vector.getZ() * scaler);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
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
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        // TODO temp particle fix
        ConduitBundleRenderState bundleState = data.get(ConduitBundleRenderState.PROPERTY);

        if (bundleState == null || bundleState.conduits().isEmpty()) {
            return ModelHelper.getMissingTexture();
        }

        if (bundleState.hasFacade()) {
            return Minecraft.getInstance()
                    .getBlockRenderer()
                    .getBlockModel(bundleState.facade())
                    .getParticleIcon(data.get(ConduitBundleBlockEntity.FACADE_MODEL_DATA));
        }
        return sprite(bundleState.getTexture(bundleState.conduits().getFirst()));
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
            @NotNull ModelData data) {
        ChunkRenderTypeSet facadeRenderTypes = data.get(ConduitBundleBlockEntity.FACADE_RENDERTYPE);
        ChunkRenderTypeSet renderTypes = ChunkRenderTypeSet.of(RenderType.cutout());
        if (facadeRenderTypes != null) {
            renderTypes = ChunkRenderTypeSet.union(renderTypes, facadeRenderTypes);
        }
        return renderTypes;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        ModelData data = IDynamicBakedModel.super.getModelData(level, pos, state, modelData);
        ModelData.Builder builder = data.derive();
        ConduitBundle conduitBundle = data.get(ConduitBundleBlockEntity.BUNDLE_MODEL_PROPERTY);
        if (conduitBundle != null && conduitBundle.hasFacade()) {
            BlockState blockState = conduitBundle.facade().get().defaultBlockState();
            BakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
            ModelData facadeData = blockModel.getModelData(level, pos, blockState, ModelData.EMPTY);
            builder.with(ConduitBundleBlockEntity.FACADE_MODEL_DATA, facadeData);
            builder.with(ConduitBundleBlockEntity.FACADE_RENDERTYPE, blockModel.getRenderTypes(blockState,
                    new SingleThreadedRandomSource(state.getSeed(pos)), facadeData));
        }
        return builder.build();
    }

    private static TextureAtlasSprite sprite(ResourceLocation location) {
        return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(location);
    }

    private static boolean isMissingModel(BakedModel model) {
        return model == Minecraft.getInstance().getModelManager().getMissingModel();
    }
}
