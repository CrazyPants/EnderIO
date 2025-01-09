//package com.enderio.conduits.common.menu;
//
//import com.enderio.conduits.api.bundle.SlotType;
//import com.enderio.conduits.common.conduit.ConduitBundle;
//import com.enderio.conduits.common.conduit.SlotData;
//import net.minecraft.core.Direction;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.neoforged.neoforge.items.IItemHandler;
//import net.neoforged.neoforge.items.SlotItemHandler;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.function.Supplier;
//
//public class ConduitSlot extends SlotItemHandler {
//
//    private final Supplier<Direction> visibleDirection;
//    private final Supplier<Integer> visibleType;
//
//    private final Direction visibleForDirection;
//    private final int visibleForType;
//    private final SlotType slotType;
//    private final ConduitBundle bundle;
//
//    public ConduitSlot(ConduitBundle bundle, IItemHandler itemHandler, Supplier<Direction> visibleDirection, Direction visibleForDirection, Supplier<Integer> visibleType, int visibleForType, SlotType slotType) {
//        super(itemHandler, new SlotData(visibleForDirection, visibleForType, slotType).slotIndex(), slotType.getX(), slotType.getY());
//        this.visibleDirection = visibleDirection;
//        this.visibleType = visibleType;
//        this.visibleForDirection = visibleForDirection;
//        this.visibleForType = visibleForType;
//        this.slotType = slotType;
//        this.bundle = bundle;
//    }
//
//    @Override
//    public boolean mayPlace(ItemStack stack) {
//        return isActive() && super.mayPlace(stack);
//    }
//
//    @Override
//    public boolean mayPickup(Player playerIn) {
//        return isActive() && super.mayPickup(playerIn);
//    }
//
//    @Override
//    @NotNull
//    public ItemStack remove(int amount) {
//        return isActive() ? super.remove(amount) : ItemStack.EMPTY;
//    }
//
//    @Override
//    public boolean isActive() {
//        return visibleDirection.get() == visibleForDirection
//            && visibleType.get() == visibleForType
//            && bundle.getConduits().size() > visibleForType
//            && slotType.isAvailableFor(bundle.getConduits().get(visibleForType).value().getMenuData());
//    }
//}
