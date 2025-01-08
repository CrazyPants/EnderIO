package com.enderio.conduits.api.ticker;

import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.config.io.IOConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IOAwareConduitTicker<TConduit extends Conduit<TConduit>> extends LoadedAwareConduitTicker<TConduit> {
    @Override
    default void tickGraph(ServerLevel level, TConduit conduit, List<ConduitNode> loadedNodes, ConduitNetwork graph,
                           ColoredRedstoneProvider coloredRedstoneProvider) {

        ListMultimap<DyeColor, Connection> extracts = ArrayListMultimap.create();
        ListMultimap<DyeColor, Connection> inserts = ArrayListMultimap.create();
        for (ConduitNode node : loadedNodes) {
            for (Direction side : Direction.values()) {
                if (node.isConnectedTo(side)) {
                    if (node.getConnectionConfig(side) instanceof IOConnectionConfig config) {
                        if (config.canExtract() && isActive(level, side, node, coloredRedstoneProvider)) {
                            extracts.get(config.extractChannel()).add(new Connection(side, node));
                        }

                        if (config.canInsert()) {
                            inserts.get(config.insertChannel()).add(new Connection(side, node));
                        }
                    }
                }
            }
        }

        for (DyeColor color : DyeColor.values()) {
            List<Connection> extractList = extracts.get(color);
            List<Connection> insertList = inserts.get(color);
            if (shouldSkipColor(extractList, insertList)) {
                continue;
            }

            tickColoredGraph(level, conduit, insertList, extractList, color, graph, coloredRedstoneProvider);
        }
    }

    default boolean shouldSkipColor(List<Connection> extractList, List<Connection> insertList) {
        return extractList.isEmpty() || insertList.isEmpty();
    }

    void tickColoredGraph(
        ServerLevel level,
        TConduit conduit,
        List<Connection> inserts,
        List<Connection> extracts,
        DyeColor color,
        ConduitNetwork graph,
        ColoredRedstoneProvider coloredRedstoneProvider);

    default boolean isActive(ServerLevel level, Direction side, ConduitNode node, ColoredRedstoneProvider coloredRedstoneProvider) {
        var connectionConfig = node.getConnectionConfig(side);
        if (!(connectionConfig instanceof RedstoneControlledConnection redstoneControlledConnection)) {
            return true;
        }

        if (redstoneControlledConnection.redstoneControl() == RedstoneControl.ALWAYS_ACTIVE) {
            return true;
        }

        if (redstoneControlledConnection.redstoneControl() == RedstoneControl.NEVER_ACTIVE) {
            return false;
        }

        boolean hasRedstone = coloredRedstoneProvider.isRedstoneActive(level, node.getPos(), redstoneControlledConnection.redstoneChannel());
        if (!hasRedstone) {
            for (Direction direction : Direction.values()) {
                if (level.getSignal(node.getPos().relative(direction), direction) > 0) {
                    hasRedstone = true;
                    break;
                }
            }
        }

        return redstoneControlledConnection.redstoneControl().isActive(hasRedstone);
    }

    class Connection {
        private final Direction direction;
        private final ConduitNode node;

        public Connection(Direction direction, ConduitNode node) {
            this.direction = direction;
            this.node = node;
        }

        public Direction direction() {
            return direction;
        }

        public ConduitNode node() {
            return node;
        }

        public BlockPos pos() {
            return node.getPos();
        }

        public BlockPos move() {
            return pos().relative(direction);
        }

        @Nullable
        public ResourceFilter extractFilter() {
            return node.getExtractFilter(direction);
        }

        @Nullable
        public ResourceFilter insertFilter() {
            return node.getInsertFilter(direction);
        }
    }
}
