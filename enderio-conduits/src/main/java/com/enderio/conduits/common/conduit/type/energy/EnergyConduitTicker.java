package com.enderio.conduits.common.conduit.type.energy;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.ticker.IOAwareConduitTicker;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlockEntity;
import com.enderio.conduits.common.init.Conduits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public class EnergyConduitTicker implements IOAwareConduitTicker<EnergyConduit> {

    public EnergyConduitTicker() {
    }

    @Override
    public void tickColoredGraph(ServerLevel level, EnergyConduit conduit, List<Connection> inserts, List<Connection> extracts, DyeColor color,
        ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider) {

        // Adjust for tick rate. Always flow up so we are at minimum meeting the required rate.
        int transferRate = (int)Math.ceil(conduit.transferRatePerTick() * (20.0 / conduit.graphTickRate()));

        EnergyConduitNetworkContext context = graph.getContext(EnergyConduitNetworkContext.TYPE);
        if (context == null) {
            return;
        }

        if (context.energyStored() <= 0) {
            return;
        }

        List<IEnergyStorage> storagesForInsert = new ArrayList<>();
        for (var insert : inserts) {
            IEnergyStorage capability = level.getCapability(Capabilities.EnergyStorage.BLOCK, insert.move(), insert.direction().getOpposite());
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
    public boolean shouldSkipColor(List<Connection> extractList, List<Connection> insertList) {
        return insertList.isEmpty();
    }
}
