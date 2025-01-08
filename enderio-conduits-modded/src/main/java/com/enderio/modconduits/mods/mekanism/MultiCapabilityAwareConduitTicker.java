package com.enderio.modconduits.mods.mekanism;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.ticker.IOAwareConduitTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiCapabilityAwareConduitTicker<TConduit extends Conduit<TConduit>, TCap> implements IOAwareConduitTicker<TConduit> {
    private final BlockCapability<? extends TCap, Direction>[] capabilities;

    public MultiCapabilityAwareConduitTicker(BlockCapability<? extends TCap, Direction>[] capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public boolean canConnectTo(Level level, BlockPos conduitPos, Direction direction) {
        for (BlockCapability<? extends TCap, Direction> cap : capabilities) {
            TCap capability = level.getCapability(cap, conduitPos.relative(direction), direction.getOpposite());
            if (capability != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void tickColoredGraph(
        ServerLevel level,
        TConduit conduit,
        List<Connection> inserts,
        List<Connection> extracts,
        DyeColor color,
        ConduitNetwork graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {

        List<CapabilityConnection<TCap>> insertCaps = new ArrayList<>();
        for (Connection insert : inserts) {
            for (BlockCapability<? extends TCap, Direction> cap : capabilities) {
                TCap capability = level.getCapability(cap, insert.move(), insert.direction().getOpposite());
                if (capability != null) {
                    insertCaps.add(new CapabilityConnection<>(insert.direction(), insert.node(), capability));
                }
            }

        }
        if (!insertCaps.isEmpty()) {
            List<CapabilityConnection<TCap>> extractCaps = new ArrayList<>();

            for (Connection extract : extracts) {
                for (BlockCapability<? extends TCap, Direction> cap : capabilities) {
                    TCap capability = level.getCapability(cap, extract.move(), extract.direction().getOpposite());
                    if (capability != null) {
                        extractCaps.add(new CapabilityConnection<>(extract.direction(), extract.node(), capability));
                    }
                }
            }
            if (!extractCaps.isEmpty()) {
                tickCapabilityGraph(conduit, insertCaps, extractCaps, level, graph, coloredRedstoneProvider);
            }
        }
    }

    protected abstract void tickCapabilityGraph(
        TConduit conduit,
        List<CapabilityConnection<TCap>> insertCaps,
        List<CapabilityConnection<TCap>> extractCaps,
        ServerLevel level,
        ConduitNetwork graph,
        ColoredRedstoneProvider coloredRedstoneProvider);

    public static class CapabilityConnection<TCap> extends Connection {
        private final TCap capability;

        public CapabilityConnection(Direction direction, ConduitNode node, TCap capability) {
            super(direction, node);
            this.capability = capability;
        }

        public TCap capability() {
            return capability;
        }
    }
}
