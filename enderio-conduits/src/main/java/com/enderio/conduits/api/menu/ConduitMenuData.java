package com.enderio.conduits.api.menu;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.core.common.network.menu.SyncSlot;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;

// TODO: Rubbish name
public abstract class ConduitMenuData {

    public interface ConduitMenuAccess {
        Holder<Conduit<?>> conduit();
        Direction side();
        ConduitNode conduitNode();
        <T extends ConnectionConfig> T getConnectionConfig(ConnectionConfigType<T> type);
        void setConnectionConfig(ConnectionConfig config);

        <T extends SyncSlot> T addSyncSlot(T syncSlot);
        <T extends SyncSlot> T addUpdatableSyncSlot(T syncSlot);
        void updateSlot(SyncSlot syncSlot);
    }

    protected final ConduitMenuAccess menu;

    /**
     * Server constructor
     */
    public ConduitMenuData(ConduitNode node, ConduitMenuAccess menu) {
        this.menu = menu;
    }

    /**
     * Client constructor
     */
    public ConduitMenuData(ConduitMenuAccess menu) {
        this.menu = menu;
    }
}
