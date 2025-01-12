package com.enderio.conduits.common.conduit.type.energy;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.ticker.IOAwareConduitTicker;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class EnergyConduitTicker extends IOAwareConduitTicker<EnergyConduit, EnergyConduitTicker.Connection> {

    public EnergyConduitTicker() {
    }

    // TODO: Need a way to prevent the extracts list from being built as it is
    // unused.

    @Override
    protected void tickGraph(ServerLevel level, EnergyConduit conduit, List<Connection> inserts,
            List<Connection> extracts, ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider) {

        // Adjust for tick rate. Always flow up so we are at minimum meeting the
        // required rate.
        int transferRate = (int) Math.ceil(conduit.transferRatePerTick() * (20.0 / conduit.graphTickRate()));

        EnergyConduitNetworkContext context = graph.getContext(EnergyConduitNetworkContext.TYPE);
        if (context == null) {
            return;
        }

        if (context.energyStored() <= 0) {
            return;
        }

        List<IEnergyStorage> storagesForInsert = new ArrayList<>();
        for (var insert : inserts) {
            IEnergyStorage capability = level.getCapability(Capabilities.EnergyStorage.BLOCK, insert.neighborPos(),
                    insert.neighborSide());
            if (capability != null) {
                storagesForInsert.add(capability);
            }
        }

        // Revert overflow.
        if (storagesForInsert.size() <= context.rotatingIndex()) {
            context.setRotatingIndex(0);
        }

        int startingRotatingIndex = context.rotatingIndex();
        for (int i = startingRotatingIndex; i < startingRotatingIndex + storagesForInsert.size(); i++) {
            int insertIndex = i % storagesForInsert.size();

            IEnergyStorage insertHandler = storagesForInsert.get(insertIndex);

            if (!insertHandler.canReceive()) {
                continue;
            }

            int energyToInsert = Math.min(transferRate, Math.max(context.energyStored(), 0));
            int energyInserted = insertHandler.receiveEnergy(energyToInsert, false);
            context.setEnergyStored(context.energyStored() - energyInserted);
            context.setRotatingIndex(insertIndex + 1);
        }
    }

    @Override
    protected boolean shouldSkip(List<Connection> extractList, List<Connection> insertList) {
        return insertList.isEmpty();
    }

    @Override
    protected @Nullable EnergyConduitTicker.Connection createConnection(Level level, ConduitNode node, Direction side) {
        IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                node.getPos().relative(side), side.getOpposite());
        if (energyStorage != null) {
            return new Connection(node, side, energyStorage);
        }

        return null;
    }

    protected static class Connection extends IOAwareConduitTicker.SimpleConnection {
        private final IEnergyStorage energyStorage;

        public Connection(ConduitNode node, Direction side, IEnergyStorage energyStorage) {
            super(node, side);
            this.energyStorage = energyStorage;
        }

        public IEnergyStorage energyStorage() {
            return energyStorage;
        }
    }
}
