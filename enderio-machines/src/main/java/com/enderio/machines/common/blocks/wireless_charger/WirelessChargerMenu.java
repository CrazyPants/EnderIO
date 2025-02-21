package com.enderio.machines.common.blocks.wireless_charger;

import com.enderio.machines.common.blocks.base.menu.PoweredMachineMenu;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.init.MachineMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class WirelessChargerMenu extends PoweredMachineMenu<WirelessChargerBlockEntity> {

    public static final int INCREASE_BUTTON_ID = 0;
    public static final int DECREASE_BUTTON_ID = 1;
    public static final int VISIBILITY_BUTTON_ID = 2;

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

    public boolean isRangeVisible() {
        // This is synced via the block entity.
        return getBlockEntity().isRangeVisible();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        // noinspection DuplicatedCode
        var blockEntity = getBlockEntity();
        return switch (id) {
        case INCREASE_BUTTON_ID -> {
            blockEntity.increaseRange();
            yield true;
        }
        case DECREASE_BUTTON_ID -> {
            blockEntity.decreaseRange();
            yield true;
        }
        case VISIBILITY_BUTTON_ID -> {
            blockEntity.setRangeVisible(!isRangeVisible());
            yield true;
        }
        default -> false;
        };
    }

}
