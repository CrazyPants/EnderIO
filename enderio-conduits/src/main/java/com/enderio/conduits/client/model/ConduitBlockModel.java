package com.enderio.conduits.client.model;

import static com.enderio.conduits.client.ConduitClientSetup.BOX;
import static com.enderio.conduits.client.ConduitClientSetup.CONDUIT_CONNECTION;
import static com.enderio.conduits.client.ConduitClientSetup.CONDUIT_CONNECTION_BOX;
import static com.enderio.conduits.client.ConduitClientSetup.CONDUIT_CONNECTOR;
import static com.enderio.conduits.client.ConduitClientSetup.CONDUIT_CORE;
import static com.enderio.conduits.client.ConduitClientSetup.CONDUIT_IO_IN;
import static com.enderio.conduits.client.ConduitClientSetup.CONDUIT_IO_IN_OUT;
import static com.enderio.conduits.client.ConduitClientSetup.CONDUIT_IO_OUT;
import static com.enderio.conduits.client.ConduitClientSetup.CONDUIT_IO_REDSTONE;
import static com.enderio.conduits.client.ConduitClientSetup.modelOf;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitNode;
import com.enderio.conduits.api.facade.FacadeType;
import com.enderio.conduits.api.model.ConduitCoreModelModifier;
import com.enderio.conduits.client.ConduitFacadeColor;
import com.enderio.conduits.client.model.conduit.facades.FacadeHelper;
import com.enderio.conduits.client.model.conduit.modifier.ConduitCoreModelModifiers;
import com.enderio.conduits.common.Area;
import com.enderio.conduits.common.conduit.ConduitBundle;
import com.enderio.conduits.common.conduit.OffsetHelper;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlockEntity;
import com.enderio.conduits.common.conduit.connection.ConnectionState;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import com.enderio.core.data.model.ModelHelper;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ConduitBlockModel implements IDynamicBakedModel {

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData extraData, @Nullable RenderType renderType) {

        List<BakedQuad> quads = new ArrayList<>();
        ConduitBundle conduitBundle = extraData.get(ConduitBundleBlockEntity.BUNDLE_MODEL_PROPERTY);
        ModelData data = extraData.get(ConduitBundleBlockEntity.FACADE_MODEL_DATA);

        if (conduitBundle != null) {
            if (FacadeHelper.areFacadesVisible()) {
                IQuadTransformer transformer = quad -> quad.tintIndex = ConduitFacadeColor
                        .moveTintIndex(quad.getTintIndex());
                Optional<Block> facadeOpt = conduitBundle.facade();
                if (facadeOpt.isPresent()) {
                    BlockState facade = facadeOpt.get().defaultBlockState();
                    var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(facade);
                    var facadeQuads = model.getQuads(facade, side, rand, data, renderType);

                    if (renderType != null && model.getRenderTypes(facade, rand, data).contains(renderType)) {
                        quads.addAll(transformer.process(facadeQuads));
                    }
                }

                // If the facade should hide the conduits, escape early.
                if (conduitBundle.hasFacade()) {
                    boolean areConduitsHidden = conduitBundle.facadeType()
                            .map(FacadeType::doesHideConduits)
                            .orElse(false);

                    if (areConduitsHidden) {
                        return quads;
                    }
                }
            }

            Direction.Axis axis = OffsetHelper.findMainAxis(conduitBundle);
            Map<Holder<Conduit<?>>, List<Vec3i>> offsets = new HashMap<>();

            for (Direction direction : Direction.values()) {
                boolean isEnd = conduitBundle.isConnectionEnd(direction);
                Direction preRotation = rotateDirection(direction, side);
                IQuadTransformer rotation = QuadTransformers.applying(rotateTransformation(direction));

                if (isEnd) {
                    quads.addAll(rotation.process(
                            modelOf(CONDUIT_CONNECTOR).getQuads(state, preRotation, rand, extraData, renderType)));
                }

                var connectedTypes = conduitBundle.getConnectedConduits(direction);
                for (int i = 0; i < connectedTypes.size(); i++) {
                    Holder<Conduit<?>> conduit = connectedTypes.get(i);
                    ConduitGraphObject node = conduitBundle.getNodeFor(conduit);

                    Vec3i offset = OffsetHelper.translationFor(direction.getAxis(),
                            OffsetHelper.offsetConduit(i, connectedTypes.size()));
                    offsets.computeIfAbsent(conduit, ignored -> new ArrayList<>()).add(offset);
                    IQuadTransformer rotationTranslation = rotation
                            .andThen(QuadTransformers.applying(translateTransformation(offset)));
                    quads.addAll(new ConduitTextureEmissiveQuadTransformer(sprite(conduitBundle, conduit), 0)
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

                        ConnectionState connectionState = conduitBundle.getConnectionState(direction, conduit);
                        if (connectionState instanceof DynamicConnectionState dyn) {
                            IQuadTransformer color = rotationTranslation
                                    .andThen(new ColorQuadTransformer(dyn.insertChannel(), dyn.extractChannel()));
                            BakedModel model = null;
                            if (dyn.isExtract() && dyn.isInsert()) {
                                model = modelOf(CONDUIT_IO_IN_OUT);
                            } else if (dyn.isInsert()) {
                                model = modelOf(CONDUIT_IO_IN);
                            } else if (dyn.isExtract()) {
                                model = modelOf(CONDUIT_IO_OUT);
                            }

                            if (model != null) {
                                quads.addAll(
                                        color.process(model.getQuads(state, preRotation, rand, extraData, renderType)));
                            }

                            if (dyn.control() == RedstoneControl.ACTIVE_WITH_SIGNAL
                                    || dyn.control() == RedstoneControl.ACTIVE_WITHOUT_SIGNAL) {
                                quads.addAll(rotationTranslation
                                        .andThen(new ColorQuadTransformer(null, dyn.redstoneChannel()))
                                        .process(modelOf(CONDUIT_IO_REDSTONE).getQuads(state, preRotation, rand,
                                                extraData, renderType)));
                            }
                        }
                    }
                }
            }

            var allTypes = conduitBundle.getConduits();
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
                    quads.addAll(new ConduitTextureEmissiveQuadTransformer(sprite(conduitBundle, toRender), 0)
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
                                sprite(conduitBundle, notRenderedEntry.getKey()), 0)
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
                            sprite(conduitBundle, notRenderedEntry.getKey()), 0).andThen(
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
        ConduitBundle conduitBundle = data.get(ConduitBundleBlockEntity.BUNDLE_MODEL_PROPERTY); // TODO temp particle
                                                                                                // fix
        if (conduitBundle == null || conduitBundle.getConduits().isEmpty()) {
            return ModelHelper.getMissingTexture();
        }
        if (conduitBundle.hasFacade()) {
            return Minecraft.getInstance()
                    .getBlockRenderer()
                    .getBlockModel(conduitBundle.facade().get().defaultBlockState())
                    .getParticleIcon(data.get(ConduitBundleBlockEntity.FACADE_MODEL_DATA));
        }
        return sprite(conduitBundle, conduitBundle.getConduits().getFirst());
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

    private static TextureAtlasSprite sprite(ConduitBundle conduitBundle, Holder<Conduit<?>> type) {
        ConduitNode node = conduitBundle.getNodeFor(type);
        ResourceLocation textureLocation = type.value().getTexture(node);
        return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(textureLocation);
    }

    private static boolean isMissingModel(BakedModel model) {
        return model == Minecraft.getInstance().getModelManager().getMissingModel();
    }
}
