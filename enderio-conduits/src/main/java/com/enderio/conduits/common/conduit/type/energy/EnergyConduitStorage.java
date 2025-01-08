package com.enderio.conduits.common.conduit.type.energy;

import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.common.init.Conduits;
import net.neoforged.neoforge.energy.IEnergyStorage;

public record EnergyConduitStorage(
    int transferRate,
    ConduitNode node
) implements IEnergyStorage {

    private static final int ENERGY_BUFFER_SCALER = 4;

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }

        var context = node.getNetwork().getOrCreateContext(EnergyConduitNetworkContext.TYPE);

        // Cap to transfer rate.
        toReceive = Math.min(transferRate(), toReceive);

        int energyReceived = Math.min(getMaxEnergyStored() - getEnergyStored(), toReceive);
        if (!simulate) {
            context.setEnergyStored(getEnergyStored() + energyReceived);
        }

        return energyReceived;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        var context = node.getNetwork().getContext(EnergyConduitNetworkContext.TYPE);
        if (context == null) {
            return 0;
        }

        return Math.max(Math.min(getMaxEnergyStored(), context.energyStored()), 0);
    }

    @Override
    public int getMaxEnergyStored() {
        // Capacity is transfer rate + nodeCount * transferRatePerTick / 2 (expanded).
        // This ensures at least the transfer rate of the cable is available, but capacity doesn't grow outrageously.
        int nodeCount = node.getNetwork().getNodes().size();

        // The maximum number of nodes before the network capacity is INT_MAX.
        int maxNodesBeforeLimit = Integer.MAX_VALUE / (transferRate() / ENERGY_BUFFER_SCALER) - ENERGY_BUFFER_SCALER;
        if (nodeCount >= maxNodesBeforeLimit) {
            return Integer.MAX_VALUE;
        }

        // Always full transfer rate plus the extra buffer.
        return transferRate() + nodeCount * (transferRate() / ENERGY_BUFFER_SCALER);
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    // The block will not expose this capability unless it can be extracted from
    // This means we don't have to worry about checking if we can extract at this point.
    @Override
    public boolean canReceive() {
        return node.getNetwork() != null;
    }
}
