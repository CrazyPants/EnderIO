package com.enderio.machines.common.blocks.wireless_charger;

import com.enderio.base.api.UseOnly;
import com.enderio.base.api.capacitor.CapacitorModifier;
import com.enderio.base.api.capacitor.QuadraticScalable;
import com.enderio.base.api.io.IOMode;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.machines.common.MachineNBTKeys;
import com.enderio.machines.common.attachment.ActionRange;
import com.enderio.machines.common.attachment.RangedActor;
import com.enderio.machines.common.blocks.base.blockentity.PoweredMachineBlockEntity;
import com.enderio.machines.common.blocks.base.blockentity.flags.CapacitorSupport;
import com.enderio.machines.common.blocks.base.energy.PoweredMachineEnergyStorage;
import com.enderio.machines.common.blocks.base.inventory.MachineInventoryLayout;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.init.MachineDataComponents;
import com.enderio.machines.common.io.IOConfig;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class WirelessChargerBlockEntity extends PoweredMachineBlockEntity implements RangedActor {

    public static final QuadraticScalable CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY,
            MachinesConfig.COMMON.ENERGY.WIRELESS_CHARGER_CAPACITY);

    public static final QuadraticScalable USAGE = new QuadraticScalable(CapacitorModifier.ENERGY_USE,
            MachinesConfig.COMMON.ENERGY.WIRELESS_CHARGER_USAGE);

    private final ModConfigSpec.ConfigValue<Integer> energyUpkeep;

    private ActionRange actionRange;

    private @javax.annotation.Nullable AABB bounds;

    public WirelessChargerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(MachineBlockEntities.WIRELESS_CHARGER.get(), worldPosition, blockState, true, CapacitorSupport.REQUIRED,
                EnergyIOMode.Input, CAPACITY, USAGE);
        actionRange = new ActionRange(MachinesConfig.COMMON.WIRELESS_CHARGER_RANGE.get(), false);
        energyUpkeep = MachinesConfig.COMMON.ENERGY.WIRELESS_CHARGER_UPKEEP;
    }

    @Override
    public IOConfig getDefaultIOConfig() {
        return IOConfig.of(IOMode.PULL);
    }

    public void chargeItem() {
        if (level == null || bounds == null) {
            return;
        }
        PoweredMachineEnergyStorage energyStorage = getEnergyStorage();
        if (energyStorage.getEnergyStored() <= 0) {
            return;
        }
        int toDistribute = Math.min(energyStorage.getEnergyStored(), getMaxEnergyUse());

        List<Player> players = level.getEntitiesOfClass(Player.class, bounds);
        for (Player player : players) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                @Nullable
                IEnergyStorage cap = stack.getCapability(Capabilities.EnergyStorage.ITEM);
                if (cap != null && cap.canReceive()) {
                    int received = cap.receiveEnergy(toDistribute, false);
                    energyStorage.consumeEnergy(received);
                    toDistribute -= received;
                    if (toDistribute <= 0) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void neighborChanged(Block neighborBlock, BlockPos neighborPos) {
        super.neighborChanged(neighborBlock, neighborPos);
        if (level != null && !level.isClientSide() && getBlockPos().above().equals(neighborPos)) {
            int maxRange = getMaxRange();
            if (getRangeExtension() > 0 || actionRange.range() > maxRange) {
                // Antenna placed or removed from the top so update range
                setActionRange(new ActionRange(maxRange, actionRange.isVisible()));
            }
        }
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (isActive()) {
            getEnergyStorage().consumeEnergy(energyUpkeep.get());
            chargeItem();
        }
    }

    @Override
    public void clientTick() {
        if (level instanceof ClientLevel clientLevel) {
            getActionRange().addClientParticle(clientLevel, getBlockPos(),
                    MachinesConfig.CLIENT.BLOCKS.WIRELESS_CHARGER_RANGE_COLOR.get());
        }
        super.clientTick();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide() && actionRange.range() > getMaxRange()) {
            setActionRange(new ActionRange(getMaxRange(), actionRange.isVisible()));
        }
        updateBounds();
    }

    @Override
    protected void saveAdditionalSynced(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditionalSynced(tag, registries);
        tag.put(MachineNBTKeys.ACTION_RANGE, actionRange.save(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(MachineNBTKeys.ACTION_RANGE)) {
            actionRange = ActionRange.parse(registries, Objects.requireNonNull(tag.get(MachineNBTKeys.ACTION_RANGE)));
        } else {
            actionRange = new ActionRange(getMaxRange(), false);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        var actionRange = components.get(MachineDataComponents.ACTION_RANGE);
        if (actionRange != null) {
            this.actionRange = actionRange;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(MachineDataComponents.ACTION_RANGE, actionRange);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(MachineNBTKeys.ACTION_RANGE);
    }

    @Override
    public boolean isActive() {
        return hasEnergy() && canAct();
    }

    @Override
    public int getMaxRange() {
        return MachinesConfig.COMMON.WIRELESS_CHARGER_RANGE.get() + getRangeExtension();
    }

    private void updateBounds() {
        bounds = new AABB(getBlockPos()).inflate(getRange());
    }

    private int getRangeExtension() {
        int rangeExtension = 0;
        if (level != null) {
            BlockState bs = level.getBlockState(getBlockPos().above());
            if (bs.hasProperty(WirelessAntennaBlock.WIRELESS_RANGE)) {
                rangeExtension = bs.getValue(WirelessAntennaBlock.WIRELESS_RANGE);
            }
        }
        return rangeExtension;
    }

    @Override
    public ActionRange getActionRange() {
        return actionRange;
    }

    @Override
    @UseOnly(LogicalSide.SERVER)
    public void setActionRange(ActionRange actionRange) {
        this.actionRange = actionRange.clamp(0, getMaxRange());
        updateBounds();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder().capacitor().build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player pPlayer) {
        return new WirelessChargerMenu(containerId, playerInventory, this);
    }
}
