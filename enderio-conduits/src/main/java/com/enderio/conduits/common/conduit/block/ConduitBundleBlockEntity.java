package com.enderio.conduits.common.conduit.block;

import com.enderio.base.api.UseOnly;
import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.conduits.ConduitNBTKeys;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitCapabilities;
import com.enderio.conduits.api.ConduitMenuData;
import com.enderio.conduits.api.ConduitNode;
import com.enderio.conduits.api.SlotType;
import com.enderio.conduits.api.upgrade.ConduitUpgrade;
import com.enderio.conduits.client.particle.ConduitBreakParticle;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.conduit.ConduitBundle;
import com.enderio.conduits.common.conduit.ConduitSavedData;
import com.enderio.conduits.common.conduit.ConduitShape;
import com.enderio.conduits.common.conduit.RightClickAction;
import com.enderio.conduits.common.conduit.SlotData;
import com.enderio.conduits.common.conduit.connection.ConnectionState;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.enderio.conduits.common.conduit.connection.StaticConnectionStates;
import com.enderio.conduits.common.conduit.graph.ConduitDataContainer;
import com.enderio.conduits.common.conduit.graph.ConduitGraphContext;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import com.enderio.conduits.common.conduit.graph.ConduitGraphUtility;
import com.enderio.conduits.common.init.ConduitBlockEntities;
import com.enderio.conduits.common.menu.ConduitMenu;
import com.enderio.core.common.blockentity.EnderBlockEntity;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import me.liliandev.ensure.ensures.EnsureSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConduitBundleBlockEntity extends EnderBlockEntity {

    public static final ModelProperty<ConduitBundle> BUNDLE_MODEL_PROPERTY = new ModelProperty<>();
    public static final ModelProperty<ModelData> FACADE_MODEL_DATA = new ModelProperty<>();
    public static final ModelProperty<ChunkRenderTypeSet> FACADE_RENDERTYPE = new ModelProperty<>();
    public static final String CONDUIT_INV_KEY = "ConduitInv";

    @UseOnly(LogicalSide.CLIENT)
    public static final Map<BlockPos, BlockState> FACADES = new HashMap<>();

    private final ConduitShape shape = new ConduitShape();

    private ConduitBundle bundle;
    @UseOnly(LogicalSide.CLIENT)
    private ConduitBundle clientBundle;

    private UpdateState checkConnection = UpdateState.NONE;

    private final Map<Holder<Conduit<?>>, ConduitGraphObject> lazyNodes = new HashMap<>();
    private ListTag lazyNodeNBT = new ListTag();
    private ConduitItemHandler conduitItemHandler = new ConduitItemHandler();

    public ConduitBundleBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ConduitBlockEntities.CONDUIT.get(), worldPosition, blockState);
        bundle = new ConduitBundle(this::scheduleTick, worldPosition);

        addDataSlot(ConduitBundle.DATA_SLOT_TYPE.create(this::getBundle, b -> bundle = b));
        addAfterSyncRunnable(this::updateClient);
    }

    public ConduitBundle getBundle() {
        return bundle;
    }

    public ConduitShape getShape() {
        return shape;
    }

    public void updateShape() {
        shape.updateConduit(bundle);
    }

    public void updateClient() {
        if (level != null && level.isClientSide) {
            clientBundle = bundle.deepCopy();
            updateShape();
            requestModelDataUpdate();
            level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());
            if (bundle.hasFacade()) {
                FACADES.put(worldPosition, bundle.facade().get().defaultBlockState());
            } else {
                FACADES.remove(worldPosition);
            }
        }
    }

    // region Network Sync

    /**
     * Handle a connection state update from the client.
     */
    @EnsureSide(EnsureSide.Side.SERVER)
    public void handleConnectionStateUpdate(Direction direction, Holder<Conduit<?>> conduit,
            DynamicConnectionState connectionState) {
        // Sanity check, the client shouldn't do this, but just to make sure there's no
        // confusion.
        if (bundle.getConnectionState(direction, conduit) instanceof DynamicConnectionState) {
            bundle.setConnectionState(direction, conduit, connectionState);

            // Update node IO state.
            var node = bundle.getNodeFor(conduit);
            node.pushState(direction, connectionState);

            // Proxied capabilities are likely to have changed.
            level.invalidateCapabilities(worldPosition);
        }

        updateClient();
        onConnectionsUpdated(conduit);
    }

    @EnsureSide(EnsureSide.Side.SERVER)
    public void handleConduitDataUpdate(Holder<Conduit<?>> conduit, ConduitDataContainer clientDataContainer) {
        var node = getBundle().getNodeFor(conduit);
        node.handleClientChanges(clientDataContainer);
    }

    // endregion

    private void scheduleTick() {
        setChanged();
    }

    @Override
    public void onLoad() {
        updateShape();

        if (level instanceof ServerLevel serverLevel) {
            sync();
            bundle.onLoad(level, getBlockPos());
            for (var entry : lazyNodes.entrySet()) {
                Holder<Conduit<?>> conduit = entry.getKey();
                ConduitGraphObject node = entry.getValue();
                loadNode(serverLevel, conduit, node);
            }
        }

        // Now that the BE is loaded, update the blocklight.
        if (bundle.hasFacade()) {
            level.getLightEngine().checkBlock(worldPosition);
        }
    }

    private void loadNode(ServerLevel serverLevel, Holder<Conduit<?>> conduit, ConduitGraphObject node) {

        Graph<ConduitGraphContext> graph = Objects.requireNonNull(node.getGraph());

        for (Direction dir : Direction.values()) {
            tryConnectTo(dir, conduit, false, false, false)
                    .ifPresent(otherNode -> ConduitGraphUtility.connect(conduit, node, otherNode));
        }

        for (GraphObject<?> object : node.getGraph().getObjects()) {
            if (object instanceof ConduitGraphObject otherNode) {
                conduit.value().onConnectTo(node, otherNode);
            }
        }

        ConduitSavedData.addPotentialGraph(conduit, graph, serverLevel);
    }

    public boolean stillValid(Player pPlayer) {
        if (level == null || level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }

        return pPlayer.canInteractWithBlock(this.worldPosition, 1.5);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level instanceof ServerLevel serverLevel) {
            ConduitSavedData savedData = ConduitSavedData.get(serverLevel);
            bundle.getConduits().forEach(type -> onChunkUnloaded(savedData, type));
        } else {
            FACADES.remove(worldPosition);
        }
    }

    private void onChunkUnloaded(ConduitSavedData savedData, Holder<Conduit<?>> conduit) {
        var node = bundle.getNodeFor(conduit);
        conduit.value().onRemoved(node, level, getBlockPos());
        savedData.putUnloadedNodeIdentifier(conduit, this.worldPosition, node);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide) {
            FACADES.remove(worldPosition);
        }
    }

    public void everyTick() {
        if (level == null) {
            return;
        }

        if (!level.isClientSide) {
            serverTick();
            checkConnection = checkConnection.next();
            if (checkConnection.isInitialized()) {
                updateConnections(level, worldPosition, null, false);
            }
        } else {
            clientTick();
        }
    }

    public void updateConnections(Level level, BlockPos pos, @Nullable BlockPos fromPos, boolean shouldActivate) {
        for (Direction direction : Direction.values()) {
            if (fromPos == null || !(level.getBlockEntity(fromPos) instanceof ConduitBundleBlockEntity)) {
                for (Holder<Conduit<?>> conduit : bundle.getConduits()) {
                    if (shouldActivate && conduit.value().hasConnectionDelay()) {
                        checkConnection = checkConnection.activate();
                        continue;
                    }

                    ConnectionState connectionState = bundle.getConnectionState(direction, conduit);
                    if (connectionState instanceof DynamicConnectionState dyn) {
                        if (!conduit.value().canForceConnectTo(level, pos, direction)) {
                            bundle.getNodeFor(conduit).clearState(direction);
                            dropConnectionItems(dyn);
                            bundle.setConnectionState(direction, conduit, StaticConnectionStates.DISCONNECTED);
                            updateShape();
                            onConnectionsUpdated(conduit);
                        }
                    } else if (connectionState == StaticConnectionStates.DISCONNECTED) {
                        tryConnectTo(direction, conduit, true, true, false);
                    }
                }
            }
        }

        updateShape();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.saveAdditional(tag, lookupProvider);
        tag.put(ConduitNBTKeys.CONDUIT_BUNDLE, bundle.save(lookupProvider));

        ListTag listTag = new ListTag();
        for (Holder<Conduit<?>> conduit : bundle.getConduits()) {
            var data = bundle.getNodeFor(conduit).conduitDataContainer();
            listTag.add(data.save(lookupProvider));
        }

        tag.put(ConduitNBTKeys.CONDUIT_EXTRA_DATA, listTag);
        tag.put(CONDUIT_INV_KEY, conduitItemHandler.serializeNBT(lookupProvider));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.loadAdditional(tag, lookupProvider);

        // loadAdditional is now called by the sync system
        // ideally we'll need presence checks to find bundle issues, but for now we're
        // making this safe.

        if (tag.contains(ConduitNBTKeys.CONDUIT_BUNDLE)) {
            bundle = ConduitBundle.parse(lookupProvider, tag.getCompound(ConduitNBTKeys.CONDUIT_BUNDLE));
            bundle.setOnChangedRunnable(this::scheduleTick);
        }

        if (tag.contains(ConduitNBTKeys.CONDUIT_EXTRA_DATA)) {
            lazyNodeNBT = tag.getList(ConduitNBTKeys.CONDUIT_EXTRA_DATA, Tag.TAG_COMPOUND);
        }

        if (tag.contains(CONDUIT_INV_KEY)) {
            conduitItemHandler.deserializeNBT(lookupProvider, tag.getCompound(CONDUIT_INV_KEY));
        }
    }

    @Override
    public void setLevel(Level pLevel) {
        super.setLevel(pLevel);

        if (level.isClientSide()) {
            clientBundle = bundle.deepCopy();
        } else {
            loadFromSavedData();
        }
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(BUNDLE_MODEL_PROPERTY, clientBundle).build();
    }

    public boolean hasType(Holder<Conduit<?>> conduit) {
        return bundle.hasType(conduit);
    }

    public RightClickAction addType(Holder<Conduit<?>> conduit, Player player) {
        RightClickAction action = bundle.addConduit(level, conduit, player);

        // something has changed
        if (action.hasChanged()) {
            List<GraphObject<ConduitGraphContext>> nodes = new ArrayList<>();
            for (Direction dir : Direction.values()) {
                tryConnectTo(dir, conduit, false, false, false).ifPresent(nodes::add);
            }

            if (level instanceof ServerLevel serverLevel) {
                ConduitGraphObject thisNode = Objects.requireNonNull(bundle.getNodeForTypeExact(conduit),
                        "no node found in conduit");
                ConduitGraphUtility.integrate(conduit, thisNode, nodes);

                for (GraphObject<ConduitGraphContext> object : thisNode.getGraph().getObjects()) {
                    if (object instanceof ConduitGraphObject node) {
                        conduit.value().onConnectTo(thisNode, node);
                    }
                }

                ConduitSavedData.addPotentialGraph(conduit, Objects.requireNonNull(thisNode.getGraph()), serverLevel);
            }

            if (action instanceof RightClickAction.Upgrade upgrade
                    && !upgrade.replacedConduit().value().canConnectTo(conduit)) {
                removeNeighborConnections(upgrade.replacedConduit());
            }

            // Update neighbors
            level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());

            updateShape();
        }

        return action;
    }

    public Optional<GraphObject<ConduitGraphContext>> tryConnectTo(Direction dir, Holder<Conduit<?>> conduit,
            boolean forceMerge, boolean shouldMergeGraph, boolean forceConnect) {
        if (level.getBlockEntity(getBlockPos().relative(dir)) instanceof ConduitBundleBlockEntity neighborBlockEntity
                && neighborBlockEntity.connectTo(dir.getOpposite(), conduit, bundle.getNodeFor(conduit), forceMerge)) {

            connect(dir, conduit);
            onConnectionsUpdated(conduit);
            neighborBlockEntity.onConnectionsUpdated(conduit);

            ConduitBundle adjacentBundle = neighborBlockEntity.getBundle();

            ConduitGraphObject firstNode = adjacentBundle.getNodeFor(conduit);
            ConduitGraphObject secondNode = bundle.getNodeFor(conduit);

            conduit.value().onConnectTo(firstNode, secondNode);

            if (firstNode.getParentGraph() != null) {
                for (var node : firstNode.getParentGraph().getNodes()) {
                    if (node != firstNode) {
                        conduit.value().onConnectTo(firstNode, node);
                    }
                }
            }

            if (secondNode.getParentGraph() != null && firstNode.getParentGraph() != secondNode.getParentGraph()) {
                for (var node : secondNode.getParentGraph().getNodes()) {
                    if (node != secondNode) {
                        conduit.value().onConnectTo(secondNode, node);
                    }
                }
            }

            if (shouldMergeGraph) {
                ConduitGraphUtility.connect(conduit, bundle.getNodeFor(conduit), adjacentBundle.getNodeFor(conduit));
            }

            return Optional.of(adjacentBundle.getNodeFor(conduit));
        } else if (conduit.value().canConnectTo(level, getBlockPos(), dir)
                || (forceConnect && conduit.value().canForceConnectTo(level, getBlockPos(), dir))) {
            if (bundle.getConnectionState(dir, conduit) instanceof DynamicConnectionState dyn && dyn.isConnection()) { // Already
                                                                                                                       // connected
                onConnectionsUpdated(conduit);
                return Optional.empty();
            }
            connectEnd(dir, conduit);
            onConnectionsUpdated(conduit);
        } else {
            this.disconnect(dir, conduit);
        }

        return Optional.empty();
    }

    public void onConnectionsUpdated(Holder<Conduit<?>> conduit) {
        if (level != null && !level.isClientSide) {
            var node = getBundle().getNodeFor(conduit);

            Set<Direction> connectedSides = Arrays.stream(Direction.values())
                    .filter(direction -> bundle.getConnectionState(direction,
                            conduit) != StaticConnectionStates.DISABLED)
                    .collect(Collectors.toSet());

            conduit.value().onConnectionsUpdated(node, level, getBlockPos(), connectedSides);
        }
    }

    /**
     * sets block to air if this is the last conduit
     */
    public void removeTypeAndDelete(Player player, Holder<Conduit<?>> conduit) {
        if (removeType(conduit, !player.getAbilities().instabuild)) {
            level.setBlock(getBlockPos(), getBlockState().getFluidState().createLegacyBlock(),
                    level.isClientSide ? Block.UPDATE_ALL_IMMEDIATE : Block.UPDATE_ALL);
        }
    }

    /**
     * Remove a conduit from the bundle.
     * @param conduit The conduit to remove.
     * @param shouldDrop Whether the conduit item should drop for this conduit.
     * @return Whether the block should now be completely removed.
     */
    public boolean removeType(Holder<Conduit<?>> conduit, boolean shouldDrop) {
        if (shouldDrop && !level.isClientSide()) {
            dropItem(ConduitBlockItem.getStackFor(conduit, 1));
            for (Direction dir : Direction.values()) {
                if (bundle.getConnectionState(dir, conduit) instanceof DynamicConnectionState dyn) {
                    dropConnectionItems(dyn);
                }
            }
        }

        boolean shouldRemove = bundle.removeConduit(level, conduit);
        removeNeighborConnections(conduit);
        updateShape();

        if (level.isClientSide) {
            ConduitBreakParticle.addDestroyEffects(getBlockPos(), conduit.value());
        }

        if (bundle.hasFacade() && shouldRemove) {
            dropFacadeItem();
        }

        return shouldRemove;
    }

    public void updateEmptyDynConnection() {
        for (Direction dir : Direction.values()) {
            for (int i = 0; i < ConduitBundle.MAX_CONDUITS; i++) {
                if (bundle.getConnectionState(dir, i) instanceof DynamicConnectionState dynState
                        && dynState.isEmpty()) {
                    dropConnectionItems(dynState);
                    bundle.disableConduit(dir, i);
                }
            }
        }
    }

    public void dropConnectionItems(DynamicConnectionState dyn) {
        for (SlotType slotType : SlotType.values()) {
            ItemStack item = dyn.getItem(slotType);
            if (!item.isEmpty()) {
                dropItem(item);
            }
        }
    }

    public void dropFacadeItem() {
        if (!bundle.hasFacade()) {
            throw new IllegalStateException("Cannot drop facade item because no facade has been set");
        }

        dropItem(bundle.facadeItem());
    }

    /**
     * Drop an item on the ground by this block.
     */
    private void dropItem(ItemStack stack) {
        level.addFreshEntity(
                new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), stack));
    }

    /**
     * Removes connections to neigbouring bundles to the given conduit.
     * @param conduit The conduit in this conduit that should be disconnected from other conduits.
     */
    public void removeNeighborConnections(Holder<Conduit<?>> conduit) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockEntity(
                    getBlockPos().relative(dir)) instanceof ConduitBundleBlockEntity neighborBlockEntity) {
                neighborBlockEntity.disconnect(dir.getOpposite(), conduit);
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            for (Direction dir : Direction.values()) {
                BlockEntity blockEntity = level.getBlockEntity(getBlockPos().relative(dir));
                if (blockEntity instanceof ConduitBundleBlockEntity neighborBlockEntity
                        && neighborBlockEntity.hasType(conduit)) {
                    Optional.of(neighborBlockEntity.bundle.getNodeFor(conduit))
                            .map(ConduitGraphObject::getGraph)
                            .filter(Objects::nonNull)
                            .ifPresent(graph -> ConduitSavedData.addPotentialGraph(conduit, graph, serverLevel));
                }
            }
        }
    }

    // region Serialization

    @UseOnly(LogicalSide.SERVER)
    private void loadFromSavedData() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ConduitSavedData savedData = ConduitSavedData.get(serverLevel);
        for (int i = 0; i < bundle.getConduits().size(); i++) {
            Holder<Conduit<?>> type = bundle.getConduits().get(i);
            loadConduitFromSavedData(savedData, type, i);
        }

        lazyNodeNBT.clear();
    }

    @UseOnly(LogicalSide.SERVER)
    private void loadConduitFromSavedData(ConduitSavedData savedData, Holder<Conduit<?>> conduit, int typeIndex) {
        if (level == null) {
            return;
        }

        ConduitGraphObject node = savedData.takeUnloadedNodeIdentifier(conduit, this.worldPosition);
        if (node == null && bundle.getNodeForTypeExact(conduit) == null) {
            ConduitDataContainer dataContainer = null;
//            if (typeIndex < lazyNodeNBT.size()) {
//                dataContainer = ConduitDataContainer.parse(level.registryAccess(), lazyNodeNBT.getCompound(typeIndex));
//            }

            node = new ConduitGraphObject(worldPosition, dataContainer);
            ConduitGraphUtility.integrate(conduit, node, List.of());
            bundle.setNodeFor(conduit, node);
            lazyNodes.put(conduit, node);
        } else if (node != null) {
            bundle.setNodeFor(conduit, node);
        }
    }

    // endregion

    /**
     * @param direction  the Direction to connect to
     * @param conduit    the conduit to be connected
     * @param node       the other node to check if those can connect
     * @param forceMerge if disabledstate should be ignored
     * @return true if a connection happens
     */
    private boolean connectTo(Direction direction, Holder<Conduit<?>> conduit, ConduitNode node, boolean forceMerge) {
        if (!doTypesMatch(conduit)) {
            return false;
        }

        if (!conduit.value().canConnectTo(bundle.getNodeFor(conduit), node)) {
            return false;
        }

        if (forceMerge || bundle.getConnectionState(direction, conduit) != StaticConnectionStates.DISABLED) {
            connect(direction, conduit);
            return true;
        }

        return false;
    }

    private boolean doTypesMatch(Holder<Conduit<?>> conduitToMatch) {
        for (Holder<Conduit<?>> conduit : bundle.getConduits()) {
            if (conduit.value().canConnectTo(conduitToMatch)) {
                return true;
            }
        }

        return false;
    }

    private void connect(Direction direction, Holder<Conduit<?>> conduit) {
        bundle.connectTo(level, worldPosition, direction, conduit, false);
        updateClient();
    }

    private void connectEnd(Direction direction, Holder<Conduit<?>> conduit) {
        bundle.connectTo(level, worldPosition, direction, conduit, true);
        updateClient();
    }

    private void disconnect(Direction direction, Holder<Conduit<?>> conduit) {
        if (bundle.disconnectFrom(direction, conduit)) {
            updateClient();
        }
    }

    public MenuProvider menuProvider(Direction direction, Holder<Conduit<?>> conduit) {
        return new ConduitMenuProvider(direction, conduit);
    }

    private class ConduitMenuProvider implements MenuProvider {

        private final Direction direction;
        private final Holder<Conduit<?>> conduit;

        private ConduitMenuProvider(Direction direction, Holder<Conduit<?>> conduit) {
            this.direction = direction;
            this.conduit = conduit;
        }

        @Override
        public Component getDisplayName() {
            return getBlockState().getBlock().getName();
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
            return new ConduitMenu(ConduitBundleBlockEntity.this, pInventory, pContainerId, direction, conduit);
        }
    }

    public static <TCap, TContext> ICapabilityProvider<ConduitBundleBlockEntity, TContext, TCap> createConduitCap(
            BlockCapability<TCap, TContext> cap) {
        return (be, context) -> {
            for (Holder<Conduit<?>> conduit : be.bundle.getConduits()) {
                var proxiedCap = getProxiedCapability(cap, be, conduit, context);
                if (proxiedCap != null) {
                    return proxiedCap;
                }
            }

            return null;
        };
    }

    @Nullable
    private static <TCap, TContext> TCap getProxiedCapability(BlockCapability<TCap, TContext> capability,
            ConduitBundleBlockEntity blockEntity, Holder<Conduit<?>> conduit, @Nullable TContext context) {

        if (blockEntity.level == null) {
            return null;
        }

        ConduitGraphObject node = blockEntity.bundle.getNodeFor(conduit);
        return conduit.value().proxyCapability(capability, node, blockEntity.level, blockEntity.getBlockPos(), context);
    }

    public IItemHandler getConduitItemHandler() {
        return conduitItemHandler;
    }

    private class ConduitItemHandler implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

        @Override
        public int getSlots() {
            return 3 * ConduitBundle.MAX_CONDUITS * 6;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot >= getSlots()) {
                return ItemStack.EMPTY;
            }

            SlotData data = SlotData.of(slot);
            if (data.conduitIndex() >= bundle.getConduits().size()) {
                return ItemStack.EMPTY;
            }

            ConnectionState connectionState = bundle.getConnectionState(data.direction(), data.conduitIndex());
            if (!(connectionState instanceof DynamicConnectionState dynamicConnectionState)) {
                return ItemStack.EMPTY;
            }

            Holder<Conduit<?>> conduit = bundle.getConduits().get(data.conduitIndex());
            ConduitMenuData conduitData = conduit.value().getMenuData();
            if ((data.slotType() == SlotType.FILTER_EXTRACT && conduitData.hasFilterExtract())
                    || (data.slotType() == SlotType.FILTER_INSERT && conduitData.hasFilterInsert())
                    || (data.slotType() == SlotType.UPGRADE_EXTRACT && conduitData.hasUpgrade())) {
                return dynamicConnectionState.getItem(data.slotType());
            }

            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            // see ItemStackHandler
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            if (!isItemValid(slot, stack)) {
                return stack;
            }

            ItemStack existing = getStackInSlot(slot);

            int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());

            if (!existing.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                    return stack;
                }

                limit -= existing.getCount();
            }

            if (limit <= 0) {
                return stack;
            }

            boolean reachedLimit = stack.getCount() > limit;

            if (!simulate) {
                if (existing.isEmpty()) {
                    setStackInSlot(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
                } else {
                    existing.grow(reachedLimit ? limit : stack.getCount());
                }
            }
            return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0) {
                return ItemStack.EMPTY;
            }

            ItemStack existing = getStackInSlot(slot);

            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int toExtract = Math.min(amount, existing.getMaxStackSize());

            if (existing.getCount() <= toExtract) {
                if (!simulate) {
                    setStackInSlot(slot, ItemStack.EMPTY);
                    return existing;
                } else {
                    return existing.copy();
                }
            } else {
                if (!simulate) {
                    setStackInSlot(slot, existing.copyWithCount(existing.getCount() - toExtract));
                }
                return existing.copyWithCount(toExtract);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot >= getSlots()) {
                return false;
            }

            SlotData slotData = SlotData.of(slot);
            if (slotData.conduitIndex() >= bundle.getConduits().size()) {
                return false;
            }

            Holder<Conduit<?>> conduit = bundle.getConduits().get(slotData.conduitIndex());

            switch (slotData.slotType()) {
            case FILTER_EXTRACT:
            case FILTER_INSERT:
                ResourceFilter resourceFilter = stack.getCapability(EIOCapabilities.Filter.ITEM);
                if (resourceFilter == null) {
                    return false;
                }

                return conduit.value().canApplyFilter(slotData.slotType(), resourceFilter);
            case UPGRADE_EXTRACT:
                ConduitUpgrade conduitUpgrade = stack.getCapability(ConduitCapabilities.CONDUIT_UPGRADE);
                if (conduitUpgrade == null) {
                    return false;
                }

                return conduit.value().canApplyUpgrade(slotData.slotType(), conduitUpgrade);
            default:
                return false;
            }
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (slot >= getSlots()) {
                return;
            }

            SlotData data = SlotData.of(slot);
            if (data.conduitIndex() >= bundle.getConduits().size()) {
                return;
            }

            Holder<Conduit<?>> conduit = bundle.getConduits().get(data.conduitIndex());
            ConduitMenuData menuData = conduit.value().getMenuData();

            if ((data.slotType() == SlotType.FILTER_EXTRACT && menuData.hasFilterExtract())
                    || (data.slotType() == SlotType.FILTER_INSERT && menuData.hasFilterInsert())
                    || (data.slotType() == SlotType.UPGRADE_EXTRACT && menuData.hasUpgrade())) {
                bundle.setConnectionItem(data.direction(), data.conduitIndex(), data.slotType(), stack);
                if (bundle.getConnectionState(data.direction(),
                        conduit) instanceof DynamicConnectionState dynamicConnectionState) {
                    ConduitGraphObject node = bundle.getNodeForTypeExact(conduit);
                    if (node != null) {
                        node.pushState(data.direction(), dynamicConnectionState);
                    }
                }
            }
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider lookupProvider) {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (int i = 0; i < getSlots(); i++) {
                ItemStack stack = getStackInSlot(i);
                list.add(i, stack.saveOptional(lookupProvider));
            }
            tag.put(CONDUIT_INV_KEY, list);
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt) {
            ListTag list = nbt.getList(CONDUIT_INV_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                setStackInSlot(i, ItemStack.parseOptional(lookupProvider, list.getCompound(i)));
            }
        }
    }

    public enum UpdateState {
        NONE, NEXT_NEXT, NEXT, INITIALIZED;

        public boolean isInitialized() {
            return this == INITIALIZED;
        }

        public UpdateState next() {
            return switch (this) {
            case NONE, INITIALIZED -> NONE;
            case NEXT_NEXT -> NEXT;
            case NEXT -> INITIALIZED;
            };
        }

        public UpdateState activate() {
            return NEXT_NEXT;
        }
    }
}
