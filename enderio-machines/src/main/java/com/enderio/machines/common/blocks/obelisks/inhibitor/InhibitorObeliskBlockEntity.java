package com.enderio.machines.common.blocks.obelisks.inhibitor;

import com.enderio.base.api.capacitor.CapacitorModifier;
import com.enderio.base.api.capacitor.LinearScalable;
import com.enderio.base.api.capacitor.QuadraticScalable;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.machines.common.blocks.base.blockentity.flags.CapacitorSupport;
import com.enderio.machines.common.blocks.base.inventory.MachineInventoryLayout;
import com.enderio.machines.common.blocks.obelisks.ObeliskBlockEntity;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.obelisk.InhibitorObeliskManager;
import com.enderio.machines.common.obelisk.ObeliskAreaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.jetbrains.annotations.Nullable;

public class InhibitorObeliskBlockEntity extends ObeliskBlockEntity<InhibitorObeliskBlockEntity> {

    private static final QuadraticScalable ENERGY_CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY,
            MachinesConfig.COMMON.ENERGY.INHIBITOR_CAPACITY);
    private static final LinearScalable ENERGY_USAGE = new LinearScalable(CapacitorModifier.ENERGY_USE,
            MachinesConfig.COMMON.ENERGY.INHIBITOR_USAGE);
    private static final LinearScalable RANGE = new LinearScalable(CapacitorModifier.ENERGY_USE,
            MachinesConfig.COMMON.INHIBITOR_RANGE);

    public InhibitorObeliskBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(MachineBlockEntities.INHIBITOR_OBELISK.get(), worldPosition, blockState, false, CapacitorSupport.REQUIRED,
                EnergyIOMode.Input, ENERGY_CAPACITY, ENERGY_USAGE, false);
    }

    @Override
    protected @Nullable ObeliskAreaManager<InhibitorObeliskBlockEntity> getAreaManager(ServerLevel level) {
        return InhibitorObeliskManager.getManager(level);
    }

    @Override
    public @Nullable MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder().capacitor().build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player pPlayer) {
        return new InhibitorObeliskMenu(containerId, playerInventory, this);
    }

    @Override
    public int getMaxRange() {
        return RANGE.scaleI(this::getCapacitorData).get();
    }

    @Override
    public String getColor() {
        return MachinesConfig.CLIENT.BLOCKS.INHIBITOR_RANGE_COLOR.get();
    }

    public boolean handleTeleportEvent(EntityTeleportEvent event) {
        AABB aabb = getAABB();
        if (aabb != null && isActive() && (aabb.contains(event.getTargetX(), event.getTargetY(), event.getTargetZ())
                || aabb.contains(event.getPrevX(), event.getPrevY(), event.getPrevZ()))) {
            event.setCanceled(true);
            return true;
        }
        return false;
    }
}
