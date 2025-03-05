package com.enderio.machines.common.blocks.base.blockentity;

import com.enderio.base.api.UseOnly;
import com.enderio.base.api.capacitor.CapacitorData;
import com.enderio.base.api.capacitor.CapacitorScalable;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.base.common.blockentity.MachineInstallable;
import com.enderio.base.common.init.EIODataComponents;
import com.enderio.base.common.item.capacitors.CapacitorItem;
import com.enderio.machines.common.MachineNBTKeys;
import com.enderio.machines.common.blocks.base.blockentity.flags.CapacitorSupport;
import com.enderio.machines.common.blocks.base.energy.PoweredMachineEnergyStorage;
import com.enderio.machines.common.blocks.base.inventory.MachineInventory;
import com.enderio.machines.common.blocks.base.state.MachineState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.energy.IEnergyStorage;

public abstract class PoweredMachineBlockEntity extends MachineBlockEntity implements MachineInstallable {

    public static final ICapabilityProvider<PoweredMachineBlockEntity, Direction, IEnergyStorage> ENERGY_STORAGE_PROVIDER = (
            be, side) -> side == null ? be.energyStorage : be.energyStorage.getSided(side);

    private final CapacitorSupport capacitorSupport;
    private CapacitorData capacitorData = CapacitorData.NONE;
    private boolean isCapacitorDataDirty;

    private final EnergyIOMode energyIOMode;
    private final CapacitorScalable scalableEnergyCapacity;
    private final CapacitorScalable scalableMaxEnergyUse;

    private final PoweredMachineEnergyStorage energyStorage;

    public PoweredMachineBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState,
            boolean isIoConfigMutable, CapacitorSupport capacitorSupport, EnergyIOMode energyIOMode,
            CapacitorScalable scalableEnergyCapacity, CapacitorScalable scalableMaxEnergyUse) {
        super(type, worldPosition, blockState, isIoConfigMutable);

        this.capacitorSupport = capacitorSupport;
        this.energyIOMode = energyIOMode;
        this.scalableEnergyCapacity = scalableEnergyCapacity;
        this.scalableMaxEnergyUse = scalableMaxEnergyUse;

        // Sanity check for capacitors.
        if (supportsCapacitor() && (!hasInventory() || !getInventory().layout().supportsCapacitor())) {
            throw new IllegalStateException(
                    "A machine which accepts a capacitor must have an inventory with a capacitor slot!");
        }

        energyStorage = createEnergyStorage();
    }

    // TODO: Temporary to support the primitive alloy smelter in its current form.
    @Deprecated(forRemoval = true, since = "7.1")
    protected PoweredMachineEnergyStorage createEnergyStorage() {
        return new PoweredMachineEnergyStorage(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updatePowerState();
        updateCapacitorState();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        updatePowerState();
        updateCapacitorState();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updatePowerState();
    }

    @Override
    protected void onInventoryContentsChanged(int slot) {
        super.onInventoryContentsChanged(slot);
        updateCapacitorData();
    }

    // region Energy Storage

    public PoweredMachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @UseOnly(LogicalSide.CLIENT)
    public final void clientSetEnergyStored(int energyStored) {
        energyStorage.setEnergyStored(energyStored);
    }

    public final boolean hasEnergy() {
        // If the machine has no capacitor, you cannot interact with it's energy storage
        if (requiresCapacitor() && !isCapacitorInstalled()) {
            return false;
        }

        return energyStorage.getEnergyStored() > 0;
    }

    public final int getMaxEnergyStored() {
        // TODO: Scalable might need redesigned to just scale directly...
        return scalableEnergyCapacity.scaleI(this::getCapacitorData).get();
    }

    public final int getMaxEnergyUse() {
        return scalableMaxEnergyUse.scaleI(this::getCapacitorData).get();
    }

    public final EnergyIOMode energyIOMode() {
        return energyIOMode;
    }

    private void updatePowerState() {
        updateMachineState(MachineState.NO_POWER, energyStorage.getEnergyStored() <= 0);
    }

    // region Distribution

    @Override
    protected int distributeResourcesInterval() {
        return energyIOMode.canOutput() ? 1 : super.distributeResourcesInterval();
    }

    @Override
    protected void distributeResources(Direction side) {
        super.distributeResources(side);

        if (energyIOMode.canOutput() && getIOMode(side).canPush()) {
            distributeEnergy(side);
        }
    }

    private void distributeEnergy(Direction side) {
        // Get the other energy handler
        IEnergyStorage otherHandler = getNeighbouringCapability(Capabilities.EnergyStorage.BLOCK, side);
        if (otherHandler == null) {
            return;
        }

        // If the other handler can receive power transmit ours
        if (otherHandler.canReceive()) {
            int energyToReceive = energyStorage.extractEnergy(Integer.MAX_VALUE, true);
            int received = otherHandler.receiveEnergy(energyToReceive, false);
            energyStorage.extractEnergy(received, false);
        }
    }

    // endregion

    // endregion

    // region Capacitor

    public final CapacitorSupport capacitorSupport() {
        return capacitorSupport;
    }

    public final boolean supportsCapacitor() {
        return capacitorSupport != CapacitorSupport.NONE;
    }

    public final boolean requiresCapacitor() {
        return capacitorSupport == CapacitorSupport.REQUIRED;
    }

    public boolean isCapacitorInstalled() {
        if (!supportsCapacitor()) {
            // TODO: Should this be an exception because we do not support capacitors?
            return false;
        }

        if (level != null && level.isClientSide()) {
            return !getCapacitorItem().isEmpty();
        }

        if (isCapacitorDataDirty) {
            updateCapacitorData();
        }

        return !capacitorData.equals(CapacitorData.NONE);
    }

    @UseOnly(LogicalSide.SERVER)
    public CapacitorData getCapacitorData() {
        if (!supportsCapacitor()) {
            throw new IllegalStateException("Unable to get capacitor data, this machine does not support capacitors!");
        }

        if (isCapacitorDataDirty) {
            updateCapacitorData();
        }

        return capacitorData;
    }

    public final ItemStack getCapacitorItem() {
        MachineInventory inventory = getInventory();
        if (inventory == null) {
            return ItemStack.EMPTY;
        }

        return inventory.getStackInSlot(inventory.layout().getCapacitorSlot());
    }

    public final int getCapacitorSlotIndex() {
        if (!hasInventory()) {
            throw new IllegalStateException("Attempt to get capacitor slot for machine with no inventory!");
        }

        var layout = getInventory().layout();
        if (!layout.supportsCapacitor()) {
            throw new IllegalStateException("Unable to get capacitor slot index, inventory has no capacitor slot.");
        }

        return layout.getCapacitorSlot();
    }

    protected void updateCapacitorData() {
        isCapacitorDataDirty = false;

        if (supportsCapacitor()) {
            capacitorData = getCapacitorItem().getOrDefault(EIODataComponents.CAPACITOR_DATA, CapacitorData.NONE);
            if (isCapacitorInstalled() && energyStorage.getEnergyStored() > energyStorage.getMaxEnergyStored()) {
                energyStorage.setEnergyStored(energyStorage.getMaxEnergyStored());
            }
            updateCapacitorState();
        }
    }

    private void updateCapacitorState() {
        updateMachineState(MachineState.NO_CAPACITOR, supportsCapacitor() && !isCapacitorInstalled());
    }

    // region MachineInstallable Implementation

    @Override
    public InteractionResult tryItemInstall(ItemStack stack, UseOnContext context) {
        if (stack.getItem() instanceof CapacitorItem && supportsCapacitor() && !isCapacitorInstalled()) {
            MachineInventory inventory = getInventory();
            if (inventory != null) {
                inventory.setStackInSlot(inventory.layout().getCapacitorSlot(), stack.copyWithCount(1));
                stack.shrink(1);
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
            }
        }

        return InteractionResult.PASS;
    }

    // endregion

    // endregion

    // region Serialization

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(MachineNBTKeys.ENERGY_STORED, energyStorage.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains(MachineNBTKeys.ENERGY_STORED, Tag.TAG_INT)) {
            energyStorage.deserializeNBT(registries, (IntTag) tag.get(MachineNBTKeys.ENERGY_STORED));
        } else if (tag.contains(MachineNBTKeys.ENERGY, Tag.TAG_COMPOUND)) {
            // SUPPORT LEGACY STORAGE FORMAT
            CompoundTag energyTag = tag.getCompound(MachineNBTKeys.ENERGY);

            if (energyTag.contains(MachineNBTKeys.ENERGY_STORED)) {
                energyStorage.setEnergyStored(energyTag.getInt(MachineNBTKeys.ENERGY_STORED));
            }
        }

        updateCapacitorData();
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        energyStorage.setEnergyStored(componentInput.getOrDefault(EIODataComponents.ENERGY, 0));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(EIODataComponents.ENERGY, energyStorage.getEnergyStored());
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(MachineNBTKeys.ENERGY_STORED);
    }

    // endregion
}
