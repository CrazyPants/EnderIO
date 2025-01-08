package com.enderio.conduits.common.conduit.type.redstone;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.ticker.IOAwareConduitTicker;
import com.enderio.conduits.common.init.ConduitBlocks;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.conduits.common.redstone.RedstoneExtractFilter;
import com.enderio.conduits.common.tag.ConduitTags;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneConduitTicker implements IOAwareConduitTicker<RedstoneConduit> {

    private final Map<DyeColor, Integer> activeColors = new EnumMap<>(DyeColor.class);

    @Override
    public boolean canConnectTo(Level level, BlockPos conduitPos, Direction direction) {
        BlockPos neighbor = conduitPos.relative(direction);
        BlockState blockState = level.getBlockState(neighbor);
        return blockState.is(ConduitTags.Blocks.REDSTONE_CONNECTABLE)
                || blockState.canRedstoneConnectTo(level, neighbor, direction);
    }

    @Override
    public boolean canForceConnectTo(Level level, BlockPos conduitPos, Direction direction) {
        BlockPos neighbor = conduitPos.relative(direction);
        BlockState blockState = level.getBlockState(neighbor);
        return !blockState.isAir();
    }

    @Override
    public void tickGraph(ServerLevel level, RedstoneConduit conduit, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {

        Collection<ConduitNode> nodes = graph.getNodes();

        RedstoneConduitNetworkContext context = graph.getOrCreateContext(RedstoneConduitNetworkContext.TYPE);
        boolean isActiveBeforeTick = context.isActive();
        context.clear();

        activeColors.clear();
        tickGraph(level, conduit, nodes.stream().filter(ConduitNode::isLoaded).toList(),
                graph, coloredRedstoneProvider);

        // If active changed, nodes need to be synced.
        if (context.isActive() != isActiveBeforeTick) {
            for (var node : nodes) {
                node.markDirty();
            }
        }
    }

    @Override
    public void tickColoredGraph(ServerLevel level, RedstoneConduit conduit, List<Connection> inserts,
            List<Connection> extracts, DyeColor color, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {

        RedstoneConduitNetworkContext networkContext = graph.getOrCreateContext(RedstoneConduitNetworkContext.TYPE);

        for (Connection extract : extracts) {
            int signal;
            if (extract.extractFilter() instanceof RedstoneExtractFilter filter) {
                signal = filter.getInputSignal(level, extract.move(), extract.direction());
            } else {
                signal = level.getSignal(extract.move(), extract.direction());
            }

            if (signal > 0) {
                networkContext.setSignal(color, signal);
            }
        }

        for (Connection insert : inserts) {
            level.neighborChanged(insert.move(), ConduitBlocks.CONDUIT.get(), insert.pos());
        }
    }

    @Override
    public boolean shouldSkipColor(List<Connection> extractList, List<Connection> insertList) {
        return extractList.isEmpty() && insertList.isEmpty(); // Only skip if no one uses the channel
    }
}
