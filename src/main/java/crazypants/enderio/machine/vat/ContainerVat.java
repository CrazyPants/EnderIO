package crazypants.enderio.machine.vat;

import crazypants.enderio.machine.gui.AbstractMachineContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerVat extends AbstractMachineContainer<TileVat> {

  // JEI wants this data without giving us a chance to instantiate a container
  public static int FIRST_RECIPE_SLOT = 0;
  public static int NUM_RECIPE_SLOT = 2;
  public static int FIRST_INVENTORY_SLOT = 2 + 0 + 0; // input + output + upgrade
  public static int NUM_INVENTORY_SLOT = 4 * 9;

  public ContainerVat(InventoryPlayer playerInv, TileVat te) {
    super(playerInv, te);
  }

  @Override
  protected void addMachineSlots(InventoryPlayer playerInv) {
    addSlotToContainer(new Slot(getInv(), 0, 56, 12) {
      @Override
      public boolean isItemValid(@Nonnull ItemStack itemStack) {
        return getInv().isItemValidForSlot(0, itemStack);
      }
    });
    addSlotToContainer(new Slot(getInv(), 1, 105, 12) {
      @Override
      public boolean isItemValid(@Nonnull ItemStack itemStack) {
        return getInv().isItemValidForSlot(1, itemStack);
      }
    });
  }

}
