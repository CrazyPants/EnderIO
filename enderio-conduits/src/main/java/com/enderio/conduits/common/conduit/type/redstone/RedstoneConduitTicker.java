package com.enderio.conduits.common.conduit.type.redstone;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.ticker.ChannelIOAwareConduitTicker;
import com.enderio.conduits.common.init.ConduitBlocks;
import com.enderio.conduits.common.redstone.RedstoneExtractFilter;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RedstoneConduitTicker extends ChannelIOAwareConduitTicker<RedstoneConduit, ChannelIOAwareConduitTicker.SimpleConnection> {

    private final Map<DyeColor, Integer> activeColors = new EnumMap<>(DyeColor.class);

    @Override
    public void tickGraph(ServerLevel level, RedstoneConduit conduit, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {

        var context = graph.getOrCreateContext(RedstoneConduitNetworkContext.TYPE);
        boolean isActiveBeforeTick = context.isActive();
        context.clear();

        activeColors.clear();
        super.tickGraph(level, conduit, graph, coloredRedstoneProvider);

        // If active changed, nodes need to be synced.
        if (context.isActive() != isActiveBeforeTick) {
            for (var node : graph.getNodes()) {
                node.markDirty();
            }
        }
    }

    @Override
    public void tickColoredGraph(ServerLevel level, RedstoneConduit conduit, List<SimpleConnection> inserts,
            List<SimpleConnection> extracts, DyeColor color, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {

        RedstoneConduitNetworkContext networkContext = graph.getOrCreateContext(RedstoneConduitNetworkContext.TYPE);

        for (SimpleConnection extract : extracts) {
            int signal;
            if (extract.extractFilter() instanceof RedstoneExtractFilter filter) {
                signal = filter.getInputSignal(level, extract.neighborPos(), extract.neighborSide());
            } else {
                signal = level.getSignal(extract.neighborPos(), extract.neighborSide());
            }

            if (signal > 0) {
                networkContext.setSignal(color, signal);
            }
        }

        for (SimpleConnection insert : inserts) {
            level.neighborChanged(insert.neighborPos(), ConduitBlocks.CONDUIT.get(), insert.pos());
        }
    }

    @Override
    protected @Nullable ChannelIOAwareConduitTicker.SimpleConnection createConnection(Level level, ConduitNode node, Direction side) {
        return new SimpleConnection(node, side);
    }

    @Override
    protected boolean shouldSkipColor(List<SimpleConnection> extractList, List<SimpleConnection> insertList) {
        // Skip if the channel is completely un-utilised.
        return extractList.isEmpty() && insertList.isEmpty();
    }
}
