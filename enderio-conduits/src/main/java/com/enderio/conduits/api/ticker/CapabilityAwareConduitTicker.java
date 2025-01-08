package com.enderio.conduits.api.ticker;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.ArrayList;
import java.util.List;

public abstract class CapabilityAwareConduitTicker<TConduit extends Conduit<TConduit>, TCap> implements IOAwareConduitTicker<TConduit> {

    @Override
    public final void tickColoredGraph(ServerLevel level, TConduit conduit, List<Connection> inserts, List<Connection> extracts,
        DyeColor color, ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider) {

        List<CapabilityConnection> insertCaps = new ArrayList<>();
        for (Connection insert : inserts) {
            // TODO: we should have bundle block entities cache neighbour capabilities...
            TCap capability = level.getCapability(getCapability(), insert.move(), insert.direction().getOpposite());
            if (capability != null) {
                insertCaps.add(new CapabilityConnection(insert.direction(), insert.node(), capability));
            }
        }

        if (!insertCaps.isEmpty()) {
            List<CapabilityConnection> extractCaps = new ArrayList<>();

            for (Connection extract : extracts) {
                TCap capability = level.getCapability(getCapability(), extract.move(), extract.direction().getOpposite());
                if (capability != null) {
                    extractCaps.add(new CapabilityConnection(extract.direction(), extract.node(), capability));
                }
            }

            if (!extractCaps.isEmpty()) {
                tickCapabilityGraph(level, conduit, insertCaps, extractCaps, graph, coloredRedstoneProvider);
            }
        }
    }

    @Override
    public boolean canConnectTo(Level level, BlockPos conduitPos, Direction direction) {
        TCap capability = level.getCapability(getCapability(), conduitPos.relative(direction), direction.getOpposite());
        return capability != null;
    }

    protected abstract void tickCapabilityGraph(ServerLevel level, TConduit conduit, List<CapabilityConnection> inserts,
        List<CapabilityConnection> extracts, ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider);

    protected abstract BlockCapability<TCap, Direction> getCapability();

    public class CapabilityConnection extends Connection {
        private final TCap capability;

        public CapabilityConnection(Direction dir, ConduitNode node, TCap capability) {
            super(dir, node);
            this.capability = capability;
        }

        public TCap capability() {
            return this.capability;
        }
    }
}
