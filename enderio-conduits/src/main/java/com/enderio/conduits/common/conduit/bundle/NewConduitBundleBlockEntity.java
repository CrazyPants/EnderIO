package com.enderio.conduits.common.conduit.bundle;

import com.enderio.base.api.UseOnly;
import com.enderio.conduits.ConduitNBTKeys;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitCapabilities;
import com.enderio.conduits.api.SlotType;
import com.enderio.conduits.api.bundle.ConduitBundleAccessor;
import com.enderio.conduits.api.facade.FacadeType;
import com.enderio.conduits.client.model.rewrite.conduit.bundle.ConduitBundleRenderState;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.conduit.ConduitSavedData;
import com.enderio.conduits.common.conduit.ConduitSorter;
import com.enderio.conduits.common.conduit.RightClickAction;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlockEntity;
import com.enderio.conduits.common.conduit.connection.ConnectionState;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.enderio.conduits.common.conduit.connection.StaticConnectionStates;
import com.enderio.conduits.common.conduit.graph.ConduitDataContainer;
import com.enderio.conduits.common.conduit.graph.ConduitGraphContext;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import com.enderio.conduits.common.conduit.graph.ConduitGraphUtility;
import com.enderio.conduits.api.connection.ConduitConnectionMode;
import com.enderio.conduits.api.connection.ConduitConnectionType;
import com.enderio.conduits.api.connection.ConduitConnection;
import com.enderio.conduits.common.init.ConduitBlockEntities;
import com.enderio.core.common.blockentity.EnderBlockEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.graph3.Graph;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class NewConduitBundleBlockEntity extends EnderBlockEntity implements ConduitBundleAccessor {

    // TODO: onConnectionsUpdated needs to be fired.
    // TODO: need to drop items when connections are removed etc.
    // TODO: model is not making junction boxes correctly...

    public static final int MAX_CONDUITS = 9;

    // TODO: The new save format is actually not finished yet.
    private static final boolean USE_LEGACY_SAVE_FORMAT = true;

    private ItemStack facadeProvider = ItemStack.EMPTY;

    private List<Holder<Conduit<?>>> conduits = new ArrayList<>();

    private Map<Holder<Conduit<?>>, ConnectionContainer> conduitConnections = new HashMap<>();

    private final NewConduitBundleInventory inventory;

    // Map of all conduit nodes for this bundle.
    private Map<Holder<Conduit<?>>, ConduitGraphObject> conduitNodes = new HashMap<>();

    // Used to recover missing nodes when loading the bundle.
    private final Map<Holder<Conduit<?>>, ConduitGraphObject> lazyNodes = new HashMap<>();
    private ListTag lazyNodeNBT = new ListTag();

    private final NewConduitShape shape = new NewConduitShape();

    // Deferred connection check
    private ConduitBundleBlockEntity.UpdateState checkConnection = ConduitBundleBlockEntity.UpdateState.NONE;

    public NewConduitBundleBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ConduitBlockEntities.CONDUIT.get(), worldPosition, blockState);

        inventory = new NewConduitBundleInventory(this) {
            @Override
            protected void onChanged() {
                setChanged();
                // TODO: Do we need to do anything else here?
            }
        };
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (level != null) {
            checkConnection = checkConnection.next();
            if (checkConnection.isInitialized()) {
                updateConnections(level, getBlockPos(), null, false);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        updateShape();

        if (level instanceof ServerLevel serverLevel) {
            // Fire on-created events
            for (var conduit : conduits) {
                conduit.value().onCreated(conduitNodes.get(conduit), level, getBlockPos(), null);
            }

            // Attempt to make connections for recovered nodes.
            for (var entry : lazyNodes.entrySet()) {
                Holder<Conduit<?>> conduit = entry.getKey();
                ConduitGraphObject node = entry.getValue();

                Graph<ConduitGraphContext> graph = Objects.requireNonNull(node.getGraph());

                for (Direction dir : Direction.values()) {
                    tryConnectTo(dir, conduit, false);
                }

                ConduitSavedData.addPotentialGraph(conduit, graph, serverLevel);
            }
        }

        // Update lighting engine now that the bundle is loaded
        if (level != null && hasFacade()) {
            level.getLightEngine().checkBlock(getBlockPos());
        }
    }

    /**
     * Fire all relevant updates when the conduits or connections change.
     */
    private void bundleChanged() {
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        level.invalidateCapabilities(getBlockPos());
        setChanged();
        updateShape();

        if (level.isClientSide()) {
            updateModel();
        }
    }

    // region Shape and Model

    public NewConduitShape getShape() {
        return shape;
    }

    public void updateShape() {
        shape.updateConduit(this);
    }

    @UseOnly(LogicalSide.CLIENT)
    public void updateModel() {
        requestModelDataUpdate();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(ConduitBundleRenderState.PROPERTY, ConduitBundleRenderState.of(this)).build();
    }

    // endregion

    // region Menu

    // TODO

    // endregion

    // region Capability Proxies

    public static <TCap, TContext> ICapabilityProvider<NewConduitBundleBlockEntity, TContext, TCap> createCapabilityProvider(
        BlockCapability<TCap, TContext> cap) {
        return (be, context) -> {
            for (Holder<Conduit<?>> conduit : be.getConduits()) {
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
        NewConduitBundleBlockEntity blockEntity, Holder<Conduit<?>> conduit, @Nullable TContext context) {

        if (blockEntity.level == null) {
            return null;
        }

        ConduitGraphObject node = blockEntity.conduitNodes.get(conduit);
        return conduit.value().proxyCapability(capability, node, blockEntity.level, blockEntity.getBlockPos(), context);
    }

    // endregion

    // region Conduits

    public List<Holder<Conduit<?>>> getConduits() {
        return conduits;
    }

    @Override
    public boolean hasConduitByType(Holder<Conduit<?>> conduit) {
        return conduits.stream().anyMatch(c -> c.value().canConnectTo(conduit));
    }

    @Override
    public boolean hasConduitStrict(Holder<Conduit<?>> conduit) {
        return conduits.contains(conduit);
    }

    @Override
    public boolean isEmpty() {
        return conduits.isEmpty() && !hasFacade();
    }

    @Override
    public boolean isFull() {
        return conduits.size() == MAX_CONDUITS;
    }

    /**
     * Finds a conduit which is replaceable by the given conduit.
     * @param possibleReplacement the conduit that may replace another.
     * @return the conduit that can be replaced, or empty if none can be replaced.
     */
    private Optional<Holder<Conduit<?>>> findReplacementCandidate(Holder<Conduit<?>> possibleReplacement) {
        return conduits.stream()
            .filter(existingConduit -> existingConduit.value().canBeReplacedBy(possibleReplacement))
            .findFirst();
    }

    /**
     * @param conduit the conduit to check for.
     * @return whether the provided conduit is compatible with the other conduits in the bundle.
     */
    private boolean isConduitCompatibleWithExisting(Holder<Conduit<?>> conduit) {
        return conduits.stream().allMatch(existingConduit -> existingConduit.value().canBeInSameBundle(conduit));
    }

    @Override
    public boolean canAddConduit(Holder<Conduit<?>> conduit) {
        if (level == null) {
            return false;
        }

        if (isFull()) {
            return false;
        }

        if (hasConduitStrict(conduit)) {
            return false;
        }

        if (findReplacementCandidate(conduit).isPresent()) {
            return true;
        }

        return isConduitCompatibleWithExisting(conduit);
    }

    @Override
    public RightClickAction addConduit(Holder<Conduit<?>> conduit, @Nullable Player player) {
        if (level == null || !(level instanceof ServerLevel serverLevel)) {
            return new RightClickAction.Blocked();
        }

        if (isFull()) {
            return new RightClickAction.Blocked();
        }

        if (hasConduitStrict(conduit)) {
            return new RightClickAction.Blocked();
        }

        // Attempt to upgrade an existing conduit.
        RightClickAction result;
        var replacementCandidate = findReplacementCandidate(conduit);
        if (replacementCandidate.isPresent()) {
            int replacementIndex = conduits.indexOf(replacementCandidate.get());
            conduits.set(replacementIndex, conduit);

            ConduitGraphObject oldNode = conduitNodes.remove(replacementCandidate.get());

            ConduitGraphObject newNode;
            if (oldNode != null) {
                // Copy data into the node
                newNode = new ConduitGraphObject(getBlockPos(), oldNode.conduitDataContainer());
                conduit.value().onRemoved(oldNode, level, getBlockPos());
                oldNode.getGraph().remove(oldNode);
            } else {
                newNode = new ConduitGraphObject(getBlockPos());
            }

            setNode(conduit, newNode);
            conduit.value().onCreated(newNode, level, getBlockPos(), player);

            result = new RightClickAction.Upgrade(replacementCandidate.get());
        } else {
            // Ensure there are no incompatible conduits.
            if (!isConduitCompatibleWithExisting(conduit)) {
                return new RightClickAction.Blocked();
            }

            // Create the new node
            ConduitGraphObject node = new ConduitGraphObject(getBlockPos());

            // Ensure the conduits list is sorted correctly.
            int id = ConduitSorter.getSortIndex(conduit);
            var addBefore = conduits.stream().filter(c -> ConduitSorter.getSortIndex(c) > id).findFirst();
            if (addBefore.isPresent()) {
                conduits.add(conduits.indexOf(addBefore.get()), conduit);
            } else {
                conduits.add(conduit);
            }

            // Add the node
            setNode(conduit, node);

            // Add connections entry
            conduitConnections.put(conduit, new ConnectionContainer(conduit));

            // NeoForge contains a patch that calls onLoad after the conduit has been placed
            // if it's the first one, so onCreated would be called twice. it's easier to
            // detect here
            if (conduits.size() != 1) {
                conduit.value().onCreated(node, level, getBlockPos(), player);
            }

            result = new RightClickAction.Insert();
        }

        // Attach the new node to its own graph
        ConduitGraphUtility.integrate(conduit, getConduitNode(conduit), List.of());

        // Now attempt to make connections.
        for (Direction side : Direction.values()) {
            tryConnectTo(side, conduit, false);
        }

        ConduitSavedData.addPotentialGraph(conduit, Objects.requireNonNull(getConduitNode(conduit).getGraph()), serverLevel);

        if (result instanceof RightClickAction.Upgrade upgrade &&
            !upgrade.replacedConduit().value().canConnectTo(conduit)) {
            removeNeighborConnections(conduit);
        }

        bundleChanged();
        return result;
    }

    @Override
    public void removeConduit(Holder<Conduit<?>> conduit, @Nullable Player player) {
        if (level == null) {
            return;
        }

        if (!hasConduitStrict(conduit)) {
            if (!FMLLoader.isProduction()) {
                throw new IllegalArgumentException(
                    "Conduit: " + conduit.getRegisteredName() + " is not present in conduit bundle "
                        + Arrays.toString(conduits.stream().map(Holder::getRegisteredName).toArray()));
            }

            return;
        }

        // Drop the conduit and it's inventory items.
        if (!level.isClientSide()) {
            if (player != null && !player.getAbilities().instabuild) {
                dropItem(ConduitBlockItem.getStackFor(conduit, 1));
                for (Direction side : Direction.values()) {
                    dropConnectionItems(side, conduit);
                }
            }
        }

        // Remove neighbour connections
        removeNeighborConnections(conduit);

        // Remove from the inventory's storage.
        inventory.removeConduit(conduit);

        // Node remove event
        var node = conduitNodes.remove(conduit);
        conduit.value().onRemoved(node, level, getBlockPos());

        // Remove from the graph.
        if (node.getGraph() != null) {
            node.getGraph().remove(node);
        }

        // Remove from the bundle
        conduits.remove(conduit);
        conduitConnections.remove(conduit);

        bundleChanged();
    }

    /**
     * Removes connections to neigbouring bundles to the given conduit.
     * @param conduit The conduit in this conduit that should be disconnected from other conduits.
     */
    public void removeNeighborConnections(Holder<Conduit<?>> conduit) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockEntity(
                getBlockPos().relative(dir)) instanceof NewConduitBundleBlockEntity neighborBlockEntity) {
                neighborBlockEntity.disconnect(dir.getOpposite(), conduit);
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            for (Direction dir : Direction.values()) {
                BlockEntity blockEntity = level.getBlockEntity(getBlockPos().relative(dir));
                if (blockEntity instanceof NewConduitBundleBlockEntity neighborBlockEntity
                    && neighborBlockEntity.hasConduitByType(conduit)) {
                    Optional.of(neighborBlockEntity.getConduitNode(conduit))
                        .map(ConduitGraphObject::getGraph)
                        .filter(Objects::nonNull)
                        .ifPresent(graph -> ConduitSavedData.addPotentialGraph(conduit, graph, serverLevel));
                }
            }
        }
    }

    private void dropItem(ItemStack stack) {
        if (level != null) {
            level.addFreshEntity(
                new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), stack));
        }
    }

    @Override
    public ConduitGraphObject getConduitNode(Holder<Conduit<?>> conduit) {
        return conduitNodes.get(conduit);
    }

    private void setNode(Holder<Conduit<?>> conduit, ConduitGraphObject loadedNode) {
        conduitNodes.put(conduit, loadedNode);

        // Give the node a reference to its inventory.
        loadedNode.setInventory(inventory.getInventoryFor(conduit));

        // Push the current connections through to the node.
        var connections = conduitConnections.computeIfAbsent(conduit, ConnectionContainer::new);
        for (Direction side : Direction.values()) {
            if (connections.getType(side) == ConduitConnectionType.CONNECTED_BLOCK) {
                loadedNode.setConnection(side, connections.getConnection(side));
            } else {
                loadedNode.setConnection(side, null);
            }
        }
    }

    // endregion

    // region Connections

    @Override
    public List<Holder<Conduit<?>>> getConnectedConduits(Direction side) {
        return conduitConnections.entrySet().stream()
            .filter(e -> e.getValue().getType(side).isConnected())
            .map(Map.Entry::getKey)
            .sorted(Comparator.comparingInt(ConduitSorter::getSortIndex))
            .toList();
    }

    @Override
    public ConduitConnectionType getConnectionType(Direction side, Holder<Conduit<?>> conduit) {
        return conduitConnections.computeIfAbsent(conduit, ConnectionContainer::new).getType(side);
    }

    @Override
    public ConduitConnection getConnection(Direction side, Holder<Conduit<?>> conduit) {
        var connections = conduitConnections.computeIfAbsent(conduit, ConnectionContainer::new);
        if (connections.getType(side) != ConduitConnectionType.CONNECTED_BLOCK) {
            throw new IllegalStateException("Conduit is not connected to a block on this side.");
        }

        return conduitConnections.get(conduit).getConnection(side);
    }

    @Override
    public boolean isEndpoint(Direction side) {
        return conduitConnections.values().stream().anyMatch(c -> c.hasEndpoint(side));
    }

    // TODO: This needs a better name or to handle blocks as well as conduits before it can be exposed via the interface.
    public boolean canConnectTo(Direction side, Holder<Conduit<?>> conduit, ConduitGraphObject otherNode, boolean isForcedConnection) {
        if (level == null) {
            return false;
        }

        if (!doTypesMatch(conduit)) {
            return false;
        }

        if (!conduit.value().canConnectTo(conduitNodes.get(conduit), otherNode)) {
            return false;
        }

        return isForcedConnection || conduitConnections.get(conduit).getType(side) != ConduitConnectionType.DISABLED;
    }

    private boolean doTypesMatch(Holder<Conduit<?>> conduitToMatch) {
        for (Holder<Conduit<?>> conduit : conduits) {
            if (conduit.value().canConnectTo(conduitToMatch)) {
                return true;
            }
        }

        return false;
    }

    public boolean tryConnectTo(Direction side, Holder<Conduit<?>> conduit, boolean isForcedConnection) {
        if (level == null) {
            return false;
        }

        if (!hasConduitStrict(conduit)) {
            throw new IllegalArgumentException("Conduit is not present in this bundle.");
        }

        // Don't attempt a connection if we already have one, or we're disabled (and not forcing a connection)
        ConduitConnectionType currentConnectionType = conduitConnections.get(conduit).getType(side);
        if (currentConnectionType.isConnected() || (!isForcedConnection && currentConnectionType == ConduitConnectionType.DISABLED)) {
            return false;
        }

        var node = conduitNodes.get(conduit);

        if (level.getBlockEntity(getBlockPos().relative(side)) instanceof NewConduitBundleBlockEntity neighbourConduitBundle) {
            // Connect to another bundle which has a compatible conduit.
            if (neighbourConduitBundle.canConnectTo(side.getOpposite(), conduit, node, isForcedConnection)) {
                // Make connections to both sides
                connectConduit(side, conduit);
                neighbourConduitBundle.connectConduit(side.getOpposite(), conduit);

                // Fire node connection events
                var neighbourNode = neighbourConduitBundle.getConduitNode(conduit);
                conduit.value().onConnectTo(node, neighbourNode);
                conduit.value().onConnectTo(neighbourNode, node);

                // Connect the graphs together
                ConduitGraphUtility.connect(conduit, node, neighbourNode);
                return true;
            }

            return false;
        } else if (conduit.value().canConnectTo(level, getBlockPos(), side)
            || (isForcedConnection && conduit.value().canForceConnectTo(level, getBlockPos(), side))) {
            connectBlock(side, conduit);
            return true;
        }

        return false;
    }

    public void onConnectionsUpdated(Holder<Conduit<?>> conduit) {
        if (level != null && !level.isClientSide) {
            var node = getConduitNode(conduit);

            Set<Direction> connectedSides = Arrays.stream(Direction.values())
                .filter(direction -> getConnectionType(direction, conduit).isConnected())
                .collect(Collectors.toSet());

            conduit.value().onConnectionsUpdated(node, level, getBlockPos(), connectedSides);
        }
    }

    private void connectConduit(Direction side, Holder<Conduit<?>> conduit) {
        conduitConnections.computeIfAbsent(conduit, ConnectionContainer::new).connectConduit(side);
        onConnectionsUpdated(conduit);
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        updateShape();
    }

    private void connectBlock(Direction side, Holder<Conduit<?>> conduit) {
        conduitConnections.computeIfAbsent(conduit, ConnectionContainer::new).connectBlock(side);
        onConnectionsUpdated(conduit);
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        updateShape();
    }

    // TODO: poorly named, we're disconnecting from another conduit on the given side.
    private void disconnect(Direction side, Holder<Conduit<?>> conduit) {
        // TODO: Old disconnect does a lot more work than this... idk why it cycles through all conduits with canConnectTo

        boolean hasChanged = false;
        for (var c : conduits) {
            if (c.value().canConnectTo(conduit)) {
                conduitConnections.computeIfAbsent(c, ConnectionContainer::new).disconnect(side);
                onConnectionsUpdated(c);
                hasChanged = true;
            }
        }

        if (hasChanged) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            updateShape();
        }
    }

    private void dropConnectionItems(Direction side, Holder<Conduit<?>> conduit) {
        for (SlotType slotType : SlotType.values()) {
            ItemStack stack = inventory.getStackInSlot(conduit, side, slotType);
            if (!stack.isEmpty()) {
                dropItem(stack);
                inventory.setStackInSlot(conduit, side, slotType, ItemStack.EMPTY);
            }
        }
    }

    // TODO: I've not properly reviewed this method.
    public void updateConnections(Level level, BlockPos pos, @Nullable BlockPos fromPos, boolean shouldActivate) {
        if (fromPos != null && level.getBlockEntity(fromPos) instanceof NewConduitBundleBlockEntity) {
            return;
        }

        for (Direction side : Direction.values()) {
            for (var conduit : conduits) {
                if (shouldActivate && conduit.value().hasConnectionDelay()) {
                    checkConnection = checkConnection.activate();
                    continue;
                }

                var currentConnectionType = getConnectionType(side, conduit);

                if (currentConnectionType == ConduitConnectionType.NONE) {
                    tryConnectTo(side, conduit, false);
                } else if (currentConnectionType == ConduitConnectionType.CONNECTED_BLOCK) {
                    if (!conduit.value().canForceConnectTo(level, getBlockPos(), side)) {
                        dropConnectionItems(side, conduit);
                        disconnect(side, conduit);
                        onConnectionsUpdated(conduit);
                    }
                }
            }
        }
    }

    // endregion

    // region Facades

    @Override
    public boolean hasFacade() {
        return !facadeProvider.isEmpty() && facadeProvider.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER) != null;
    }

    @Override
    public Block getFacadeBlock() {
        if (facadeProvider.isEmpty()) {
            throw new IllegalStateException("This bundle has no facade provider.");
        }

        var provider = facadeProvider.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER);
        if (provider == null) {
            // TODO: How to handle this error gracefully?
            //       For now default to a bedrock facade.
            return Blocks.BEDROCK;
        }

        return provider.block();
    }

    @Override
    public FacadeType getFacadeType() {
        if (facadeProvider.isEmpty()) {
            throw new IllegalStateException("This bundle has no facade provider.");
        }

        var provider = facadeProvider.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER);
        if (provider == null) {
            return FacadeType.BASIC;
        }

        return provider.type();
    }

    @Override
    public ItemStack getFacadeProvider() {
        return facadeProvider;
    }

    @Override
    public void setFacadeProvider(ItemStack facadeProvider) {
        this.facadeProvider = facadeProvider;
        bundleChanged();
    }

    @Override
    public void clearFacade() {
        this.facadeProvider = ItemStack.EMPTY;
        bundleChanged();
    }

    public void dropFacadeItem() {
        dropItem(facadeProvider);
    }

    // endregion

    // region Network Sync

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);

        // Update shape and model after receiving an update from the server.
        updateShape();
        updateModel();
    }

    @Override
    public void handleUpdateTag(CompoundTag syncData, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(syncData, lookupProvider);

        // Update shape and model after receiving an update from the server.
        updateShape();
        updateModel();
    }

    // endregion

    // region Serialization

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (!level.isClientSide()) {
            loadFromSavedData();
        }
    }

    @UseOnly(LogicalSide.SERVER)
    private void loadFromSavedData() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ConduitSavedData savedData = ConduitSavedData.get(serverLevel);
        for (int i = 0; i < conduits.size(); i++) {
            Holder<Conduit<?>> type = conduits.get(i);
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
        if (node == null && conduitNodes.get(conduit) == null) {
            ConduitDataContainer dataContainer = null;
            if (typeIndex < lazyNodeNBT.size()) {
                dataContainer = ConduitDataContainer.parse(level.registryAccess(), lazyNodeNBT.getCompound(typeIndex));
            }

            node = new ConduitGraphObject(worldPosition, dataContainer);
            ConduitGraphUtility.integrate(conduit, node, List.of());
            setNode(conduit, node);
            lazyNodes.put(conduit, node);
        } else if (node != null) {
            setNode(conduit, node);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        if (level instanceof ServerLevel serverLevel) {
            var savedData = ConduitSavedData.get(serverLevel);

            for (var conduit : conduits) {
                var node = conduitNodes.get(conduit);
                conduit.value().onRemoved(node, level, getBlockPos());
                savedData.putUnloadedNodeIdentifier(conduit, this.worldPosition, node);
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        // TODO: Remove from facade map.
    }

    private static final String FACADE_PROVIDER_KEY = "FacadeProvider";
    private static final String CONDUITS_KEY = "Conduits";
    private static final String CONNECTIONS_KEY = "Connections";
    private static final String NODES_KEY = "Nodes";
    public static final String CONDUIT_INV_KEY = "ConduitInv";

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(CONDUIT_INV_KEY, inventory.serializeNBT(registries));

        if (!USE_LEGACY_SAVE_FORMAT) {
            // Save conduit nodes as another recovery option
            // TODO: If we save the node, why do we need the additional CONDUIT_EXTRA_DATA tags...
            // ... the same nodes contain the data being saved.
            // Done in conduit list order.
            ListTag listTag = new ListTag();
            for (Holder<Conduit<?>> conduit : conduits) {
                var data = conduitNodes.get(conduit);
                listTag.add(data.save(registries));
            }
            tag.put(NODES_KEY, listTag);
        }

        // Save node data in case of need for recovery
        // Done in conduit list order.
        ListTag listTag = new ListTag();
        for (Holder<Conduit<?>> conduit : conduits) {
            var data = conduitNodes.get(conduit).conduitDataContainer();
            listTag.add(data.save(registries));
        }

        tag.put(ConduitNBTKeys.CONDUIT_EXTRA_DATA, listTag);
    }

    @Override
    protected void saveAdditionalSynced(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditionalSynced(tag, registries);

        // TODO: Only save here if we're using the new save format, if we're using old, add the new format in getUpdateTag?
        //       it'll mean much less data is sent.
        if (USE_LEGACY_SAVE_FORMAT) {
            var bundle = createLegacyBundle();
            tag.put(ConduitNBTKeys.CONDUIT_BUNDLE, bundle.save(registries));
        } else {
            if (!conduits.isEmpty()) {
                ListTag conduitList = new ListTag();
                for (var conduit : conduits) {
                    conduitList.add(Conduit.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), conduit).getOrThrow());
                }
                tag.put(CONDUITS_KEY, conduitList);
            }

            // TODO: Save connections

            if (!facadeProvider.isEmpty()) {
                tag.put(FACADE_PROVIDER_KEY, facadeProvider.save(registries));
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains(ConduitNBTKeys.CONDUIT_BUNDLE)) {
            var bundle = LegacyConduitBundle.parse(registries, tag.getCompound(ConduitNBTKeys.CONDUIT_BUNDLE));
            loadFromLegacyBundle(bundle);
        } else {
            // New save format
            if (tag.contains(CONDUITS_KEY)) {
                ListTag conduitList = tag.getList(CONDUITS_KEY, 10);
                for (var conduitTag : conduitList) {
                    conduits.add(Conduit.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), conduitTag).getOrThrow());
                }
            }

            // TODO: Load connections

            if (tag.contains(FACADE_PROVIDER_KEY)) {
                facadeProvider = ItemStack.parseOptional(registries, tag.getCompound(FACADE_PROVIDER_KEY));
            }

            if (tag.contains(NODES_KEY)) {
                ListTag listTag = tag.getList(NODES_KEY, Tag.TAG_COMPOUND);
                for (int i = 0; i < listTag.size(); i++) {
                    var data = ConduitGraphObject.parse(registries, listTag.getCompound(i));
                    setNode(conduits.get(i), data);
                }
            }
        }

        // Load inventory
        if (tag.contains(CONDUIT_INV_KEY)) {
            inventory.deserializeNBT(registries, tag.getCompound(CONDUIT_INV_KEY));
        }

        // Load node data used for recovery
        if (tag.contains(ConduitNBTKeys.CONDUIT_EXTRA_DATA)) {
            lazyNodeNBT = tag.getList(ConduitNBTKeys.CONDUIT_EXTRA_DATA, Tag.TAG_COMPOUND);
        }
    }

    @SuppressWarnings("removal")
    private void loadFromLegacyBundle(LegacyConduitBundle bundle) {
        // Copy the conduit list
        conduits = new ArrayList<>();
        conduits.addAll(bundle.conduits);

        // Copy conduit nodes across
        conduitNodes = new HashMap<>();
        for (var entry : bundle.conduitNodes.entrySet()) {
            setNode(entry.getKey(), entry.getValue());
        }

        // Copy legacy connections into the new bundle
        conduitConnections = new HashMap<>();
        for (var conduit : conduits) {
            int conduitIndex = conduits.indexOf(conduit);
            var connections = conduitConnections.computeIfAbsent(conduit, ConnectionContainer::new);

            for (Direction side : Direction.values()) {
                var legacySide = bundle.connections.get(side);

                var state = legacySide.getConnectionState(conduitIndex);

                if (state == StaticConnectionStates.CONNECTED || state == StaticConnectionStates.CONNECTED_ACTIVE) {
                    connections.internalSetType(side, ConduitConnectionType.CONNECTED_CONDUIT);
                } else if (state == StaticConnectionStates.DISCONNECTED) {
                    connections.internalSetType(side, ConduitConnectionType.NONE);
                } else if (state == StaticConnectionStates.DISABLED) {
                    connections.internalSetType(side, ConduitConnectionType.DISABLED);
                } else if (state instanceof DynamicConnectionState dynamicState) {
                    connections.internalSetType(side, ConduitConnectionType.CONNECTED_BLOCK);

                    ConduitConnectionMode mode;
                    if (dynamicState.isInsert() && dynamicState.isExtract()) {
                        mode = ConduitConnectionMode.BOTH;
                    } else if (dynamicState.isInsert()) {
                        mode = ConduitConnectionMode.IN;
                    } else {
                        mode = ConduitConnectionMode.OUT;
                    }

                    connections.setConnection(side, new ConduitConnection(
                        mode,
                        dynamicState.insertChannel(),
                        dynamicState.extractChannel(),
                        dynamicState.control(),
                        dynamicState.redstoneChannel()
                    ));

                    inventory.setStackInSlot(conduit, side, SlotType.FILTER_INSERT, dynamicState.filterInsert());
                    inventory.setStackInSlot(conduit, side, SlotType.FILTER_EXTRACT, dynamicState.filterExtract());
                    inventory.setStackInSlot(conduit, side, SlotType.UPGRADE_EXTRACT, dynamicState.upgradeExtract());
                }
            }
        }
    }

    @SuppressWarnings("removal")
    private LegacyConduitBundle createLegacyBundle() {
        Map<Direction, LegacyConduitBundle.ConduitConnection> legacyConnectionsMap = new EnumMap<>(Direction.class);
        for (var conduit : conduits) {
            int conduitIndex = conduits.indexOf(conduit);
            var connections = conduitConnections.get(conduit);

            for (Direction side : Direction.values()) {
                var legacySide = legacyConnectionsMap.computeIfAbsent(side, ignored -> new LegacyConduitBundle.ConduitConnection());

                var type = connections.getType(side);
                if (type == ConduitConnectionType.DISABLED) {
                    legacySide.setConnectionState(conduitIndex, StaticConnectionStates.DISABLED);
                } else if (type == ConduitConnectionType.NONE) {
                    legacySide.setConnectionState(conduitIndex, StaticConnectionStates.DISCONNECTED);
                } else if (type == ConduitConnectionType.CONNECTED_CONDUIT) {
                    legacySide.setConnectionState(conduitIndex, StaticConnectionStates.CONNECTED);
                } else if (type == ConduitConnectionType.CONNECTED_BLOCK) {
                    var connection = connections.getConnection(side);

                    var legacyConnection = new DynamicConnectionState(
                        connection.canInput(),
                        connection.inputChannel(),
                        connection.canOutput(),
                        connection.outputChannel(),
                        connection.redstoneControl(),
                        connection.redstoneChannel(),
                        inventory.getStackInSlot(conduit, side, SlotType.FILTER_INSERT),
                        inventory.getStackInSlot(conduit, side, SlotType.FILTER_EXTRACT),
                        inventory.getStackInSlot(conduit, side, SlotType.UPGRADE_EXTRACT)
                    );

                    legacySide.setConnectionState(conduitIndex, legacyConnection);
                }
            }
        }

        return new LegacyConduitBundle(
            getBlockPos(),
            conduits,
            legacyConnectionsMap,
            facadeProvider,
            conduitNodes
        );
    }

    // endregion

    private class ConnectionContainer {
        private final Holder<Conduit<?>> conduit;
        private final Map<Direction, ConduitConnectionType> connectionTypes = new EnumMap<>(Direction.class);
        private final Map<Direction, ConduitConnection> connectionData = new EnumMap<>(Direction.class);

        public ConnectionContainer(Holder<Conduit<?>> conduit) {
            this.conduit = conduit;
            for (Direction dir : Direction.values()) {
                connectionTypes.put(dir, ConduitConnectionType.NONE);
            }
        }

        public ConduitConnectionType getType(Direction side) {
            return connectionTypes.getOrDefault(side, ConduitConnectionType.NONE);
        }

        /**
         * @deprecated Used for legacy data loading only.
         */
        @Deprecated
        public void internalSetType(Direction side, ConduitConnectionType type) {
            connectionTypes.put(side, type);
        }

        private void connectConduit(Direction side) {
            connectionTypes.put(side, ConduitConnectionType.CONNECTED_CONDUIT);
            connectionData.remove(side);
            conduitNodes.get(conduit).setConnection(side, null);
        }

        private void connectBlock(Direction side) {
            connectionTypes.put(side, ConduitConnectionType.CONNECTED_BLOCK);
            var defaultConnection = conduit.value().getDefaultConnection(level, getBlockPos(), side);
            setConnection(side, defaultConnection);
        }

        private void disconnect(Direction side) {
            connectionTypes.put(side, ConduitConnectionType.NONE);
            connectionData.remove(side);
            conduitNodes.get(conduit).setConnection(side, null);
        }

        public ConduitConnection getConnection(Direction side) {
            return connectionData.get(side);
        }

        public void setConnection(Direction side, ConduitConnection connection) {
            connectionData.put(side, connection);
            conduitNodes.get(conduit).setConnection(side, connection);
        }

        public boolean hasEndpoint(Direction side) {
            return getType(side) == ConduitConnectionType.CONNECTED_BLOCK;
        }
    }

    // Matches the same data format as the original conduit bundle.
    // Enables us to convert between the new and old formats easily.
    @SuppressWarnings("removal")
    private record LegacyConduitBundle(
        BlockPos pos,
        List<Holder<Conduit<?>>> conduits,
        Map<Direction, ConduitConnection> connections,
        ItemStack facadeItem,
        Map<Holder<Conduit<?>>, ConduitGraphObject> conduitNodes
    ) {
        public static Codec<LegacyConduitBundle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(i -> i.pos),
                Conduit.CODEC.listOf().fieldOf("conduits").forGetter(i -> i.conduits),
                Codec.unboundedMap(Direction.CODEC, ConduitConnection.CODEC)
                    .fieldOf("connections")
                    .forGetter(i -> i.connections),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("facade", ItemStack.EMPTY).forGetter(i -> i.facadeItem),
                Codec.unboundedMap(Conduit.CODEC, ConduitGraphObject.CODEC).fieldOf("nodes").forGetter(i -> i.conduitNodes))
            .apply(instance, LegacyConduitBundle::new));

        public Tag save(HolderLookup.Provider lookupProvider) {
            return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static LegacyConduitBundle parse(HolderLookup.Provider lookupProvider, Tag tag) {
            return CODEC.decode(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow().getFirst();
        }

        public static final class ConduitConnection {

            public static Codec<ConduitConnection> CODEC = ConnectionState.CODEC.listOf(0, MAX_CONDUITS)
                .xmap(ConduitConnection::new, i -> Arrays.stream(i.connectionStates).toList());

            private final ConnectionState[] connectionStates = Util.make(() -> {
                var states = new ConnectionState[MAX_CONDUITS];
                Arrays.fill(states, StaticConnectionStates.DISCONNECTED);
                return states;
            });

            public ConduitConnection() {
            }

            private ConduitConnection(List<ConnectionState> connectionStates) {
                if (connectionStates.size() > MAX_CONDUITS) {
                    throw new IllegalArgumentException(
                        "Cannot store more than " + MAX_CONDUITS + " conduit types per bundle.");
                }

                for (var i = 0; i < connectionStates.size(); i++) {
                    this.connectionStates[i] = connectionStates.get(i);
                }
            }

            public ConnectionState getConnectionState(int index) {
                return connectionStates[index];
            }

            public void setConnectionState(int i, ConnectionState state) {
                connectionStates[i] = state;
            }
        }
    }
}
