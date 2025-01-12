package com.enderio.conduits.api.ticker;

import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.config.io.ChanneledIOConnectionConfig;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A channel IO-aware ticker.
 *
 * This will check {@link ConduitNode#isActive(Direction)} for extraction connections to ensure it has a redstone signal.
 *
 * @param <T> The conduit type
 * @param <U> The type of connection for the ticker implementation
 */
public abstract class ChannelIOAwareConduitTicker<T extends Conduit<T, ? extends ChanneledIOConnectionConfig>, U extends ChannelIOAwareConduitTicker.SimpleConnection>
        implements ConduitTicker<T> {

    @Override
    public void tickGraph(ServerLevel level, T conduit, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {
        ListMultimap<DyeColor, U> extracts = ArrayListMultimap.create();
        ListMultimap<DyeColor, U> inserts = ArrayListMultimap.create();
        for (ConduitNode node : graph.getNodes()) {
            // Ensure the node is loaded
            if (!node.isLoaded()) {
                continue;
            }

            for (Direction side : Direction.values()) {
                if (node.isConnectedTo(side)) {
                    var config = node.getConnectionConfig(side, conduit.connectionConfigType());

                    if (config.canExtract()) {
                        var connection = createConnection(level, node, side);
                        if (connection != null) {
                            extracts.get(config.extractChannel()).add(connection);
                        }
                    }

                    if (config.canInsert()) {
                        var connection = createConnection(level, node, side);
                        if (connection != null) {
                            inserts.get(config.extractChannel()).add(connection);
                        }
                    }
                }
            }
        }

        for (DyeColor color : DyeColor.values()) {
            List<U> extractList = extracts.get(color);
            List<U> insertList = inserts.get(color);
            if (shouldSkipColor(extractList, insertList)) {
                continue;
            }

            tickColoredGraph(level, conduit, insertList, extractList, color, graph, coloredRedstoneProvider);
        }
    }

    protected boolean shouldSkipColor(List<U> extractList, List<U> insertList) {
        return extractList.isEmpty() || insertList.isEmpty();
    }

    @Nullable
    protected abstract U createConnection(Level level, ConduitNode node, Direction side);

    protected abstract void tickColoredGraph(ServerLevel level, T conduit, List<U> inserts, List<U> extracts,
            DyeColor color, ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider);

    public static class SimpleConnection {
        private final ConduitNode node;
        private final Direction side;

        public SimpleConnection(ConduitNode node, Direction side) {
            this.node = node;
            this.side = side;
        }

        public ConduitNode node() {
            return node;
        }

        public BlockPos pos() {
            return node.getPos();
        }

        public Direction side() {
            return side;
        }

        public BlockPos neighborPos() {
            return pos().relative(side);
        }

        public Direction neighborSide() {
            return side.getOpposite();
        }

        @Nullable
        public ResourceFilter insertFilter() {
            return node.getInsertFilter(side);
        }

        @Nullable
        public ResourceFilter extractFilter() {
            return node.getExtractFilter(side);
        }
    }
}
