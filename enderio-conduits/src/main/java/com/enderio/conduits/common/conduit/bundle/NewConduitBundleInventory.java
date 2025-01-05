package com.enderio.conduits.common.conduit.bundle;

import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitCapabilities;
import com.enderio.conduits.api.SlotType;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.api.bundle.ConduitInventory;
import com.enderio.conduits.common.conduit.SlotData;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class NewConduitBundleInventory implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    // TODO: Currently conduit inventories are fairly strict - we might be able to
    // improve this in the future?

    public static int MAX_SLOTS_PER_CONDUIT = 3;
    public static int MAX_CONNECTIONS = Direction.values().length; // 6

    private static final String CONDUIT_INV_KEY = "ConduitInv";

    private final ConduitBundleReader conduitBundle;

    private Map<Holder<Conduit<?>>, Map<Direction, NonNullList<ItemStack>>> conduitSlots = new HashMap<>();

    public NewConduitBundleInventory(ConduitBundleReader conduitBundle) {
        this.conduitBundle = conduitBundle;
    }

    protected void onChanged() {
    }

    public ConduitInventory getInventoryFor(Holder<Conduit<?>> conduit) {
        return new InventoryReference(this, conduit);
    }

    public void removeConduit(Holder<Conduit<?>> conduit) {
        conduitSlots.remove(conduit);
    }

    @Override
    public int getSlots() {
        return MAX_SLOTS_PER_CONDUIT * NewConduitBundleBlockEntity.MAX_CONDUITS * MAX_CONNECTIONS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot > getSlots()) {
            throw new IndexOutOfBoundsException("Slot out of bounds");
        }

        SlotData slotData = SlotData.of(slot);
        if (slotData.conduitIndex() >= conduitBundle.getConduits().size()) {
            return ItemStack.EMPTY;
        }

        return getStackInSlot(conduitBundle.getConduits().get(slotData.conduitIndex()), slotData.direction(),
                slotData.slotType());
    }

    public ItemStack getStackInSlot(Holder<Conduit<?>> conduit, Direction side, SlotType slotType) {
        return getStackInSlot(conduit, side, slotType.ordinal());
    }

    public ItemStack getStackInSlot(Holder<Conduit<?>> conduit, Direction side, int slot) {
        if (!conduitBundle.hasConduitStrict(conduit)) {
            throw new IllegalArgumentException("Conduit not found in bundle");
        }

        if (slot < 0 || slot >= MAX_SLOTS_PER_CONDUIT) {
            throw new IndexOutOfBoundsException("Slot out of bounds");
        }

        var conduitSides = conduitSlots.computeIfAbsent(conduit, ignored -> new EnumMap<>(Direction.class));
        var slots = conduitSides.computeIfAbsent(side,
                ignored -> NonNullList.withSize(MAX_SLOTS_PER_CONDUIT, ItemStack.EMPTY));

        return slots.get(slot);
    }

    public void setStackInSlot(Holder<Conduit<?>> conduit, Direction side, int slot, ItemStack stack) {
        if (!conduitBundle.hasConduitStrict(conduit)) {
            throw new IllegalArgumentException("Conduit not found in bundle");
        }

        if (slot < 0 || slot >= MAX_SLOTS_PER_CONDUIT) {
            throw new IndexOutOfBoundsException("Slot out of bounds");
        }

        var conduitSides = conduitSlots.computeIfAbsent(conduit, ignored -> new EnumMap<>(Direction.class));
        var slots = conduitSides.computeIfAbsent(side,
                ignored -> NonNullList.withSize(MAX_SLOTS_PER_CONDUIT, ItemStack.EMPTY));
        slots.set(slot, stack);
        onChanged();
    }

    public void setStackInSlot(Holder<Conduit<?>> conduit, Direction side, SlotType slotType, ItemStack stack) {
        setStackInSlot(conduit, side, slotType.ordinal(), stack);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot > getSlots()) {
            throw new IndexOutOfBoundsException("Slot out of bounds");
        }

        SlotData slotData = SlotData.of(slot);
        if (slotData.conduitIndex() >= conduitBundle.getConduits().size()) {
            return;
        }

        setStackInSlot(conduitBundle.getConduits().get(slotData.conduitIndex()), slotData.direction(),
                slotData.slotType(), stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return null;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot >= getSlots()) {
            return false;
        }

        SlotData slotData = SlotData.of(slot);
        if (slotData.conduitIndex() >= conduitBundle.getConduits().size()) {
            return false;
        }

        Holder<Conduit<?>> conduit = conduitBundle.getConduits().get(slotData.conduitIndex());

        switch (slotData.slotType()) {
        case FILTER_EXTRACT:
        case FILTER_INSERT:
            ResourceFilter resourceFilter = stack.getCapability(EIOCapabilities.Filter.ITEM);
            if (resourceFilter == null) {
                return false;
            }

            return conduit.value().canApplyFilter(slotData.slotType(), resourceFilter);
        case UPGRADE_EXTRACT:
            // Upgrades have been removed
        default:
            return false;
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < getSlots(); i++) {
            ItemStack stack = getStackInSlot(i);
            list.add(i, stack.saveOptional(registries));
        }
        tag.put(CONDUIT_INV_KEY, list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        ListTag list = tag.getList(CONDUIT_INV_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            setStackInSlot(i, ItemStack.parseOptional(registries, list.getCompound(i)));
        }
    }

    private record InventoryReference(NewConduitBundleInventory inventory, Holder<Conduit<?>> conduit)
            implements ConduitInventory {
        @Override
        public ItemStack getStackInSlot(Direction side, SlotType slotType) {
            return inventory.getStackInSlot(conduit, side, slotType);
        }

        @Override
        public void setStackInSlot(Direction side, SlotType slotType, ItemStack stack) {
            inventory.setStackInSlot(conduit, side, slotType, stack);
        }
    }
}
