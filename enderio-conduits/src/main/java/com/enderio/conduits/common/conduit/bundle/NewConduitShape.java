package com.enderio.conduits.common.conduit.bundle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.api.connection.ConnectionStatus;
import com.enderio.conduits.common.Area;
import com.enderio.conduits.common.conduit.OffsetHelper;
import java.util.*;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class NewConduitShape {

    private final Map<Pair<Direction, Holder<Conduit<?>>>, VoxelShape> conduitConnections = new HashMap<>();
    private final Map<Holder<Conduit<?>>, VoxelShape> conduitShapes = new HashMap<>();

    private final Map<Holder<Conduit<?>>, List<VoxelShape>> individualShapes = new HashMap<>();

    private static final VoxelShape CONNECTOR = Block.box(2.5f, 2.5, 15f, 13.5f, 13.5f, 16f);
    public static final VoxelShape CONNECTION = Block.box(6.5f, 6.5f, 9.5, 9.5f, 9.5f, 16);
    private static final VoxelShape CORE = Block.box(6.5f, 6.5f, 6.5f, 9.5f, 9.5f, 9.5f);
    private VoxelShape totalShape = CORE;

    public NewConduitShape() {

    }

    public void updateConduit(ConduitBundleReader bundle) {
        this.conduitShapes.clear();
        this.conduitConnections.clear();
        this.individualShapes.clear();
        for (Holder<Conduit<?>> conduit : bundle.getConduits()) {
            updateShapeForConduit(bundle, conduit);
        }
        updateTotalShape();
    }

    // TODO: Looks weird when the connecting boxes arrive, but this at least now matches 1.12 behaviour.
    public VoxelShape getShapeFromHit(BlockPos pos, HitResult result) {
        var aimedConduit = getConduit(pos, result);

        if (aimedConduit == null || !individualShapes.containsKey(aimedConduit)) {
            return Shapes.empty();
        }

        Vec3 vec3 = result.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());

        for (var shape : individualShapes.get(aimedConduit)) {
            Optional<Vec3> point = shape.closestPointTo(vec3);
            if (point.isEmpty()) {
                continue;
            }

            if (point.get().closerThan(vec3, Mth.EPSILON)) { // can't be 0 due to double
                return shape;
            }
        }

        return Shapes.empty();
    }

    @Nullable
    public Holder<Conduit<?>> getConduit(BlockPos pos, HitResult result) {
        return getLookUpValue(conduitShapes, pos, result);
    }

    @Nullable
    public Pair<Direction, Holder<Conduit<?>>> getConnectionFromHit(BlockPos pos, HitResult hit) {
        return getLookUpValue(conduitConnections, pos, hit);
    }

    @Nullable
    private <T> T getLookUpValue(Map<T, VoxelShape> shapes, BlockPos pos, HitResult result) {
        Vec3 vec3 = result.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        for (Map.Entry<T, VoxelShape> entry : shapes.entrySet()) {
            Optional<Vec3> point = entry.getValue().closestPointTo(vec3);
            if (point.isEmpty()) {
                continue;
            }

            if (point.get().closerThan(vec3, Mth.EPSILON)) { // can't be 0 due to double
                return entry.getKey();
            }
        }

        return null;
    }

    private void updateTotalShape() {
        this.totalShape = Shapes.empty();
        this.conduitShapes.values()
                .forEach(s -> this.totalShape = Shapes.joinUnoptimized(this.totalShape, s, BooleanOp.OR));
        totalShape.optimize();
    }

    public VoxelShape getTotalShape() {
        return this.totalShape;
    }

    private void updateShapeForConduit(ConduitBundleReader conduitBundle, Holder<Conduit<?>> conduit) {
        List<VoxelShape> individualShapeList = individualShapes.computeIfAbsent(conduit, ignored -> new ArrayList<>());

        VoxelShape conduitShape = Shapes.empty();
        Direction.Axis axis = OffsetHelper.findMainAxis(conduitBundle);
        Map<Holder<Conduit<?>>, List<Vec3i>> offsets = new HashMap<>();
        for (Direction direction : Direction.values()) {
            // Only create and save connection shape if it's a block connection, as that's what the lookup is for.
            VoxelShape conduitConnectionShape = null;

            // TODO: Lift the connector plate out of updateShapeForConduit?
            if (conduitBundle.getConnectionStatus(direction, conduit) == ConnectionStatus.CONNECTED_BLOCK) {
                VoxelShape connectorShape = rotateVoxelShape(CONNECTOR, direction);
                conduitShape = Shapes.joinUnoptimized(conduitShape, connectorShape, BooleanOp.OR);
                conduitConnectionShape = connectorShape;

                individualShapeList.add(connectorShape);
            }

            var connectedTypes = conduitBundle.getConnectedConduits(direction);
            if (connectedTypes.contains(conduit)) {
                Vec3i offset = OffsetHelper.translationFor(direction.getAxis(),
                        OffsetHelper.offsetConduit(connectedTypes.indexOf(conduit), connectedTypes.size()));
                offsets.computeIfAbsent(conduit, ignored -> new ArrayList<>()).add(offset);
                VoxelShape connectionShape = rotateVoxelShape(CONNECTION, direction).move(offset.getX() * 3f / 16f,
                        offset.getY() * 3f / 16f, offset.getZ() * 3f / 16f);
                conduitShape = Shapes.joinUnoptimized(conduitShape, connectionShape, BooleanOp.OR);

                if (conduitConnectionShape != null) {
                    conduitConnectionShape = Shapes.join(conduitConnectionShape, connectionShape, BooleanOp.OR);
                }

                individualShapeList.add(connectionShape);
            }

            if (conduitConnectionShape != null) {
                conduitConnections.put(new Pair<>(direction, conduit), conduitConnectionShape);
            }
        }

        var allConduits = conduitBundle.getConduits();
        @Nullable
        Area box = null;
        @Nullable
        Holder<Conduit<?>> notRendered = null;
        int i = allConduits.indexOf(conduit);
        if (i == -1) {
            conduitShapes.put(conduit, Shapes.block());
            return;
        }

        @Nullable
        List<Vec3i> offsetsForConduit = offsets.get(conduit);
        if (offsetsForConduit != null) {
            // all are pointing to the same xyz reference meaning that we can draw the core
            if (offsetsForConduit.stream().distinct().count() != 1) {
                box = new Area(offsetsForConduit.toArray(new Vec3i[0]));
            }
        } else {
            notRendered = conduit;
        }

        VoxelShape coreShape = Shapes.empty();

        if (offsetsForConduit != null && (box == null || !box.contains(offsetsForConduit.get(0)))) {
            coreShape = Shapes.joinUnoptimized(
                coreShape, CORE.move(offsetsForConduit.get(0).getX() * 3f / 16f,
                            offsetsForConduit.get(0).getY() * 3f / 16f, offsetsForConduit.get(0).getZ() * 3f / 16f),
                    BooleanOp.OR);
        }

        if (box != null) {
            if (notRendered != null) {
                Vec3i offset = OffsetHelper.translationFor(axis, OffsetHelper.offsetConduit(i, allConduits.size()));
                if (!box.contains(offset)) {
                    coreShape = Shapes.joinUnoptimized(coreShape,
                            CORE.move(offset.getX() * 3f / 16f, offset.getY() * 3f / 16f, offset.getZ() * 3f / 16f),
                            BooleanOp.OR);
                }
            }

            coreShape = Shapes.joinUnoptimized(coreShape, CORE.move(box.getMin().getX() * 3f / 16f,
                    box.getMin().getY() * 3f / 16f, box.getMin().getZ() * 3f / 16f), BooleanOp.OR);
        } else {
            if (notRendered != null) {
                Vec3i offset = OffsetHelper.translationFor(axis, OffsetHelper.offsetConduit(i, allConduits.size()));
                coreShape = Shapes.joinUnoptimized(coreShape,
                        CORE.move(offset.getX() * 3f / 16f, offset.getY() * 3f / 16f, offset.getZ() * 3f / 16f),
                        BooleanOp.OR);
            }
        }

        conduitShape = Shapes.joinUnoptimized(conduitShape, coreShape, BooleanOp.OR);

        conduitShapes.put(conduit, conduitShape.optimize());
        individualShapeList.add(coreShape.optimize());
    }

    /**
     * Rotates a VoxelShape around the center to the specified Direction, Origin is SOUTH
     *
     * @param toRotate
     * @param direction
     * @return the rotated VoxelShape
     */
    public static VoxelShape rotateVoxelShape(VoxelShape toRotate, Direction direction) {
        VoxelShape[] buffer = new VoxelShape[] { toRotate, Shapes.empty() };
        if (direction.get2DDataValue() == -1) {
            if (direction == Direction.DOWN) {
                buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1],
                        Shapes.box(minX, 1 - maxZ, minY, maxX, 1 - minZ, maxY)));
            } else {
                buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY,
                        maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.box(minX, minZ, minY, maxX, maxZ, maxY)));
            }

            return buffer[1];
        }

        for (int i = 0; i < (direction.get2DDataValue()) % 4; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY,
                    maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }

}
