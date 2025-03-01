package com.enderio.machines.common.io.energy;

import com.enderio.base.api.io.IOConfigurable;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.machines.common.MachineNBTKeys;
import com.enderio.machines.common.config.MachinesConfig;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Machine energy storage medium.
 * Uses capacitor keys to determine maximum capacity and transfer rate.
 * Also provides sided access through capabilities.
 */
public class MachineEnergyStorage implements IMachineEnergyStorage, INBTSerializable<CompoundTag> {
    private final IOConfigurable config;
    private final EnergyIOMode ioMode;

    private int energyStored;

    private int energyRecieved;
    private int energyUsed;
    private int previousEnergy = -1;

    private final Supplier<Integer> capacity;
    private final Supplier<Integer> usageRate;

    public MachineEnergyStorage(IOConfigurable config, EnergyIOMode ioMode, Supplier<Integer> capacity, Supplier<Integer> usageRate) {
        this.config = config;
        this.ioMode = ioMode;
        this.capacity = capacity;
        this.usageRate = usageRate;
    }

    @Override
    public final IOConfigurable getConfig() {
        return config;
    }

    @Override
    public final EnergyIOMode getIOMode() {
        return ioMode;
    }

    // Override in BE
    protected void onContentsChanged() {
      if(previousEnergy < 0) {
          previousEnergy = energyStored;
          return;
      }
      if(energyStored > previousEnergy) {
          energyRecieved += energyStored - previousEnergy;
      } else if(energyStored < previousEnergy) {
          energyUsed += previousEnergy - energyStored;
      }
      previousEnergy = energyStored;
    }

    @Override
    public int getEnergyStored() {
        return Math.min(energyStored, getMaxEnergyStored());
    }


    @Override
    public void resetEnergyChanges() {
        energyRecieved = 0;
        energyStored = 0;
        previousEnergy = energyStored;
    }

    @Override
    public int getEnergyReceived() {
        return energyRecieved;
    }

    @Override
    public int getEnergyUsed() {
        return energyStored;
    }

    /**
     * Set the energy stored in the storage.
     */
    public void setEnergyStored(int energy) {
        energyStored = Math.min(energy, getMaxEnergyStored());
        onContentsChanged();
    }

    @Override
    public int addEnergy(int energy) {
        return addEnergy(energy, false);
    }

    @Override
    public int addEnergy(int energy, boolean simulate) {
        int energyBefore = getEnergyStored();
        int newEnergyStored = Math.min(getEnergyStored() + energy, getMaxEnergyStored());
        if (!simulate) {
            setEnergyStored(newEnergyStored);
            onContentsChanged();
        }
        return newEnergyStored - energyBefore;
    }

    @Override
    public int takeEnergy(int energy) {
        int energyBefore = getEnergyStored();
        setEnergyStored(Math.max(getEnergyStored() - energy, 0));
        onContentsChanged();
        return energyBefore - getEnergyStored();
    }

    @Override
    public int consumeEnergy(int energy, boolean simulate) {
        // Cap rate
        int usableEnergy = Math.min(getEnergyStored(), Math.min(energy, getMaxEnergyUse()));
        if (!simulate) {
            return takeEnergy(usableEnergy);
        }
        return usableEnergy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity.get();
    }

    @Override
    public int getMaxEnergyUse() {
        return usageRate.get();
    }

    @Override
    public boolean canExtract() {
        return ioMode.canOutput();
    }

    @Override
    public boolean canReceive() {
        return ioMode.canInput();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive() || getMaxEnergyStored() == 0) {
            return 0;
        }

        int maximumEnergyToReceive;
        if (MachinesConfig.COMMON.ENERGY.THROTTLE_ENERGY_INPUT.get()) {
            maximumEnergyToReceive = Math.min(getMaxEnergyUse() * 2, maxReceive);
        } else {
            maximumEnergyToReceive = maxReceive;
        }

        int energyReceived = Math.min(getMaxEnergyStored() - getEnergyStored(), maximumEnergyToReceive);
        if (!simulate) {
            addEnergy(energyReceived);
        }

        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) {
            return 0;
        }

        int energyExtracted = Math.min(getEnergyStored(), maxExtract);
        if (!simulate) {
            takeEnergy(energyExtracted);
        }

        return energyExtracted;
    }

    @Nullable
    public IEnergyStorage getForSide(@Nullable Direction side) {
        if (side == null) {
            return this;
        }

        if (config.getIOMode(side).canConnect()) {
            return new Sided(this, side);
        }

        return null;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider lookupProvider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(MachineNBTKeys.ENERGY_STORED, getEnergyStored());
        tag.putInt(MachineNBTKeys.ENERGY_RECEiVED, energyRecieved);
        tag.putInt(MachineNBTKeys.ENERGY_USED, energyUsed);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt) {
        energyStored = nbt.getInt(MachineNBTKeys.ENERGY_STORED);
        energyRecieved = nbt.getInt(MachineNBTKeys.ENERGY_RECEiVED);
        energyUsed = nbt.getInt(MachineNBTKeys.ENERGY_USED);
    }

    private record Sided(MachineEnergyStorage wrapped, Direction side) implements IEnergyStorage {

        @Override
        public int getEnergyStored() {
            return wrapped.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return wrapped.getMaxEnergyStored();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive()) {
                return 0;
            }

            return wrapped.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract()) {
                return 0;
            }

            return wrapped.extractEnergy(maxExtract, simulate);
        }

        @Override
        public boolean canExtract() {
            if (wrapped.getIOMode().respectIOConfig() && !wrapped.getConfig().getIOMode(side).canOutput()) {
                return false;
            }

            return wrapped.canExtract();
        }

        @Override
        public boolean canReceive() {
            if (wrapped.getIOMode().respectIOConfig() && !wrapped.getConfig().getIOMode(side).canInput()) {
                return false;
            }

            return wrapped.canReceive();
        }
    }
}
