package com.enderio.conduits.common.conduit.type.item;

import com.enderio.base.api.filter.ItemStackFilter;
import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.ticker.ChannelIOAwareConduitTicker;
import com.enderio.conduits.api.ticker.NewIOAwareConduitTicker;
import com.enderio.conduits.common.init.ConduitTypes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class ItemConduitTicker extends NewIOAwareConduitTicker<ItemConduit, ItemConduitConnectionConfig, ItemConduitTicker.Connection> {

    @Override
    protected void tickColoredGraph(ServerLevel level, ItemConduit conduit, List<Connection> senders, List<Connection> receivers, DyeColor color,
        ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider) {

        toNextExtract: for (Connection extract : receivers) {
            ItemConduitNodeData nodeData = extract.node().getOrCreateNodeData(ConduitTypes.NodeData.ITEM.get());

            IItemHandler extractHandler = extract.itemHandler();
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

                var connectionConfig = extract.node().getConnectionConfig(extract.side(), ConduitTypes.ConnectionTypes.ITEM.get());

                int startingIndex = 0;
                if (connectionConfig.isRoundRobin()) {
                    startingIndex = nodeData.getIndex(extract.side());
                    if (senders.size() <= startingIndex) {
                        startingIndex = 0;
                    }
                }

                for (int j = startingIndex; j < startingIndex + senders.size(); j++) {
                    int insertIndex = j % senders.size();
                    Connection insert = senders.get(insertIndex);

                    if (!connectionConfig.isSelfFeed() && extract.side() == insert.side()
                            && extract.node().getPos().equals(insert.node().getPos())) {
                        continue;
                    }

                    if (insert.insertFilter() instanceof ItemStackFilter itemFilter) {
                        if (!itemFilter.test(extractedItem)) {
                            continue;
                        }
                    }

                    ItemStack notInserted = ItemHandlerHelper.insertItem(insert.itemHandler, extractedItem, false);
                    int successfullyInserted = extractedItem.getCount() - notInserted.getCount();

                    if (successfullyInserted > 0) {
                        extracted += successfullyInserted;
                        extractHandler.extractItem(i, successfullyInserted, false);
                        if (extracted >= speed || isEmpty(extractHandler, i + 1)) {
                            if (connectionConfig.isRoundRobin()) {
                                nodeData.setIndex(extract.side(), insertIndex + 1);
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
    protected void preProcessReceivers(List<Connection> receivers) {
        receivers.sort(Comparator.comparingInt((Connection n) -> n.config().priority()).reversed());
    }

    @Override
    @Nullable
    protected Connection createConnection(Level level, ConduitNode node, Direction side) {
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, node.getPos().relative(side), side.getOpposite());
        if (itemHandler != null) {
            return new Connection(node, side, node.getConnectionConfig(side, ItemConduitConnectionConfig.TYPE), itemHandler);
        }

        return null;
    }

    protected static class Connection extends SimpleConnection<ItemConduitConnectionConfig> {
        private final IItemHandler itemHandler;

        public Connection(ConduitNode node, Direction side, ItemConduitConnectionConfig config, IItemHandler itemHandler) {
            super(node, side, config);
            this.itemHandler = itemHandler;
        }

        public IItemHandler itemHandler() {
            return itemHandler;
        }
    }
}
