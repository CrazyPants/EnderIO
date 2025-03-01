package com.enderio.machines.common.io.energy;

import com.enderio.base.api.io.IOConfigurable;
import com.enderio.base.api.io.energy.EnergyIOMode;
import net.minecraft.util.Tuple;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Machine energy storage extensions.
 */
// NOTE: Using 'I' prefix here for consistency with Neo EnergyStorage
public interface IMachineEnergyStorage extends IEnergyStorage {
    /**
     * Set the amount of energy inside the storage
     */
    void setEnergyStored(int energy);

    /**
     * Add energy into storage.
     *
     * @return Amount of energy added.
     */
    int addEnergy(int energy);

    /**
     * Add energy into storage.
     *
     * @return Amount of energy added.
     */
     int addEnergy(int energy, boolean simulate);

    /**
     * Remove energy from storage.
     *
     * @return Amount of energy taken.
     * @apiNote Uncapped, if you are consuming energy, use {@link #consumeEnergy(int, boolean)} instead.
     */
    int takeEnergy(int energy);

    /**
     * Consume energy from storage.
     *
     * @return Amount of energy consumed.
     * @apiNote This is capped to {@link #getMaxEnergyUse()}, for uncapped use {@link #takeEnergy(int)} instead.
     */
    int consumeEnergy(int energy, boolean simulate);

    /**
     * Get the maximum energy use rate for the storage.
     * Generally this is the consumption speed or generation speed.
     */
    int getMaxEnergyUse();

    /**
     * Get the IO config for the storage.
     * This is generally provided by the machine.
     */
    IOConfigurable getConfig();

    /**
     * Get the energy IO mode for the storage
     * Determines how the IO config modifies behaviour.
     */
    EnergyIOMode getIOMode();

    int getEnergyReceived();

    int getEnergyUsed();

    void resetEnergyChanges();
}
