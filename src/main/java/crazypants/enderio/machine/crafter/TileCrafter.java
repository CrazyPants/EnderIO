/*package crazypants.enderio.machine.crafter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.enderio.core.common.inventory.InventorySlot;
import com.enderio.core.common.util.ItemUtil;
import com.mojang.authlib.GameProfile;

import crazypants.enderio.config.Config;
import crazypants.enderio.machine.MachineObject;
import crazypants.enderio.machine.base.te.AbstractCapabilityPoweredMachineEntity;
import crazypants.enderio.machine.fakeplayer.FakePlayerEIO;
import crazypants.enderio.paint.IPaintable;
import info.loenwind.autosave.annotations.Storable;
import info.loenwind.autosave.annotations.Store;
import info.loenwind.autosave.handlers.minecraft.HandleItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

@Storable
public class TileCrafter extends AbstractCapabilityPoweredMachineEntity implements IPaintable.IPaintableTileEntity {

  @Store
  DummyCraftingGrid craftingGrid = new DummyCraftingGrid();

  @Store(handler = HandleItemStack.HandleItemStackArrayList.class)
  private final List<ItemStack> containerItems;

  @Store
  private boolean bufferStacks = true;

  private long ticksSinceLastCraft = 0;

  private FakePlayerEIO playerInst;

  public TileCrafter() {
    super(MachineObject.blockCrafter);
    containerItems = new ArrayList<ItemStack>();
  }

  @Override
  public @Nonnull String getMachineName() {
    return MachineObject.blockCrafter.getUnlocalisedName();
  }

//  @Override
//  public boolean isMachineItemValidForSlot(int slot, ItemStack itemstack) {
//    if (!slotDefinition.isInputSlot(slot)) {
//      return false;
//    }
//    return craftingGrid.inv[slot] != null && compareDamageable(itemstack, craftingGrid.inv[slot]);
//  }

  @Override
  public boolean isActive() {
    return false;
  }

  @Override
  protected boolean processTasks(boolean redstoneCheck) {
    ticksSinceLastCraft++;
    if (!redstoneCheck || !craftingGrid.hasValidRecipe() || !canMergeOutput() || !hasRequiredPower()) {
      return false;
    }
    int ticksPerCraft = getTicksPerCraft();
    if (ticksSinceLastCraft <= ticksPerCraft) {
      return false;
    }
    ticksSinceLastCraft = 0;

    // process buffered container items
    if (!containerItems.isEmpty()) {
      Iterator<ItemStack> iter = containerItems.iterator();
      while (iter.hasNext()) {
        ItemStack stack = iter.next();
        InventorySlot output = outputSlots.getSlot(0);
        ItemStack rem = output.insertItem(0, stack, false);
        if (rem.isEmpty()) {
          iter.remove();
        } else {
          stack.setCount(rem.getCount());
        }
      }
      return false;
    }

    if (craftRecipe()) {
        getEnergy().extractEnergy(getPowerUsePerCraft(), false);
    }
    return false;
  }

  private boolean hasRequiredPower() {
    return true; //getEnergyStored() >= getPowerUsePerCraft();
  }

//  @Override
//  public int getPowerUsePerTick() {
//    return (int) Math.ceil(getPowerUsePerCraft() / (double) getTicksPerCraft());
//  }

  protected int getPowerUsePerCraft() {
    return Config.crafterRfPerCraft;
  }

  public int getTicksPerCraft() {
    return 20;//Math.max(1, CapacitorKey.CRAFTER_TICKS.get(getCapacitorData()));
  }

  static boolean compareDamageable(ItemStack stack, ItemStack req) {
    if (stack.isItemEqual(req)) {
      return true;
    }
    if (stack.isItemStackDamageable() && stack.getItem() == req.getItem()) {
      return stack.getItemDamage() < stack.getMaxDamage();
    }
    return false;
  }

  private static final UUID uuid = UUID.fromString("9b381cae-3c95-4a64-b958-1e25b0a4c790");
  private static final GameProfile DUMMY_PROFILE = new GameProfile(uuid, "[EioCrafter]");

  private boolean craftRecipe() {

    // (1) Find the items to craft with and put a copy into a temp crafting grid;
    // also record what was used to destroy it later
    InventoryCrafting inv = new InventoryCrafting(new Container() {
      @Override
      public boolean canInteractWith(EntityPlayer var1) {
        return false;
      }
    }, 3, 3);

    int[] usedItems = new int[9];

    for (int j = 0; j < 9; j++) {
      ItemStack req = craftingGrid.getStackInSlot(j);
      if (!req.isEmpty()) {
        for (int i = 0; i < 9; i++) {
          if (inventory[i] != null && inventory[i].getCount() > usedItems[i] && compareDamageable(inventory[i], req)) {
            req = ItemStack.EMPTY;
            usedItems[i]++;
            ItemStack craftingItem = inventory[i].copy();
            craftingItem.setCount(1);
            inv.setInventorySlotContents(j, craftingItem);
            break;
          }
        }
        if (!req.isEmpty()) {
          return false;
        }
      }
    }

    // (2) Try to craft with the temp grid
    ItemStack output = CraftingManager.getInstance().findMatchingRecipe(inv, world);

    // (3) If we got a result, ...
    if (output != null) {
      if (playerInst == null) {
        playerInst = new FakePlayerEIO(world, getLocation(), DUMMY_PROFILE);
        playerInst.setOwner(getOwner());
      }
      MinecraftForge.EVENT_BUS.post(new ItemCraftedEvent(playerInst, output, inv));

      NonNullList<ItemStack> remaining = CraftingManager.getInstance().getRemainingItems(inv, world);

      // (3a) ... remove the used up items and ...
      for (int i = 0; i < 9; i++) {
        for (int j = 0; j < usedItems[i] && inventory[i] != null; j++) {
          setInventorySlotContents(i, eatOneItemForCrafting(i, inventory[i].copy(), remaining));
        }
      }
      
      for(ItemStack stack : remaining) {
        if(stack != null) {
          containerItems.add(stack.copy());
        }
      }

      // (3b) ... put the result into its slot
      if (inventory[9] == null) {
        setInventorySlotContents(9, output);
      } else if (ItemUtil.areStackMergable(inventory[9], output)) {
        ItemStack cur = inventory[9].copy();
        cur.grow(output.getCount());
        if (cur.getCount() > cur.getMaxStackSize()) {
          // we check beforehand that there is enough free space, but some mod may return different
          // amounts based on the nbt of the input items (e.g. magical wood)
          ItemStack overflow = cur.copy();
          overflow.setCount(cur.getCount() - cur.getMaxStackSize());
          cur.setCount(cur.getMaxStackSize());
          containerItems.add(overflow);
        }
        setInventorySlotContents(9, cur);
      } else {
        // some mod may return different nbt based on the nbt of the input items (e.g. TE machines?)
        containerItems.add(output);
      }
    } else {
      // Crafting failed. This is not supposed to happen, but if a recipe is nbt-sensitive, it can.
      // To avoid being stuck in a dead loop, we flush the non-working input items.
      for (int j = 0; j < 9; j++) {
        if (usedItems[j] > 0 && inventory[j] != null) {
          ItemStack rejected = inventory[j].copy();
          rejected.setCount(Math.min(inventory[j].getCount(), usedItems[j]));
          containerItems.add(rejected);
          if (inventory[j].getCount() <= usedItems[j]) {
            inventory[j] = null;
          } else {
            inventory[j].getCount() -= usedItems[j];
          }
        }
      }
    }

    return true;
  }

  private ItemStack eatOneItemForCrafting(int slot, ItemStack avail, ItemStack[] remaining) {
    //if one of the remaining items is the container item for the input, place the remaining item in the same grid
    if (remaining != null && remaining.length > 0 && avail.getItem().hasContainerItem(avail)) {
      ItemStack used = avail.getItem().getContainerItem(avail);
      if(used != null) {
        for(int i=0; i < remaining.length;  i++) {
          ItemStack s  = remaining[i];
          if(s != null && s.isItemEqualIgnoreDurability(used) && isItemValidForSlot(slot, s)) {
            remaining[i] = null;
            return s;
          }
        }
      }
    }
    avail.shrink(1);
    if (avail.getCount() == 0) {
      avail = null;
    }
    return avail;
  }

  private boolean canMergeOutput() {
    if (inventory[9] == null) {
      return true;
    }
    ItemStack output = craftingGrid.getOutput();
    if (!ItemUtil.areStackMergable(inventory[9], output)) {
      return false;
    }
    return output.getMaxStackSize() >= (inventory[9].getCount() + output.getCount());
  }

  @Override
  public int getInventoryStackLimit() {
    return bufferStacks ? 64 : 1;
  }

  public boolean isBufferStacks() {
    return bufferStacks;
  }

  public void setBufferStacks(boolean bufferStacks) {
    this.bufferStacks = bufferStacks;
  }

  public void updateCraftingOutput() {
    InventoryCrafting inv = new InventoryCrafting(new Container() {

      @Override
      public boolean canInteractWith(EntityPlayer var1) {
        return false;
      }
    }, 3, 3);

    for (int i = 0; i < 9; i++) {
      inv.setInventorySlotContents(i, craftingGrid.getStackInSlot(i));
    }
    ItemStack matches = CraftingManager.getInstance().findMatchingRecipe(inv, world);
    craftingGrid.setInventorySlotContents(9, matches);
    markDirty();
  }

}
*/