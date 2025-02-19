package com.enderio.machines.common.blocks.wireless_charger;

import com.enderio.machines.common.blocks.base.menu.PoweredMachineMenu;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.init.MachineMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class WirelessChargerMenu extends PoweredMachineMenu<WirelessChargerBlockEntity> {

    public WirelessChargerMenu(int pContainerId, Inventory inventory, WirelessChargerBlockEntity blockEntity) {
        super(MachineMenus.WIRELESS_CHARGER.get(), pContainerId, inventory, blockEntity);
        addSlots();
    }

    public WirelessChargerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(MachineMenus.WIRELESS_CHARGER.get(), containerId, playerInventory, buf,
                MachineBlockEntities.WIRELESS_CHARGER.get());
        addSlots();
    }

    private void addSlots() {
        addCapacitorSlot(12, 60);
        addPlayerInventorySlots(8, 84);
    }

}
