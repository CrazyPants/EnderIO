package com.enderio.machines.common.blocks.obelisks.aversion;

import com.enderio.base.api.capacitor.CapacitorModifier;
import com.enderio.base.api.capacitor.QuadraticScalable;
import com.enderio.base.api.filter.EntityFilter;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.machines.common.blocks.base.blockentity.flags.CapacitorSupport;
import com.enderio.machines.common.blocks.base.inventory.MachineInventoryLayout;
import com.enderio.machines.common.blocks.obelisks.ObeliskBlockEntity;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.obelisk.AversionObeliskManager;
import com.enderio.machines.common.obelisk.ObeliskAreaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import org.jetbrains.annotations.Nullable;

public class AversionObeliskBlockEntity extends ObeliskBlockEntity<AversionObeliskBlockEntity> {

    private static final QuadraticScalable ENERGY_CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY,
            MachinesConfig.COMMON.ENERGY.AVERSION_CAPACITY);
    private static final QuadraticScalable ENERGY_USAGE = new QuadraticScalable(CapacitorModifier.ENERGY_USE,
            MachinesConfig.COMMON.ENERGY.AVERSION_USAGE);
    private static final ModConfigSpec.ConfigValue<Integer> perSpawnUse = MachinesConfig.COMMON.ENERGY.AVERSION_USAGE;


    public AversionObeliskBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(MachineBlockEntities.AVERSION_OBELISK.get(), worldPosition, blockState, false, CapacitorSupport.REQUIRED,
                EnergyIOMode.Input, ENERGY_CAPACITY, ENERGY_USAGE);
    }

    @Override
    protected @Nullable ObeliskAreaManager<AversionObeliskBlockEntity> getAreaManager(ServerLevel level) {
        return AversionObeliskManager.getManager(level);
    }

    @Override
    public @Nullable MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder()
                .inputSlot((integer,
                        itemStack) -> itemStack.getCapability(EIOCapabilities.Filter.ITEM) instanceof EntityFilter)
                .slotAccess(FILTER)
                .capacitor()
                .build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AversionObeliskMenu(containerId, pPlayerInventory, this);
    }

    @Override
    public int getMaxRange() {
        return 32;
    }

    @Override
    public String getColor() {
        return MachinesConfig.CLIENT.BLOCKS.AVERSION_RANGE_COLOR.get();
    }

    @Override
    public boolean canAct() {
        return super.canAct() && FILTER.getItemStack(this).getCapability(EIOCapabilities.Filter.ITEM) instanceof EntityFilter;
    }

    public boolean handleSpawnEvent(FinalizeSpawnEvent event) {
        if (!isActive() || getAABB() == null) {
            return false;
        }

        if (FILTER.getItemStack(this).getCapability(EIOCapabilities.Filter.ITEM) instanceof EntityFilter entityFilter) {
            if (!entityFilter.test(event.getEntity())) {
                return false;
            }
        }

        if (isActive() && getAABB().contains(event.getX(), event.getY(), event.getZ())) {
            int cost = ENERGY_USAGE.base().get(); // TODO scale on entity? The issue is that it needs the
                                                  // energy "now" and can't wait for it like other machines
            int energy = getEnergyStorage().consumeEnergy(cost, true);
            if (energy == cost) {
                event.setSpawnCancelled(true);
                getEnergyStorage().consumeEnergy(cost, true);
                return true;
            }
        }

        return false;
    }
}
