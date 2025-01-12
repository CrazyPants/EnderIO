package com.enderio.conduits.common.conduit.type.redstone;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.ticker.IOAwareConduitTicker;
import com.enderio.conduits.common.init.ConduitBlocks;
import com.enderio.conduits.common.redstone.RedstoneExtractFilter;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RedstoneConduitTicker extends
        IOAwareConduitTicker<RedstoneConduit, RedstoneConduitConnectionConfig, RedstoneConduitTicker.Connection> {

    @Override
    public void tickGraph(ServerLevel level, RedstoneConduit conduit, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {

        var context = graph.getOrCreateContext(RedstoneConduitNetworkContext.TYPE);
        boolean isActiveBeforeTick = context.isActive();
        context.nextTick();

        super.tickGraph(level, conduit, graph, coloredRedstoneProvider);

        // If active changed -or- this graph is fresh, nodes need to be synced.
        if (context.isNew() || context.isActive() != isActiveBeforeTick) {
            for (var node : graph.getNodes()) {
                if (node.isLoaded()) {
                    node.markDirty();
                }
            }
        }
    }

    @Override
    protected void tickColoredGraph(ServerLevel level, RedstoneConduit conduit, List<Connection> senders,
            List<Connection> receivers, DyeColor color, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {

        RedstoneConduitNetworkContext networkContext = graph.getOrCreateContext(RedstoneConduitNetworkContext.TYPE);

        for (Connection receiver : receivers) {
            int signal;
            if (receiver.extractFilter() instanceof RedstoneExtractFilter filter) {
                signal = filter.getInputSignal(level, receiver.neighborPos(), receiver.neighborSide());
            } else {
                signal = level.getSignal(receiver.neighborPos(), receiver.neighborSide());
            }

            if (signal > 0) {
                networkContext.setSignal(color, signal);
            }
        }

        // Only update neighbours if this is a new context or the signal strength
        // changed this time.
        if (networkContext.isNew() || networkContext.getSignal(color) != networkContext.getSignalLastTick(color)) {
            for (Connection sender : senders) {
                level.updateNeighborsAt(sender.pos(), ConduitBlocks.CONDUIT.get());

                // Update blocks connected to strong signals too.
                if (sender.config().isStrongOutputSignal()) {
                    level.updateNeighborsAt(sender.neighborPos(), ConduitBlocks.CONDUIT.get());
                }
            }
        }
    }

    @Override
    protected boolean shouldSkipColor(List<Connection> senders, List<Connection> receivers) {
        return senders.isEmpty() && receivers.isEmpty();
    }

    @Override
    protected @Nullable Connection createConnection(Level level, ConduitNode node, Direction side) {
        return new Connection(node, side, node.getConnectionConfig(side, RedstoneConduitConnectionConfig.TYPE));
    }

    protected static class Connection extends SimpleConnection<RedstoneConduitConnectionConfig> {
        public Connection(ConduitNode node, Direction side, RedstoneConduitConnectionConfig config) {
            super(node, side, config);
        }
    }
}
