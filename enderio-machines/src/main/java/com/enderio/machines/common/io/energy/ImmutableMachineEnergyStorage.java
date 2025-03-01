package com.enderio.machines.common.io.energy;

import com.enderio.base.api.io.IOConfigurable;
import com.enderio.base.api.io.energy.EnergyIOMode;
import org.apache.commons.lang3.NotImplementedException;

/**
 * An immutable machine energy storage.
 * Used for client side syncing.
 */
@Deprecated(forRemoval = true, since = "7.1")
public class ImmutableMachineEnergyStorage implements IMachineEnergyStorage {
    /**
     * A default value, storing no energy.
     */
    public static final ImmutableMachineEnergyStorage EMPTY = new ImmutableMachineEnergyStorage(0, 0, 0, 0, 0);

    private final int energyStored;
    private final int maxEnergyStored;
    private final int maxEnergyUse;
    private final int energyReceived;
    private final int energyUsed;

    public ImmutableMachineEnergyStorage(int energyStored, int maxEnergyStored, int maxEnergyUse, int energyReceived, int energyUsed) {
        this.energyStored = energyStored;
        this.maxEnergyStored = maxEnergyStored;
        this.maxEnergyUse = maxEnergyUse;
        this.energyReceived = energyReceived;
        this.energyUsed = energyUsed;
    }

    public ImmutableMachineEnergyStorage(IMachineEnergyStorage storage) {
        this(storage.getEnergyStored(), storage.getMaxEnergyStored(), storage.getMaxEnergyUse(), storage.getEnergyReceived(), storage.getEnergyUsed());
    }

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return maxEnergyStored;
    }

    @Override
    public int getMaxEnergyUse() {
        return maxEnergyUse;
    }

    // This class is intended for internal use only, don't expose.
    @Override
    public IOConfigurable getConfig() {
        throw new NotImplementedException();
    }

    // This class is intended for internal use only, don't expose.
    @Override
    public EnergyIOMode getIOMode() {
        throw new NotImplementedException();
    }

    @Override
    public int getEnergyReceived() {
        return energyReceived;
    }

    @Override
    public int getEnergyUsed() {
        return energyUsed;
    }

    @Override
    public void resetEnergyChanges() {
    }

    /**
     * @deprecated This storage is immutable.
     */
    @Deprecated
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated This storage is immutable.
     */
    @Deprecated
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    /**
     * @deprecated This storage is immutable.
     */
    @Deprecated
    @Override
    public void setEnergyStored(int energy) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated This storage is immutable.
     */
    @Deprecated
    @Override
    public int addEnergy(int energy) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated This storage is immutable.
     */
    @Deprecated
    @Override
    public int addEnergy(int energy, boolean simulate) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated This storage is immutable.
     */
    @Deprecated
    @Override
    public int takeEnergy(int energy) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated This storage is immutable.
     */
    @Deprecated
    @Override
    public int consumeEnergy(int energy, boolean simulate) {
        throw new UnsupportedOperationException();
    }
}
