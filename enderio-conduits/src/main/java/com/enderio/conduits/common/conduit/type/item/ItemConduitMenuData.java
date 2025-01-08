package com.enderio.conduits.common.conduit.type.item;

import com.enderio.conduits.api.menu.ConduitMenuData;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.core.common.network.menu.BoolSyncSlot;

public class ItemConduitMenuData extends ConduitMenuData {

    private final BoolSyncSlot isRoundRobinSlot;
    private final BoolSyncSlot isSelfFeedSlot;

    public ItemConduitMenuData(ConduitNode node, ConduitMenuAccess menu) {
        super(node, menu);

        // TODO: add updatable sync slot.
        isRoundRobinSlot = menu.addUpdatableSyncSlot(
            BoolSyncSlot.readOnly(() -> connectionConfig().isRoundRobin())
        );

        isSelfFeedSlot = menu.addUpdatableSyncSlot(
            BoolSyncSlot.readOnly(() -> connectionConfig().isSelfFeed())
        );
    }

    public ItemConduitMenuData(ConduitMenuAccess menu) {
        super(menu);

        isRoundRobinSlot = BoolSyncSlot.standalone();
        isSelfFeedSlot = BoolSyncSlot.standalone();
    }

    public ItemConduitConnectionConfig connectionConfig() {
        return menu.getConnectionConfig(ItemConduitConnectionConfig.TYPE);
    }

    public boolean isRoundRobin() {
        return isRoundRobinSlot.get();
    }

    public void setIsRoundRobin(boolean isRoundRobin) {
        isRoundRobinSlot.set(isRoundRobin);
        menu.updateSlot(isRoundRobinSlot);
    }

    public boolean isSelfFeed() {
        return isSelfFeedSlot.get();
    }

    public void setIsSelfFeed(boolean isSelfFeed) {
        isSelfFeedSlot.set(isSelfFeed);
        menu.updateSlot(isSelfFeedSlot);
    }
}
