package crazypants.enderio.conduits.gui;

import javax.annotation.Nonnull;

import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.conduit.item.ItemFunctionUpgrade;
import crazypants.enderio.base.filter.IFilter;
import crazypants.enderio.base.filter.IItemFilterUpgrade;
import crazypants.enderio.base.filter.capability.IFilterHolder;
import crazypants.enderio.base.filter.fluid.items.IItemFilterFluidUpgrade;
import crazypants.enderio.base.filter.item.items.IItemFilterItemUpgrade;
import crazypants.enderio.conduits.capability.IUpgradeHolder;
import crazypants.enderio.conduits.conduit.item.IItemConduit;
import crazypants.enderio.conduits.conduit.liquid.ILiquidConduit;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * The Inventory for Holding Conduit Upgrades
 */
public class InventoryUpgrades implements IItemHandlerModifiable {

  private @Nonnull EnumFacing dir;

  private IFilterHolder<IFilter> filterHolder;
  private IUpgradeHolder upgradeHolder;

  public InventoryUpgrades(@Nonnull EnumFacing dir) {
    this.dir = dir;
  }

  public void setFilterHolder(IFilterHolder<IFilter> filterHolder) {
    this.filterHolder = filterHolder;
  }

  public void setUpgradeHolder(IUpgradeHolder upgradeHolder) {
    this.upgradeHolder = upgradeHolder;
  }

  @Override
  @Nonnull
  public ItemStack getStackInSlot(int slot) {
    switch (slot) {
    case 0:
      return upgradeHolder != null ? upgradeHolder.getUpgradeStack(dir.ordinal()) : ItemStack.EMPTY;
    case 2:
      return filterHolder != null ? filterHolder.getFilterStack(filterHolder.getInputFilterIndex(), dir.ordinal()) : ItemStack.EMPTY;
    case 3:
      return filterHolder != null ? filterHolder.getFilterStack(filterHolder.getOutputFilterIndex(), dir.ordinal()) : ItemStack.EMPTY;
    default:
      return ItemStack.EMPTY;
    }
  }

  @Nonnull
  @Override
  public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
    ItemStack slotStack = stack.splitStack(getSlotLimit(slot));

    if (!simulate) {
      setInventorySlotContents(slot, slotStack);
    }
    return stack;
  }

  @Nonnull
  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    ItemStack current = getStackInSlot(slot);
    if (current.isEmpty()) {
      return current;
    }
    ItemStack result;
    ItemStack remaining;
    if (amount >= current.getCount()) {
      result = current.copy();
      remaining = ItemStack.EMPTY;
    } else {
      result = current.copy();
      result.setCount(amount);
      remaining = current.copy();
      remaining.shrink(amount);
    }

    if (!simulate) {
      setInventorySlotContents(slot, remaining);
    }
    return result;
  }

  private void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
    switch (slot) {
    case 0:
      if (upgradeHolder != null) {
        upgradeHolder.setUpgradeStack(dir.ordinal(), stack);
      }
      break;
    case 2:
      if (filterHolder != null) {
        filterHolder.setFilterStack(filterHolder.getInputFilterIndex(), dir.ordinal(), stack);
      }
      break;
    case 3:
      if (filterHolder != null) {
        filterHolder.setFilterStack(filterHolder.getOutputFilterIndex(), dir.ordinal(), stack);
      }
      break;
    }
  }

  public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack, IConduit con) {
    if (stack.isEmpty()) {
      return false;
    }
    switch (slot) {
    case 0:
      return stack.getItem() instanceof ItemFunctionUpgrade;
    case 2:
      return isFilterUpgradeAccepted(stack, con);
    case 3:
      return isFilterUpgradeAccepted(stack, con);
    }
    return false;
  }

  private boolean isFilterUpgradeAccepted(@Nonnull ItemStack stack, IConduit con) {
    if (con instanceof IItemConduit) {
      return stack.getItem() instanceof IItemFilterItemUpgrade;
    } else if (con instanceof ILiquidConduit) {
      return stack.getItem() instanceof IItemFilterFluidUpgrade;
    }
    return stack.getItem() instanceof IItemFilterUpgrade;
  }

  @Override
  public int getSlots() {
    return 4;
  }

  @Override
  public int getSlotLimit(int slot) {
    return slot == 0 ? 15 : 1;
  }

  @Override
  public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
    setInventorySlotContents(slot, stack);
  }

}