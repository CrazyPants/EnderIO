package com.enderio.conduits.common.conduit;

import com.enderio.base.common.util.ThrowableUtil;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.joml.Vector2i;
import org.slf4j.Logger;

public class OffsetHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     *
     * Creates an offset based on the Schema
     *
     * @param typeIndex of the type you want to query
     * @param maxTypes for that Direction
     * @return the offset
     * <p>
     *  ###
     *  #1#
     *  ###
     * <p>
     *  #1#
     *  ###
     *  #2#
     * <p>
     *  1##
     *  #2#
     *  ##3
     * <p>
     *  all after 3:
     *  615
     *  294
     *  738
     */

    public static Map<Integer, Vector2i> positions = Util.make(() -> {
        Map<Integer, Vector2i> positions = new HashMap<>();
        positions.put(1, new Vector2i(0, -1));
        positions.put(2, new Vector2i(-1, 0));
        positions.put(3, new Vector2i(0, 1));
        positions.put(4, new Vector2i(1, 0));
        positions.put(5, new Vector2i(1, -1));
        positions.put(6, new Vector2i(-1, -1));
        positions.put(7, new Vector2i(-1, 1));
        positions.put(8, new Vector2i(1, 1));
        positions.put(9, new Vector2i(0, 0));
        return positions;
    });

    public static Vector2i offsetConduit(int conduitIndex, int maxConduits) {
        if (conduitIndex >= maxConduits) {
            LOGGER.warn("Higher index than existing conduits in OffsetHelper#offsetConduit",
                    ThrowableUtil.addStackTrace(new IndexOutOfBoundsException("higher index than existing conduits")));
            return new Vector2i();
        }

        if (conduitIndex < 0) {
            LOGGER.warn("Negative index in OffsetHelper#offsetConduit",
                    ThrowableUtil.addStackTrace(new IndexOutOfBoundsException("negative index")));
            return new Vector2i();
        }

        if (maxConduits == 1) {
            return new Vector2i();
        }

        if (maxConduits == 2) {
            return conduitIndex == 0 ? new Vector2i(0, -1) : new Vector2i(0, 1);
        }

        if (maxConduits == 3) {
            switch (conduitIndex) {
            case 0 -> {
                return new Vector2i(-1, -1);
            }
            case 1 -> {
                return new Vector2i();
            }
            case 2 -> {
                return new Vector2i(1, 1);
            }
            default -> throw new IllegalStateException();
            }
        }

        if (maxConduits < 9) {
            Vector2i vector2i = positions.get(conduitIndex + 1);
            if (vector2i != null) {
                return vector2i;
            }
        }

        LOGGER.warn("Fallback was applied in OffsetHelper#offsetConduit",
                ThrowableUtil.addStackTrace(new IndexOutOfBoundsException("fallback was applied")));
        return new Vector2i();
    }

    public static Vec3i translationFor(Direction.Axis axis, Vector2i offset) {
        return switch (axis) {
        case X -> new Vec3i(0, offset.y(), offset.x());
        case Y -> new Vec3i(offset.x(), 0, offset.y());
        case Z -> new Vec3i(offset.x(), offset.y(), 0);
        };
    }

    public static Direction.Axis findMainAxis(ConduitBundleReader bundle) {
        List<Direction> connectedDirs = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (!bundle.getConnectedConduits(dir).isEmpty()) {
                connectedDirs.add(dir);
            }
        }

        if (connectedDirs.isEmpty()) {
            return Direction.Axis.Z;
        }

        // get Last as MainAxis, because those are the horizontal ones
        return connectedDirs.get(connectedDirs.size() - 1).getAxis();
    }
}
