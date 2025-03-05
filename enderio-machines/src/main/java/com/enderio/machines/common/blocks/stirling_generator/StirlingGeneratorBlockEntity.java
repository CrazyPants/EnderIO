package com.enderio.machines.common.blocks.stirling_generator;

import com.enderio.base.api.capacitor.CapacitorModifier;
import com.enderio.base.api.capacitor.FixedScalable;
import com.enderio.base.api.capacitor.LinearScalable;
import com.enderio.base.api.capacitor.QuadraticScalable;
import com.enderio.base.api.capacitor.SteppedScalable;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.machines.common.MachineNBTKeys;
import com.enderio.machines.common.blocks.base.blockentity.PoweredMachineBlockEntity;
import com.enderio.machines.common.blocks.base.blockentity.flags.CapacitorSupport;
import com.enderio.machines.common.blocks.base.inventory.MachineInventoryLayout;
import com.enderio.machines.common.blocks.base.inventory.SingleSlotAccess;
import com.enderio.machines.common.blocks.base.state.MachineState;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.io.energy.MachineEnergyStorage;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StirlingGeneratorBlockEntity extends PoweredMachineBlockEntity {

    public static final QuadraticScalable CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY,
            MachinesConfig.COMMON.ENERGY.STIRLING_GENERATOR_CAPACITY);

    public static final SteppedScalable FUEL_EFFICIENCY = new SteppedScalable(CapacitorModifier.FUEL_EFFICIENCY,
            MachinesConfig.COMMON.ENERGY.STIRLING_GENERATOR_FUEL_EFFICIENCY_BASE,
            MachinesConfig.COMMON.ENERGY.STIRLING_GENERATOR_FUEL_EFFICIENCY_STEP);

    public static final LinearScalable GENERATION_SPEED = new LinearScalable(
            CapacitorModifier.BURNING_ENERGY_GENERATION, MachinesConfig.COMMON.ENERGY.STIRLING_GENERATOR_PRODUCTION);

    public static final SingleSlotAccess FUEL = new SingleSlotAccess();

    private int burnTime;
    private int burnDuration;

    public StirlingGeneratorBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(MachineBlockEntities.STIRLING_GENERATOR.get(), worldPosition, blockState, true, CapacitorSupport.REQUIRED,
                EnergyIOMode.Output, CAPACITY, FixedScalable.ZERO);
    }

    public int getGenerationRate() {
        return GENERATION_SPEED.scaleI(this::getCapacitorData).get();
    }

    public int getFuelEfficiency() {
        return FUEL_EFFICIENCY.scaleI(this::getCapacitorData).get();
    }

    @Override
    public MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder()
                .inputSlot((slot, stack) -> stack.getBurnTime(RecipeType.SMELTING) > 0
                        && stack.getCraftingRemainingItem().isEmpty())
                .slotAccess(FUEL)
                .capacitor()
                .build();
    }

    @Override
    public void serverTick() {
        // We ignore redstone control here.
        if (isGenerating()) {
            burnTime--;

            if (!requiresCapacitor() || isCapacitorInstalled()) {
                getEnergyStorage().addEnergy(getGenerationRate());
            }
        }

        // Taking more fuel is locked behind redstone control.
        if (canAct()) {
            if (!isGenerating() && getEnergyStorage().getEnergyStored() < getEnergyStorage().getMaxEnergyStored()) {
                // Get the fuel
                ItemStack fuel = FUEL.getItemStack(this);
                if (!fuel.isEmpty()) {
                    // Get the burn time.
                    int burningTime = fuel.getBurnTime(RecipeType.SMELTING);

                    if (burningTime > 0) {
                        float burnSpeed = MachinesConfig.COMMON.ENERGY.STIRLING_GENERATOR_BURN_SPEED.get().floatValue();
                        float efficiency = getFuelEfficiency() / 100.0f;

                        burnTime = (int) Math.floor(burningTime * burnSpeed * efficiency);
                        burnDuration = burnTime;

                        // Remove the fuel
                        fuel.shrink(1);
                    }
                }
            }
        }

        super.serverTick();
    }

    @Override
    public boolean isActive() {
        return canAct() && hasEnergy() && isGenerating();
    }

    public boolean isGenerating() {
        if (level == null) {
            return false;
        }

        return burnTime > 0;
    }

    public float getBurnProgress() {
        if (burnDuration != 0) {
            return burnTime / (float) burnDuration;
        }

        return 0;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new StirlingGeneratorMenu(pContainerId, pInventory, this);
    }

    @Override
    protected void onInventoryContentsChanged(int slot) {
        super.onInventoryContentsChanged(slot);
        if (FUEL.isSlot(slot)) {
            updateMachineState(MachineState.EMPTY_INPUT, FUEL.getItemStack(this).isEmpty());
        }
    }

    protected MachineEnergyStorage createEnergyStorage(EnergyIOMode energyIOMode, Supplier<Integer> capacity,
            Supplier<Integer> usageRate) {
        return new MachineEnergyStorage(this, energyIOMode, capacity, usageRate) {
            @Override
            protected void onContentsChanged() {
                setChanged();
                updateMachineState(MachineState.FULL_POWER,
                        (getEnergyStorage().getEnergyStored() >= getEnergyStorage().getMaxEnergyStored())
                                && isCapacitorInstalled());
            }
        };
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider lookupProvider) {
        super.loadAdditional(pTag, lookupProvider);

        if (pTag.contains(MachineNBTKeys.BURN_TIME, CompoundTag.TAG_INT)) {
            burnTime = pTag.getInt(MachineNBTKeys.BURN_TIME);
        }

        if (pTag.contains(MachineNBTKeys.BURN_DURATION, CompoundTag.TAG_INT)) {
            burnDuration = pTag.getInt(MachineNBTKeys.BURN_DURATION);
        }

        updateMachineState(MachineState.NO_POWER, false);
        updateMachineState(MachineState.FULL_POWER,
                (getEnergyStorage().getEnergyStored() >= getEnergyStorage().getMaxEnergyStored())
                        && isCapacitorInstalled());
        updateMachineState(MachineState.EMPTY_INPUT, FUEL.getItemStack(this).isEmpty());
    }

    @Override
    public void saveAdditional(CompoundTag pTag, HolderLookup.Provider lookupProvider) {
        super.saveAdditional(pTag, lookupProvider);

        pTag.putInt(MachineNBTKeys.BURN_TIME, burnTime);
        pTag.putInt(MachineNBTKeys.BURN_DURATION, burnDuration);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        updateMachineState(MachineState.NO_POWER, false);
        updateMachineState(MachineState.FULL_POWER,
                (getEnergyStorage().getEnergyStored() >= getEnergyStorage().getMaxEnergyStored())
                        && isCapacitorInstalled());
        updateMachineState(MachineState.EMPTY_INPUT, FUEL.getItemStack(this).isEmpty());
    }
}
