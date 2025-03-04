package com.enderio.machines.common.blocks.obelisks.inhibitor;

import com.enderio.machines.common.blocks.obelisks.ObeliskMenu;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.init.MachineMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class InhibitorObeliskMenu extends ObeliskMenu<InhibitorObeliskBlockEntity> {

    public InhibitorObeliskMenu(int pContainerId, Inventory inventory, InhibitorObeliskBlockEntity blockEntity) {
        super(MachineMenus.INHIBITOR_OBELISK.get(), pContainerId, inventory, blockEntity);
        addSlots();
    }

    public InhibitorObeliskMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(MachineMenus.INHIBITOR_OBELISK.get(), containerId, playerInventory, buf,
                MachineBlockEntities.INHIBITOR_OBELISK.get());
        addSlots();
    }

    private void addSlots() {
        addCapacitorSlot(12, 60);
        addPlayerInventorySlots(8, 84);
    }
}
