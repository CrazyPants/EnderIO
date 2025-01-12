package com.enderio.conduits.api.ticker;

import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.config.IOConnectionConfig;
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
 * @param <V> The type of connection for the ticker implementation
 */
public abstract class IOAwareConduitTicker<T extends Conduit<T, U>, U extends IOConnectionConfig, V extends IOAwareConduitTicker.SimpleConnection<U>>
        implements ConduitTicker<T> {

    @Override
    public void tickGraph(ServerLevel level, T conduit, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {
        ListMultimap<DyeColor, V> senders = ArrayListMultimap.create();
        ListMultimap<DyeColor, V> receivers = ArrayListMultimap.create();

        for (ConduitNode node : graph.getNodes()) {
            // Ensure the node is loaded
            if (!node.isLoaded()) {
                continue;
            }

            for (Direction side : Direction.values()) {
                if (node.isConnectedTo(side)) {
                    var config = node.getConnectionConfig(side, conduit.connectionConfigType());

                    if (canSend(node, config)) {
                        var connection = createConnection(level, node, side);
                        if (connection != null) {
                            senders.get(config.sendColor()).add(connection);
                        }
                    }

                    if (canReceive(node, config)) {
                        var connection = createConnection(level, node, side);
                        if (connection != null) {
                            receivers.get(config.receiveColor()).add(connection);
                        }
                    }
                }
            }
        }

        for (DyeColor color : DyeColor.values()) {
            List<V> colorSenders = senders.get(color);
            List<V> colorReceivers = receivers.get(color);
            if (shouldSkipColor(colorSenders, colorReceivers)) {
                continue;
            }

            tickColoredGraph(level, conduit, colorSenders, colorReceivers, color, graph, coloredRedstoneProvider);
        }
    }

    protected boolean canSend(ConduitNode node, U config) {
        return config.canSend(node::hasRedstoneSignal);
    }

    protected boolean canReceive(ConduitNode node, U config) {
        return config.canReceive(node::hasRedstoneSignal);
    }

    protected boolean shouldSkipColor(List<V> senders, List<V> receivers) {
        return senders.isEmpty() || receivers.isEmpty();
    }

    protected void preProcessReceivers(List<V> receivers) {
        // Could implement a pre-sort here.
    }

    @Nullable
    protected abstract V createConnection(Level level, ConduitNode node, Direction side);

    protected abstract void tickColoredGraph(ServerLevel level, T conduit, List<V> senders, List<V> receivers,
            DyeColor color, ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider);

    public static class SimpleConnection<T extends IOConnectionConfig> {
        private final ConduitNode node;
        private final Direction side;
        private final T config;

        public SimpleConnection(ConduitNode node, Direction side, T config) {
            this.node = node;
            this.side = side;
            this.config = config;
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

        public T config() {
            return config;
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
