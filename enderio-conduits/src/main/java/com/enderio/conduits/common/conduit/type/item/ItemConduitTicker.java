package com.enderio.conduits.common.conduit.type.item;

import com.enderio.base.api.filter.ItemStackFilter;
import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.ticker.CapabilityAwareConduitTicker;
import com.enderio.conduits.common.init.ConduitTypes;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ItemConduitTicker extends CapabilityAwareConduitTicker<ItemConduit, IItemHandler> {

    @Override
    protected void tickCapabilityGraph(ServerLevel level, ItemConduit conduit, List<CapabilityConnection> inserts,
            List<CapabilityConnection> extracts, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {

        toNextExtract: for (CapabilityConnection extract : extracts) {
            ItemConduitNodeData nodeData = extract.node().getOrCreateNodeData(ConduitTypes.NodeData.ITEM.get());

            IItemHandler extractHandler = extract.capability();
            int extracted = 0;

            int speed = conduit.transferRatePerCycle();

            nextItem: for (int i = 0; i < extractHandler.getSlots(); i++) {
                ItemStack extractedItem = extractHandler.extractItem(i, speed - extracted, true);
                if (extractedItem.isEmpty()) {
                    continue;
                }

                if (extract.extractFilter() instanceof ItemStackFilter itemFilter) {
                    if (!itemFilter.test(extractedItem)) {
                        continue;
                    }
                }

                var connectionConfig = extract.node().getConnectionConfig(extract.direction(), ConduitTypes.ConnectionTypes.ITEM.get());

                int startingIndex = 0;
                if (connectionConfig.isRoundRobin()) {
                    startingIndex = nodeData.getIndex(extract.direction());
                    if (inserts.size() <= startingIndex) {
                        startingIndex = 0;
                    }
                }

                for (int j = startingIndex; j < startingIndex + inserts.size(); j++) {
                    int insertIndex = j % inserts.size();
                    CapabilityConnection insert = inserts.get(insertIndex);

                    if (!connectionConfig.isSelfFeed() && extract.direction() == insert.direction()
                            && extract.pos() == insert.pos()) {
                        continue;
                    }

                    if (insert.insertFilter() instanceof ItemStackFilter itemFilter) {
                        if (!itemFilter.test(extractedItem)) {
                            continue;
                        }
                    }

                    ItemStack notInserted = ItemHandlerHelper.insertItem(insert.capability(), extractedItem, false);
                    int successfullyInserted = extractedItem.getCount() - notInserted.getCount();

                    if (successfullyInserted > 0) {
                        extracted += successfullyInserted;
                        extractHandler.extractItem(i, successfullyInserted, false);
                        if (extracted >= speed || isEmpty(extractHandler, i + 1)) {
                            if (connectionConfig.isRoundRobin()) {
                                nodeData.setIndex(extract.direction(), insertIndex + 1);
                            }
                            continue toNextExtract;
                        } else {
                            continue nextItem;
                        }
                    }
                }
            }
        }
    }

    private boolean isEmpty(IItemHandler itemHandler, int afterIndex) {
        for (var i = afterIndex; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected BlockCapability<IItemHandler, Direction> getCapability() {
        return Capabilities.ItemHandler.BLOCK;
    }
}
