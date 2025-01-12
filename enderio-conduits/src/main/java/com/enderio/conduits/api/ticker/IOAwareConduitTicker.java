package com.enderio.conduits.api.ticker;

import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.config.io.IOConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class IOAwareConduitTicker<TConduit extends Conduit<TConduit, ? extends IOConnectionConfig>, TConnection extends IOAwareConduitTicker.SimpleConnection>
        implements ConduitTicker<TConduit> {
    @Override
    public void tickGraph(ServerLevel level, TConduit conduit, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {
        List<TConnection> extracts = new ArrayList<>();
        List<TConnection> inserts = new ArrayList<>();
        for (ConduitNode node : graph.getNodes()) {
            // Ensure the node is loaded
            if (!node.isLoaded()) {
                continue;
            }

            for (Direction side : Direction.values()) {
                if (node.isConnectedTo(side)) {
                    var config = node.getConnectionConfig(side, conduit.connectionConfigType());
                    if (config.canExtract() && isActive(level, side, node, coloredRedstoneProvider)) {
                        var connection = createConnection(level, node, side);
                        if (connection != null) {
                            extracts.add(connection);
                        }
                    }

                    if (config.canInsert()) {
                        var connection = createConnection(level, node, side);
                        if (connection != null) {
                            inserts.add(connection);
                        }
                    }
                }
            }
        }

        if (shouldSkip(extracts, inserts)) {
            return;
        }

        tickGraph(level, conduit, inserts, extracts, graph, coloredRedstoneProvider);
    }

    protected boolean shouldSkip(List<TConnection> extractList, List<TConnection> insertList) {
        return extractList.isEmpty() || insertList.isEmpty();
    }

    @Nullable
    protected abstract TConnection createConnection(Level level, ConduitNode node, Direction side);

    protected abstract void tickGraph(ServerLevel level, TConduit conduit, List<TConnection> inserts,
            List<TConnection> extracts, ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider);

    // TODO: This needs to be factored out...
    private boolean isActive(ServerLevel level, Direction side, ConduitNode node,
            ColoredRedstoneProvider coloredRedstoneProvider) {
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

        boolean hasRedstone = coloredRedstoneProvider.isRedstoneActive(level, node.getPos(),
                redstoneControlledConnection.redstoneChannel());
        if (!hasRedstone) {
            for (Direction direction : Direction.values()) {
                if (level.getSignal(node.getPos().relative(direction), direction.getOpposite()) > 0) {
                    hasRedstone = true;
                    break;
                }
            }
        }

        return redstoneControlledConnection.redstoneControl().isActive(hasRedstone);
    }

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
