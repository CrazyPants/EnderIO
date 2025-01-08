package com.enderio.conduits.common.menu;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.common.conduit.ConduitBundle;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlock;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlockEntity;
import com.enderio.conduits.common.init.ConduitMenus;
import com.enderio.conduits.common.network.ConduitMenuSelectionPacket;
import com.enderio.core.common.menu.LegacyBaseBlockEntityMenu;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

public class ConduitMenu extends LegacyBaseBlockEntityMenu<ConduitBundleBlockEntity> {
    private Direction direction;
    private Holder<Conduit<?, ?>> conduit;

    public ConduitMenu(@Nullable ConduitBundleBlockEntity blockEntity, Inventory inventory, int pContainerId,
            Direction direction, Holder<Conduit<?, ?>> conduit) {
        super(ConduitMenus.CONDUIT_MENU.get(), pContainerId, blockEntity, inventory);

        this.direction = direction;
        this.conduit = conduit;
        if (blockEntity != null) {
            IItemHandler conduitItemHandler = blockEntity.getConduitItemHandler();
            for (Direction forDirection : Direction.values()) {
                for (int i = 0; i < ConduitBundle.MAX_CONDUITS; i++) {
                    for (SlotType slotType : SlotType.values()) {
                        addSlot(new ConduitSlot(blockEntity.getBundle(), conduitItemHandler, () -> this.direction,
                                forDirection, () -> blockEntity.getBundle().getConduits().indexOf(this.conduit), i,
                                slotType));
                    }
                }
            }
        }

        addPlayerInventorySlots(23, 113);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack resultItemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack itemInSlot = slot.getItem();
            resultItemStack = itemInSlot.copy();
            if (pIndex < this.slots.size() - PLAYER_INVENTORY_SIZE) {
                if (!this.moveItemStackTo(itemInSlot, this.slots.size() - PLAYER_INVENTORY_SIZE, this.slots.size(),
                        true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemInSlot, 0, this.slots.size() - PLAYER_INVENTORY_SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (itemInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return resultItemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return getBlockEntity() != null && getBlockEntity().stillValid(player)
        // usually called serverside only, but I want to have a clientcheck for type, to
        // close the screen clientside immediatly
                && (player instanceof ServerPlayer || clientValid());
    }

    private boolean clientValid() {
        return getBlockEntity().getBundle().getConduits().contains(conduit)
                && ConduitBundleBlock.canBeOrIsValidConnection(getBlockEntity(), conduit, direction);
    }

    public static ConduitMenu factory(int pContainerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        BlockEntity entity = inventory.player.level().getBlockEntity(buf.readBlockPos());
        Direction direction = buf.readEnum(Direction.class);
        Holder<Conduit<?, ?>> type = Conduit.STREAM_CODEC.decode(buf);
        if (entity instanceof ConduitBundleBlockEntity castBlockEntity) {
            return new ConduitMenu(castBlockEntity, inventory, pContainerId, direction, type);
        }

        LogManager.getLogger().warn("couldn't find BlockEntity");
        return new ConduitMenu(null, inventory, pContainerId, direction, type);
    }

    public Holder<Conduit<?, ?>> getConduit() {
        return conduit;
    }

    public void setConduit(Holder<Conduit<?, ?>> conduit) {
        this.conduit = conduit;

        if (getBlockEntity().hasLevel() && getBlockEntity().getLevel().isClientSide()) {
            PacketDistributor.sendToServer(new ConduitMenuSelectionPacket(this.conduit));
        }
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (getBlockEntity() != null && player instanceof ServerPlayer serverPlayer
                && serverPlayer.serverLevel()
                        .players()
                        .stream()
                        .filter(p -> p != player)
                        .noneMatch(p -> p.containerMenu instanceof ConduitMenu)) {
            getBlockEntity().updateEmptyDynConnection();
        }
    }
}
