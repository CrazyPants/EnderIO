package com.enderio.core.common.menu;

import com.enderio.core.common.network.menu.BlockEntityMenuHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public abstract class BaseBlockEntityMenu<T extends BlockEntity> extends BaseEnderMenu {

    private final T blockEntity;

    /**
     * Server menu constructor
     */
    protected BaseBlockEntityMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory,
            T blockEntity) {
        super(menuType, containerId, playerInventory);
        this.blockEntity = blockEntity;
    }

    /**
     * Client menu constructor.
     * Loads the block entity from the buffer and ensures the block entity type matches
     */
    @SafeVarargs
    protected BaseBlockEntityMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory,
            RegistryFriendlyByteBuf buf, BlockEntityType<? extends T>... blockEntityTypes) {
        super(menuType, containerId, playerInventory);
        this.blockEntity = BlockEntityMenuHelper.getBlockEntityFrom(buf, playerInventory.player.level(),
                blockEntityTypes);
    }

    public T getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(getBlockEntity(), player);
    }
}
