package com.enderio.conduits.common.conduit.bundle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.api.connection.ConduitConnectionType;
import com.enderio.conduits.common.Area;
import com.enderio.conduits.common.conduit.OffsetHelper;
import java.util.*;
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
    private final Map<Holder<Conduit<?>>, VoxelShape> conduitShapes = new HashMap<>();
    private final Map<Direction, VoxelShape> directionShapes = new HashMap<>();
    private static final VoxelShape CONNECTOR = Block.box(2.5f, 2.5, 15f, 13.5f, 13.5f, 16f);
    public static final VoxelShape CONNECTION = Block.box(6.5f, 6.5f, 9.5, 9.5f, 9.5f, 16);
    private static final VoxelShape CORE = Block.box(6.5f, 6.5f, 6.5f, 9.5f, 9.5f, 9.5f);
    private VoxelShape totalShape = CORE;

    public NewConduitShape() {

    }

    public void updateConduit(ConduitBundleReader bundle) {
        this.conduitShapes.clear();
        this.directionShapes.clear();
        for (Holder<Conduit<?>> conduit : bundle.getConduits()) {
            updateShapeForConduit(bundle, conduit);
        }
        updateTotalShape();
    }

    public VoxelShape getShapeFromHit(BlockPos pos, HitResult result) {
        return Optional.ofNullable(this.conduitShapes.get(getConduit(pos, result))).orElse(Shapes.empty());
    }

    @Nullable
    public Holder<Conduit<?>> getConduit(BlockPos pos, HitResult result) {
        return getLookUpValue(conduitShapes, pos, result);
    }

    @Nullable
    public Direction getDirection(BlockPos pos, HitResult result) {
        return getLookUpValue(directionShapes, pos, result);
    }

    @Nullable
    private <T> T getLookUpValue(Map<T, VoxelShape> shapes, BlockPos pos, HitResult result) {
        for (Map.Entry<T, VoxelShape> entry : shapes.entrySet()) {
            Vec3 vec3 = result.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
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
        VoxelShape conduitShape = Shapes.empty();
        Direction.Axis axis = OffsetHelper.findMainAxis(conduitBundle);
        Map<Holder<Conduit<?>>, List<Vec3i>> offsets = new HashMap<>();
        for (Direction direction : Direction.values()) {
            VoxelShape directionShape = directionShapes.getOrDefault(direction, Shapes.empty());
            if (conduitBundle.getConnectionType(direction, conduit) == ConduitConnectionType.CONNECTED_BLOCK) {
                VoxelShape connectorShape = rotateVoxelShape(CONNECTOR, direction);
                directionShape = Shapes.joinUnoptimized(directionShape, connectorShape, BooleanOp.OR);
                conduitShape = Shapes.joinUnoptimized(conduitShape, connectorShape, BooleanOp.OR);
            }

            var connectedTypes = conduitBundle.getConnectedConduits(direction);
            if (connectedTypes.contains(conduit)) {
                Vec3i offset = OffsetHelper.translationFor(direction.getAxis(),
                        OffsetHelper.offsetConduit(connectedTypes.indexOf(conduit), connectedTypes.size()));
                offsets.computeIfAbsent(conduit, ignored -> new ArrayList<>()).add(offset);
                VoxelShape connectionShape = rotateVoxelShape(CONNECTION, direction).move(offset.getX() * 3f / 16f,
                        offset.getY() * 3f / 16f, offset.getZ() * 3f / 16f);
                directionShape = Shapes.joinUnoptimized(directionShape, connectionShape, BooleanOp.OR);
                conduitShape = Shapes.joinUnoptimized(conduitShape, connectionShape, BooleanOp.OR);
            }
            directionShapes.put(direction, directionShape.optimize());
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

        var type = allConduits.get(i);
        @Nullable
        List<Vec3i> offsetsForConduit = offsets.get(type);
        if (offsetsForConduit != null) {
            // all are pointing to the same xyz reference meaning that we can draw the core
            if (offsetsForConduit.stream().distinct().count() != 1) {
                box = new Area(offsetsForConduit.toArray(new Vec3i[0]));
            }
        } else {
            notRendered = type;
        }

        if (offsetsForConduit != null && (box == null || !box.contains(offsetsForConduit.get(0)))) {
            conduitShape = Shapes.joinUnoptimized(
                    conduitShape, CORE.move(offsetsForConduit.get(0).getX() * 3f / 16f,
                            offsetsForConduit.get(0).getY() * 3f / 16f, offsetsForConduit.get(0).getZ() * 3f / 16f),
                    BooleanOp.OR);
        }

        if (box != null) {
            if (notRendered != null) {
                Vec3i offset = OffsetHelper.translationFor(axis, OffsetHelper.offsetConduit(i, allConduits.size()));
                if (!box.contains(offset)) {
                    conduitShape = Shapes.joinUnoptimized(conduitShape,
                            CORE.move(offset.getX() * 3f / 16f, offset.getY() * 3f / 16f, offset.getZ() * 3f / 16f),
                            BooleanOp.OR);
                }
            }

            conduitShape = Shapes.joinUnoptimized(conduitShape, CORE.move(box.getMin().getX() * 3f / 16f,
                    box.getMin().getY() * 3f / 16f, box.getMin().getZ() * 3f / 16f), BooleanOp.OR);
        } else {
            if (notRendered != null) {
                Vec3i offset = OffsetHelper.translationFor(axis, OffsetHelper.offsetConduit(i, allConduits.size()));
                conduitShape = Shapes.joinUnoptimized(conduitShape,
                        CORE.move(offset.getX() * 3f / 16f, offset.getY() * 3f / 16f, offset.getZ() * 3f / 16f),
                        BooleanOp.OR);
            }
        }

        conduitShapes.put(conduit, conduitShape.optimize());
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
