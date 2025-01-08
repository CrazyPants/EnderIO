package com.enderio.conduits.api.bundle;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the inventory of a single conduit in a bundle.
 * The inventory is divided into sides for each possible connection.
 */
@ApiStatus.Experimental
public interface ConduitInventory {
    ItemStack getStackInSlot(Direction side, SlotType slotType);

    void setStackInSlot(Direction side, SlotType slotType, ItemStack stack);
}
