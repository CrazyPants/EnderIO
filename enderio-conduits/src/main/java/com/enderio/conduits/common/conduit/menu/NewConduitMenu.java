package com.enderio.conduits.common.conduit.menu;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.ConnectionStatus;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.connection.config.io.IOConnectionConfig;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.common.conduit.bundle.NewConduitBundleBlockEntity;
import com.enderio.conduits.common.init.ConduitBlockEntities;
import com.enderio.conduits.common.init.ConduitMenus;
import com.enderio.core.common.menu.BaseBlockEntityMenu;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class NewConduitMenu extends BaseBlockEntityMenu<NewConduitBundleBlockEntity> {

    public static final int BUTTON_TOGGLE_0_ID = 0;
    public static final int BUTTON_TOGGLE_1_ID = 1;
    public static final int BUTTON_SELECT_CONDUIT_START_ID = 2;
    public static final int BUTTON_SELECT_CONDUIT_ID_COUNT = NewConduitBundleBlockEntity.MAX_CONDUITS;

    private static final int RESERVED_BUTTON_ID_COUNT = BUTTON_SELECT_CONDUIT_START_ID + BUTTON_SELECT_CONDUIT_ID_COUNT + 1;

    private final Direction side;
    private Holder<Conduit<?>> selectedConduit; // TODO: Sync with sync slot instead of using initial menu open?

    public NewConduitMenu(int containerId, Inventory playerInventory, NewConduitBundleBlockEntity blockEntity, Direction side, Holder<Conduit<?>> selectedConduit) {
        super(ConduitMenus.CONDUIT_MENU.get(), containerId, playerInventory, blockEntity);

        this.side = side;
        this.selectedConduit = selectedConduit;

        // TODO: Add conduit slots.

        addPlayerInventorySlots(23, 113);
    }

    public NewConduitMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(ConduitMenus.CONDUIT_MENU.get(), containerId, playerInventory, buf, ConduitBlockEntities.CONDUIT.get());

        side = buf.readEnum(Direction.class);
        selectedConduit = Conduit.STREAM_CODEC.decode(buf);

        // TODO: Add conduit slots.

        addPlayerInventorySlots(23, 113);
    }

    public Direction getSide() {
        return side;
    }

    public Holder<Conduit<?>> getSelectedConduit() {
        return selectedConduit;
    }

    private ConduitNode conduitNode() {
        return getBlockEntity().getConduitNode(selectedConduit);
    }

    public ConnectionConfigType<?> connectionConfigType() {
        return selectedConduit.value().connectionConfigType();
    }

    public ConnectionConfig connectionConfig() {
        return getBlockEntity().getConnectionConfig(side, selectedConduit);
    }

    public <T extends ConnectionConfig> T connectionConfig(ConnectionConfigType<T> type) {
        var config = getBlockEntity().getConnectionConfig(side, selectedConduit);
        if (config.type() == type) {
            //noinspection unchecked
            return (T) config;
        }

        throw new IllegalStateException("Connection config type mismatch");
    }

    public boolean isConnected() {
        return getBlockEntity().getConnectionStatus(side, selectedConduit) == ConnectionStatus.CONNECTED_BLOCK;
    }

    public CompoundTag getClientDataTag() {
        return getBlockEntity().getConduitClientDataTag(selectedConduit);
    }

    @Override
    public boolean stillValid(Player player) {
        // TODO: canBeOrIsValidConnection.
        return super.stillValid(player) && getBlockEntity().hasConduitStrict(selectedConduit) &&
            getBlockEntity().canBeOrIsConnection(side, selectedConduit);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        var bundle = getBlockEntity();
        var currentConfig = connectionConfig();

        switch (id) {
        case BUTTON_TOGGLE_0_ID:
            if (currentConfig instanceof IOConnectionConfig ioConfig) {
                var newConfig = ioConfig.withInsert(!ioConfig.canInsert());
                bundle.setConnectionConfig(side, selectedConduit, newConfig);

                if (newConfig.isConnected()) {
                    bundle.setConnectionStatus(side, selectedConduit, ConnectionStatus.CONNECTED_BLOCK);
                } else {
                    bundle.setConnectionStatus(side, selectedConduit, ConnectionStatus.DISCONNECTED);
                }
            } else {
                // Non IO connections are controlled soley by the connection type.
                var connectionType = bundle.getConnectionStatus(side, selectedConduit);
                if (connectionType == ConnectionStatus.CONNECTED_BLOCK) {
                    bundle.setConnectionStatus(side, selectedConduit, ConnectionStatus.DISCONNECTED);
                } else {
                    bundle.setConnectionStatus(side, selectedConduit, ConnectionStatus.CONNECTED_BLOCK);
                }
            }

            return true;
        case BUTTON_TOGGLE_1_ID:
            if (currentConfig instanceof IOConnectionConfig ioConfig) {
                var newConfig = ioConfig.withExtract(!ioConfig.canExtract());
                bundle.setConnectionConfig(side, selectedConduit, newConfig);

                if (newConfig.isConnected()) {
                    bundle.setConnectionStatus(side, selectedConduit, ConnectionStatus.CONNECTED_BLOCK);
                } else {
                    bundle.setConnectionStatus(side, selectedConduit, ConnectionStatus.DISCONNECTED);
                }
            }
            return true;
        }

        if (id >= BUTTON_SELECT_CONDUIT_START_ID && id <= BUTTON_SELECT_CONDUIT_START_ID + BUTTON_SELECT_CONDUIT_ID_COUNT) {
            // TODO: attempt to change to a different conduit on the same face.
            var conduitList = getBlockEntity().getConduits();

            // TODO Find and switch to conduit and tell the client.
        }

//        if (id >= RESERVED_BUTTON_ID_COUNT) {
//            if (menuConfig().onMenuButtonClicked(conduitNode(), id - RESERVED_BUTTON_ID_COUNT)) {
//                return true;
//            }
//        }

        return super.clickMenuButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }
}
