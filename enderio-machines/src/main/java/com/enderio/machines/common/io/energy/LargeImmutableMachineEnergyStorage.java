package com.enderio.machines.common.io.energy;

import com.enderio.base.api.io.IOConfigurable;
import com.enderio.base.api.io.energy.EnergyIOMode;
import org.apache.commons.lang3.NotImplementedException;

/**
 * An immutable machine energy storage.
 * Used for client side syncing.
 */
@Deprecated(forRemoval = true, since = "7.1")
public class LargeImmutableMachineEnergyStorage implements IMachineEnergyStorage, ILargeMachineEnergyStorage {
    /**
     * A default value, storing no energy.
     */
    public static final LargeImmutableMachineEnergyStorage EMPTY = new LargeImmutableMachineEnergyStorage(0, 0);

    private final long energyStored;
    private final long maxEnergyStored;

    public LargeImmutableMachineEnergyStorage(long energyStored, long maxEnergyStored) {
        this.energyStored = energyStored;
        this.maxEnergyStored = maxEnergyStored;
    }

    @Override
    public int getEnergyStored() {
        return (int)Math.min(energyStored, Integer.MAX_VALUE);
    }

    @Override
    public long getLargeEnergyStored() {
        return energyStored;
    }
    @Override
    public int getMaxEnergyStored() {
        return (int)Math.min(maxEnergyStored, Integer.MAX_VALUE);
    }

    @Override
    public long getLargeMaxEnergyStored() {
        return maxEnergyStored;
    }

    @Override
    public int getMaxEnergyUse() {
        return 0;
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
        return 0;
    }

    @Override
    public int getEnergyUsed() {
        return 0;
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
