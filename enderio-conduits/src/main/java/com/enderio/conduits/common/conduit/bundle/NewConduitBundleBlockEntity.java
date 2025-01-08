package com.enderio.conduits.common.conduit.bundle;

import com.enderio.conduits.ConduitNBTKeys;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitCapabilities;
import com.enderio.conduits.api.bundle.ConduitInventory;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.bundle.ConduitBundleAccessor;
import com.enderio.conduits.api.connection.ConduitConnectionType;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.facade.FacadeType;
import com.enderio.conduits.api.network.node.NodeData;
import com.enderio.conduits.client.model.conduit.bundle.ConduitBundleRenderState;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.conduit.ConduitSavedData;
import com.enderio.conduits.common.conduit.ConduitSorter;
import com.enderio.conduits.api.bundle.AddConduitResult;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlockEntity;
import com.enderio.conduits.common.conduit.connection.ConnectionState;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.enderio.conduits.common.conduit.connection.StaticConnectionStates;
import com.enderio.conduits.common.conduit.graph.ConduitConnectionHost;
import com.enderio.conduits.common.conduit.graph.ConduitDataContainer;
import com.enderio.conduits.common.conduit.graph.ConduitGraphContext;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import com.enderio.conduits.common.conduit.graph.ConduitGraphUtility;
import com.enderio.conduits.common.conduit.menu.NewConduitMenu;
import com.enderio.conduits.common.init.ConduitBlockEntities;
import com.enderio.core.common.blockentity.EnderBlockEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.graph3.Graph;
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

import me.liliandev.ensure.ensures.EnsureSide;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
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
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public final class NewConduitBundleBlockEntity extends EnderBlockEntity implements ConduitBundleAccessor, Clearable {

    public static final int MAX_CONDUITS = 9;

    private ItemStack facadeProvider = ItemStack.EMPTY;

    private List<Holder<Conduit<?>>> conduits = new ArrayList<>();

    private Map<Holder<Conduit<?>>, ConnectionContainer> conduitConnections = new HashMap<>();

    private final NewConduitBundleInventory inventory;

    // Map of all conduit nodes for this bundle.
    private final Map<Holder<Conduit<?>>, ConduitGraphObject> conduitNodes = new HashMap<>();

    // Used to recover missing nodes when loading the bundle.
    private final Map<Holder<Conduit<?>>, ConduitGraphObject> lazyNodes = new HashMap<>();
    private ListTag lazyNodeNBT = null;
    private Map<Holder<Conduit<?>>, NodeData> lazyNodeData = null;

    // The client has no nodes, so we hold the data like this.
    private final Map<Holder<Conduit<?>>, CompoundTag> clientConduitDataTags = new HashMap<>();

    private final NewConduitShape shape = new NewConduitShape();

    private boolean hasDirtyNodes = false;

    // Deferred connection check
    private ConduitBundleBlockEntity.UpdateState checkConnection = ConduitBundleBlockEntity.UpdateState.NONE;

    public NewConduitBundleBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ConduitBlockEntities.CONDUIT.get(), worldPosition, blockState);

        inventory = new NewConduitBundleInventory(this) {
            @Override
            protected void onChanged() {
                setChanged();
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

            if (this.level.getGameTime() % 5 == 0) {
                if (hasDirtyNodes) {
                    // This is for sending updates to clients when the nodes are dirty
                    // as such we only fire a block update
                    // TODO: We're also saving here, but maybe we shouldn't bother?
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
                    setChanged();
                    hasDirtyNodes = false;
                }
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

    @EnsureSide(EnsureSide.Side.CLIENT)
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

    public boolean canBeOrIsConnection(Direction side, Holder<Conduit<?>> conduit) {
        if (level == null) {
            return false;
        }

        // If we've lost the conduit
        if (!hasConduitStrict(conduit)) {
            return false;
        }

        // Cannot create a connection to a bundle
        if (level.getBlockEntity(getBlockPos().relative(side)) instanceof NewConduitBundleBlockEntity) {
            return false;
        }

        // If they've managed to open the menu, a connection could already have been established
        // TODO: Maybe create a map to track canConnect from the conduit so this is a guarantee.
        return true;
    }

    public MenuProvider getMenuProvider(Direction side, Holder<Conduit<?>> conduit) {
        return new ConduitMenuProvider(side, conduit);
    }

    private class ConduitMenuProvider implements MenuProvider {

        private final Direction side;
        private final Holder<Conduit<?>> conduit;

        private ConduitMenuProvider(Direction side, Holder<Conduit<?>> conduit) {
            this.side = side;
            this.conduit = conduit;
        }

        @Override
        public Component getDisplayName() {
            return conduit.value().description();
        }

        @Override
        public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return new NewConduitMenu(containerId, inventory, NewConduitBundleBlockEntity.this, side, conduit);
        }
    }

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
        if (node.getNetwork() == null) {
            return null;
        }

        return conduit.value().proxyCapability(capability, node, blockEntity.level, blockEntity.getBlockPos(), context);
    }

    // endregion

    // region Conduits

    public List<Holder<Conduit<?>>> getConduits() {
        return List.copyOf(conduits);
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
    public AddConduitResult addConduit(Holder<Conduit<?>> conduit, @Nullable Player player) {
        if (level == null || !(level instanceof ServerLevel serverLevel)) {
            return new AddConduitResult.Blocked();
        }

        if (isFull()) {
            return new AddConduitResult.Blocked();
        }

        if (hasConduitStrict(conduit)) {
            return new AddConduitResult.Blocked();
        }

        // Attempt to upgrade an existing conduit.
        AddConduitResult result;
        var replacementCandidate = findReplacementCandidate(conduit);
        if (replacementCandidate.isPresent()) {
            int replacementIndex = conduits.indexOf(replacementCandidate.get());
            conduits.set(replacementIndex, conduit);

            // Add connections entry
            var oldConnectionContainer = conduitConnections.remove(replacementCandidate.get());
            conduitConnections.put(conduit, oldConnectionContainer.copyFor(conduit)); // TODO: Remove incompatible connections!

            if (!level.isClientSide()) {
                ConduitGraphObject oldNode = conduitNodes.remove(replacementCandidate.get());

                ConduitGraphObject newNode;
                if (oldNode != null) {
                    // Copy data into the node
                    newNode = new ConduitGraphObject(getBlockPos(), oldNode.getNodeData());
                    conduit.value().onRemoved(oldNode, level, getBlockPos());
                    oldNode.getGraph().remove(oldNode);
                } else {
                    newNode = new ConduitGraphObject(getBlockPos());
                }

                setNode(conduit, newNode);
                conduit.value().onCreated(newNode, level, getBlockPos(), player);
            }

            result = new AddConduitResult.Upgrade(replacementCandidate.get());
        } else {
            // Ensure there are no incompatible conduits.
            if (!isConduitCompatibleWithExisting(conduit)) {
                return new AddConduitResult.Blocked();
            }

            // Ensure the conduits list is sorted correctly.
            int id = ConduitSorter.getSortIndex(conduit);
            var addBefore = conduits.stream().filter(c -> ConduitSorter.getSortIndex(c) > id).findFirst();
            if (addBefore.isPresent()) {
                conduits.add(conduits.indexOf(addBefore.get()), conduit);
            } else {
                conduits.add(conduit);
            }

            // Add connections entry
            conduitConnections.put(conduit, new ConnectionContainer(conduit));

            if (!level.isClientSide()) {
                // Create the new node
                ConduitGraphObject node = new ConduitGraphObject(getBlockPos());

                // Add the node
                setNode(conduit, node);

                // NeoForge contains a patch that calls onLoad after the conduit has been placed
                // if it's the first one, so onCreated would be called twice. it's easier to
                // detect here
                if (conduits.size() != 1) {
                    conduit.value().onCreated(node, level, getBlockPos(), player);
                }
            }

            result = new AddConduitResult.Insert();
        }

        if (!level.isClientSide()) {
            // Attach the new node to its own graph
            var node = getConduitNode(conduit);
            ConduitGraphUtility.integrate(conduit, node, List.of());

            // Attach the bundle
            node.attach(new ConnectionHost(this, conduit));
        }

        // Now attempt to make connections.
        for (Direction side : Direction.values()) {
            tryConnectTo(side, conduit, false);
        }

        if (!level.isClientSide()) {
            ConduitSavedData.addPotentialGraph(conduit, Objects.requireNonNull(getConduitNode(conduit).getGraph()), serverLevel);
        }

        if (result instanceof AddConduitResult.Upgrade upgrade
                && !upgrade.replacedConduit().value().canConnectTo(conduit)) {
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

        // Node remove event
        if (!level.isClientSide()) {
            var node = getConduitNode(conduit);
            conduit.value().onRemoved(node, level, getBlockPos());
            node.detach();

            // Remove from the graph.
            if (node.getGraph() != null) {
                node.getGraph().remove(node);
            }
        }

        // Remove from the inventory's storage.
        inventory.removeConduit(conduit);

        // Remove from the bundle
        conduits.remove(conduit);
        conduitConnections.remove(conduit);
        conduitNodes.remove(conduit);

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
    public ConduitInventory getInventory(Holder<Conduit<?>> conduit) {
        if (!hasConduitStrict(conduit)) {
            throw new IllegalStateException("Conduit not found in bundle.");
        }

        return inventory.getInventoryFor(conduit);
    }

    @EnsureSide(EnsureSide.Side.SERVER)
    public ConduitGraphObject getConduitNode(Holder<Conduit<?>> conduit) {
        if (!hasConduitByType(conduit)) {
            throw new IllegalStateException("Conduit not found in bundle.");
        }

        return conduitNodes.get(conduit);
    }

    @Override
    @Nullable
    public CompoundTag getConduitClientDataTag(Holder<Conduit<?>> conduit) {
        if (!conduit.value().hasClientDataTag()) {
            return null;
        }

        if (level != null && !level.isClientSide()) {
            return conduit.value().getClientDataTag(getConduitNode(conduit));
        }

        return clientConduitDataTags.get(conduit);
    }

    @EnsureSide(EnsureSide.Side.SERVER)
    private void setNode(Holder<Conduit<?>> conduit, ConduitGraphObject loadedNode) {
        conduitNodes.put(conduit, loadedNode);

        // Attach to the node to provide connection data and inventory.
        loadedNode.attach(new ConnectionHost(this, conduit));
    }

    // endregion

    // region Connections

    @Override
    public List<Holder<Conduit<?>>> getConnectedConduits(Direction side) {
        return conduitConnections.entrySet()
                .stream()
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
    public ConnectionConfig getConnectionConfig(Direction side, Holder<Conduit<?>> conduit) {
        return conduitConnections.get(conduit).getConfig(side);
    }

    @Override
    public void setConnectionConfig(Direction side, Holder<Conduit<?>> conduit, ConnectionConfig config) {
        if (config.type() != conduit.value().connectionConfigType()) {
            throw new IllegalArgumentException("Connection config is not the right type for this conduit.");
        }

        conduitConnections.get(conduit).setConfig(side, config);
        bundleChanged();
    }

    // Intended for use by the menu, might need a better interface?
    @EnsureSide(EnsureSide.Side.SERVER)
    public void setConnectionType(Direction side, Holder<Conduit<?>> conduit, ConduitConnectionType type) {
        if (!hasConduitStrict(conduit)) {
            throw new IllegalArgumentException("Conduit is not present in this bundle.");
        }

        conduitConnections.get(conduit).setType(side, type);
        bundleChanged();
    }

    @Override
    public boolean isEndpoint(Direction side) {
        return conduitConnections.values().stream().anyMatch(c -> c.hasEndpoint(side));
    }

    // TODO: This needs a better name or to handle blocks as well as conduits before
    // it can be exposed via the interface.
    public boolean canConnectTo(Direction side, Holder<Conduit<?>> conduit, ConduitGraphObject otherNode,
            boolean isForcedConnection) {
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

        // Don't attempt a connection if we already have one, or we're disabled (and not
        // forcing a connection)
        ConduitConnectionType currentConnectionType = conduitConnections.get(conduit).getType(side);
        if (currentConnectionType.isConnected()
                || (!isForcedConnection && currentConnectionType == ConduitConnectionType.DISABLED)) {
            return false;
        }

        var node = conduitNodes.get(conduit);

        if (level.getBlockEntity(
                getBlockPos().relative(side)) instanceof NewConduitBundleBlockEntity neighbourConduitBundle) {
            // Connect to another bundle which has a compatible conduit.
            if (neighbourConduitBundle.canConnectTo(side.getOpposite(), conduit, node, isForcedConnection)) {
                // Make connections to both sides
                connectConduit(side, conduit);
                neighbourConduitBundle.connectConduit(side.getOpposite(), conduit);

                // Fire node connection events
                if (!level.isClientSide()) {
                    var neighbourNode = neighbourConduitBundle.getConduitNode(conduit);
                    conduit.value().onConnectTo(node, neighbourNode);
                    conduit.value().onConnectTo(neighbourNode, node);

                    // Connect the graphs together
                    ConduitGraphUtility.connect(conduit, node, neighbourNode);
                }
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
        conduitConnections.computeIfAbsent(conduit, ConnectionContainer::new)
            .setType(side, ConduitConnectionType.CONNECTED_CONDUIT);
        onConnectionsUpdated(conduit);
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        updateShape();
    }

    private void connectBlock(Direction side, Holder<Conduit<?>> conduit) {
        conduitConnections.computeIfAbsent(conduit, ConnectionContainer::new)
            .setType(side, ConduitConnectionType.CONNECTED_BLOCK);
        onConnectionsUpdated(conduit);
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        updateShape();
    }

    // TODO: poorly named, we're disconnecting from another conduit on the given
    // side.
    private void disconnect(Direction side, Holder<Conduit<?>> conduit) {
        boolean hasChanged = false;
        for (var c : conduits) {
            if (c.value().canConnectTo(conduit)) {
                conduitConnections.computeIfAbsent(c, ConnectionContainer::new)
                    .setType(side, ConduitConnectionType.NONE);
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
        return !facadeProvider.isEmpty()
                && facadeProvider.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER) != null;
    }

    @Override
    public Block getFacadeBlock() {
        if (facadeProvider.isEmpty()) {
            throw new IllegalStateException("This bundle has no facade provider.");
        }

        var provider = facadeProvider.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER);
        if (provider == null) {
            // TODO: How to handle this error gracefully?
            // For now default to a bedrock facade.
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

        updateShape();
        updateModel();
    }

    @Override
    public void handleUpdateTag(CompoundTag syncData, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(syncData, lookupProvider);

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

    @EnsureSide(EnsureSide.Side.SERVER)
    private void loadFromSavedData() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ConduitSavedData savedData = ConduitSavedData.get(serverLevel);
        for (int i = 0; i < conduits.size(); i++) {
            Holder<Conduit<?>> type = conduits.get(i);
            loadConduitFromSavedData(savedData, type, i);
        }

        lazyNodeData = null;
        lazyNodeNBT = null;
    }

    @EnsureSide(EnsureSide.Side.SERVER)
    private void loadConduitFromSavedData(ConduitSavedData savedData, Holder<Conduit<?>> conduit, int typeIndex) {
        if (level == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ConduitGraphObject node = savedData.takeUnloadedNodeIdentifier(conduit, this.worldPosition);
        if (node == null && conduitNodes.get(conduit) == null) {
            // Attempt to recover node data
            NodeData nodeData = null;
            if (lazyNodeData != null && lazyNodeData.containsKey(conduit)) {
                nodeData = lazyNodeData.remove(conduit);
            }

            if (nodeData == null) {
                // Attempt to load legacy recovery data.
                ConduitDataContainer dataContainer = null;
                if (lazyNodeNBT != null && typeIndex < lazyNodeNBT.size()) {
                    dataContainer = ConduitDataContainer.parse(level.registryAccess(), lazyNodeNBT.getCompound(typeIndex));
                }

                if (dataContainer != null) {
                    node = new ConduitGraphObject(getBlockPos(), dataContainer);
                } else {
                    node = new ConduitGraphObject(getBlockPos());
                }
            } else {
                node = new ConduitGraphObject(getBlockPos(), nodeData);
            }

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
                node.detach();
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
    public static final String NODE_DATA_KEY = "NodeData";

    private static final String CONDUIT_CLIENT_SYNC_DATA_KEY = "ConduitSyncTags";

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag updateTag = super.getUpdateTag(registries);

        // Send conduit sync data
        ListTag nodeDataList = new ListTag();

        for (var conduit : conduits) {
            if (conduit.value().hasClientDataTag()) {
                var node = getConduitNode(conduit);
                CompoundTag tag = new CompoundTag();
                tag.put("Conduit", Conduit.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), conduit).getOrThrow());
                tag.put("Data", conduit.value().getClientDataTag(node));
                nodeDataList.add(tag);
            }
        }

        updateTag.put(CONDUIT_CLIENT_SYNC_DATA_KEY, nodeDataList);
        return updateTag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(CONDUIT_INV_KEY, inventory.serializeNBT(registries));

        var serializationContext = registries.createSerializationContext(NbtOps.INSTANCE);

        // NEW: Save node data in case of need for recovery
        ListTag nodeData = new ListTag();
        for (Holder<Conduit<?>> conduit : conduits) {
            var data = conduitNodes.get(conduit).getNodeData();

            if (data != null) {
                CompoundTag nodeTag = new CompoundTag();
                nodeTag.put("Conduit", Conduit.CODEC.encodeStart(serializationContext, conduit).getOrThrow());
                nodeTag.put("Data", NodeData.GENERIC_CODEC.encodeStart(serializationContext, data).getOrThrow());
                nodeData.add(nodeTag);
            }
        }
        tag.put(NODE_DATA_KEY, nodeData);
    }

    @Override
    protected void saveAdditionalSynced(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditionalSynced(tag, registries);

        if (!conduits.isEmpty()) {
            ListTag conduitList = new ListTag();
            for (var conduit : conduits) {
                conduitList.add(
                        Conduit.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), conduit)
                                .getOrThrow());
            }
            tag.put(CONDUITS_KEY, conduitList);

            // Save connections
            ListTag conduitConnectionsList = new ListTag();
            for (var conduit : conduits) {
                ListTag connectionsList = new ListTag();
                for (Direction side : Direction.values()) {
                    CompoundTag connectionTag = new CompoundTag();
                    connectionTag.putString("Side", side.getSerializedName());
                    connectionTag.putString("Type", getConnectionType(side, conduit).getSerializedName());

                    var config = getConnectionConfig(side, conduit);
                    if (!config.equals(config.type().getDefault())) {
                        connectionTag.put("Config", ConnectionConfig.GENERIC_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), config).getOrThrow());
                    }

                    connectionsList.add(connectionTag);
                }

                conduitConnectionsList.add(connectionsList);
            }
            tag.put(CONNECTIONS_KEY, conduitConnectionsList);
        }

        if (!facadeProvider.isEmpty()) {
            tag.put(FACADE_PROVIDER_KEY, facadeProvider.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains(ConduitNBTKeys.CONDUIT_BUNDLE)) {
            // Convert the legacy bundle to the new format
            var bundle = LegacyConduitBundle.parse(registries, tag.getCompound(ConduitNBTKeys.CONDUIT_BUNDLE));
            loadFromLegacyBundle(bundle);
        } else {
            // New save format
            conduits.clear();
            if (tag.contains(CONDUITS_KEY, Tag.TAG_LIST)) {
                // Get untyped list tag.
                ListTag conduitList = (ListTag)tag.get(CONDUITS_KEY);
                for (var conduitTag : conduitList) {
                    conduits.add(Conduit.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), conduitTag)
                            .getOrThrow());
                }
            }

            // Load connections
            conduitConnections.clear();
            if (tag.contains(CONNECTIONS_KEY)) {
                ListTag conduitConnectionsList = tag.getList(CONNECTIONS_KEY, Tag.TAG_LIST);

                for (int i = 0; i < conduitConnectionsList.size(); i++) {
                    ListTag connectionsList = conduitConnectionsList.getList(i);
                    Holder<Conduit<?>> conduit = conduits.get(i);

                    ConnectionContainer connections = new ConnectionContainer(conduit);
                    for (int j = 0; j < connectionsList.size(); j++) {
                        CompoundTag connectionTag = connectionsList.getCompound(j);
                        Direction side = Direction.byName(connectionTag.getString("Side"));
                        ConduitConnectionType type = ConduitConnectionType.byName(connectionTag.getString("Type"));

                        if (side != null && type != null) {
                            connections.setType(side, type);

                            if (connectionTag.contains("Config")) {
                                ConnectionConfig config = ConnectionConfig.GENERIC_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), connectionTag.get("Config")).getOrThrow();
                                connections.setConfig(side, config);
                            }
                        }
                    }

                    conduitConnections.put(conduit, connections);
                }
            }

            if (tag.contains(FACADE_PROVIDER_KEY)) {
                facadeProvider = ItemStack.parseOptional(registries, tag.getCompound(FACADE_PROVIDER_KEY));
            }
        }

        // Load inventory
        if (tag.contains(CONDUIT_INV_KEY)) {
            inventory.deserializeNBT(registries, tag.getCompound(CONDUIT_INV_KEY));
        }

        // Load node data used for recovery
        if (tag.contains(ConduitNBTKeys.CONDUIT_EXTRA_DATA)) {
            lazyNodeNBT = tag.getList(ConduitNBTKeys.CONDUIT_EXTRA_DATA, Tag.TAG_COMPOUND);
        } else if (tag.contains(NODE_DATA_KEY)) {
            var list = tag.getList(NODE_DATA_KEY, Tag.TAG_COMPOUND);
            lazyNodeData = new HashMap<>();

            var serializationContext = registries.createSerializationContext(NbtOps.INSTANCE);

            for (int i = 0; i < list.size(); i++) {
                var nodeTag = list.getCompound(i);
                var conduitParseResult = Conduit.CODEC.parse(serializationContext, nodeTag.get("Conduit"));

                if (conduitParseResult.isError()) {
                    continue;
                }

                var dataParseResult = NodeData.GENERIC_CODEC.parse(serializationContext, nodeTag.get("Data"));
                if (dataParseResult.isError()) {
                    continue;
                }

                lazyNodeData.put(conduitParseResult.getOrThrow(), dataParseResult.getOrThrow());
            }
        }

        // Load synced node data
        if (tag.contains(CONDUIT_CLIENT_SYNC_DATA_KEY)) {
            clientConduitDataTags.clear();

            ListTag nodeDataList = tag.getList(CONDUIT_CLIENT_SYNC_DATA_KEY, Tag.TAG_COMPOUND);
            var serializationContext = registries.createSerializationContext(NbtOps.INSTANCE);
            for (int i = 0; i < nodeDataList.size(); i++) {
                CompoundTag nodeTag = nodeDataList.getCompound(i);
                var conduit = Conduit.CODEC.parse(serializationContext, nodeTag.get("Conduit")).getOrThrow();
                clientConduitDataTags.put(conduit, nodeTag.getCompound("Data"));
            }
        }
    }

    @SuppressWarnings("removal")
    private void loadFromLegacyBundle(LegacyConduitBundle bundle) {
        // Copy the conduit list
        conduits = new ArrayList<>();
        conduits.addAll(bundle.conduits);

        // Copy facade provider
        facadeProvider = bundle.facadeItem.copy();

        // Copy legacy connections into the new bundle
        conduitConnections = new HashMap<>();
        for (var conduit : conduits) {
            int conduitIndex = conduits.indexOf(conduit);
            var connections = conduitConnections.computeIfAbsent(conduit, ConnectionContainer::new);

            for (Direction side : Direction.values()) {
                var legacySide = bundle.connections.get(side);

                var state = legacySide.getConnectionState(conduitIndex);

                if (state == StaticConnectionStates.CONNECTED || state == StaticConnectionStates.CONNECTED_ACTIVE) {
                    connections.setType(side, ConduitConnectionType.CONNECTED_CONDUIT);
                } else if (state == StaticConnectionStates.DISCONNECTED) {
                    connections.setType(side, ConduitConnectionType.NONE);
                } else if (state == StaticConnectionStates.DISABLED) {
                    connections.setType(side, ConduitConnectionType.DISABLED);
                } else if (state instanceof DynamicConnectionState dynamicState) {
                    connections.setType(side, ConduitConnectionType.CONNECTED_BLOCK);

                    connections.setConfig(side, conduit.value().convertConnection(dynamicState.isInsert(), dynamicState.isExtract(),
                        dynamicState.insertChannel(), dynamicState.extractChannel(), dynamicState.control(),
                        dynamicState.redstoneChannel()));

                    inventory.setStackInSlot(conduit, side, SlotType.FILTER_INSERT, dynamicState.filterInsert());
                    inventory.setStackInSlot(conduit, side, SlotType.FILTER_EXTRACT, dynamicState.filterExtract());
                }
            }
        }
    }

    @Override
    public void clearContent() {
        // Remove all conduits and facades, this is normally called by /set
        for (var conduit : getConduits()) {
            removeConduit(conduit, null);
        }

        clearFacade();
    }

    // endregion

    private class ConnectionContainer {
        private final Holder<Conduit<?>> conduit;
        private final Map<Direction, ConduitConnectionType> connectionTypes = new EnumMap<>(Direction.class);
        private final Map<Direction, ConnectionConfig> connectionConfigs = new EnumMap<>(Direction.class);

        public ConnectionContainer(Holder<Conduit<?>> conduit) {
            this.conduit = conduit;
            for (Direction dir : Direction.values()) {
                connectionTypes.put(dir, ConduitConnectionType.NONE);
            }
        }

        public ConnectionContainer copyFor(Holder<Conduit<?>> conduit) {
            var copy = new ConnectionContainer(conduit);
            copy.connectionTypes.putAll(connectionTypes);

            // Only copy connection config if compatible.
            if (this.conduit.value().connectionConfigType() == conduit.value().connectionConfigType()) {
                copy.connectionConfigs.putAll(connectionConfigs);
            }
            return copy;
        }

        public ConduitConnectionType getType(Direction side) {
            return connectionTypes.getOrDefault(side, ConduitConnectionType.NONE);
        }

        public void setType(Direction side, ConduitConnectionType type) {
            connectionTypes.put(side, type);

            if (type == ConduitConnectionType.CONNECTED_BLOCK) {
                if (connectionConfigs.containsKey(side)) {
                    var config = connectionConfigs.get(side);
                    if (!config.isConnected()) {
                        connectionConfigs.put(side, config.reconnected());
                    }
                }
            }
        }

        public ConnectionConfig getConfig(Direction side) {
            var defaultConfig = conduit.value().connectionConfigType().getDefault();
            var config = connectionConfigs.getOrDefault(side, defaultConfig);

            // Ensure the connection type is correct.
            // If it isn't, revert to the default.
            if (config.type() != conduit.value().connectionConfigType()) {
                config = conduit.value().connectionConfigType().getDefault();
                connectionConfigs.put(side, config);
                bundleChanged(); // TODO: is this right?
            }

            return config;
        }

        public void setConfig(Direction side, ConnectionConfig config) {
            connectionConfigs.put(side, config);
        }

        public boolean hasEndpoint(Direction side) {
            return getType(side) == ConduitConnectionType.CONNECTED_BLOCK;
        }
    }

    private record ConnectionHost(NewConduitBundleBlockEntity conduitBundle, Holder<Conduit<?>> conduit) implements ConduitConnectionHost {

        @Override
        public BlockPos pos() {
            return conduitBundle.getBlockPos();
        }

        @Override
        public boolean isConnectedTo(Direction side) {
            return conduitBundle.getConnectionType(side, conduit) == ConduitConnectionType.CONNECTED_BLOCK;
        }

        @Override
        public ConnectionConfig getConnectionConfig(Direction side) {
            return conduitBundle.getConnectionConfig(side, conduit);
        }

        @Override
        public void setConnectionConfig(Direction side, ConnectionConfig connectionConfig) {
            throw new NotImplementedException();
        }

        @Override
        public ConduitInventory inventory() {
            return conduitBundle.getInventory(conduit);
        }

        @Override
        public void onNodeDirty() {
            conduitBundle.hasDirtyNodes = true;
        }

        @Override
        public boolean isLoaded() {
            return conduitBundle.level != null && conduitBundle.level.isLoaded(pos()) && conduitBundle.level.shouldTickBlocksAt(pos());
        }
    }

    // Matches the same data format as the original conduit bundle.
    // Enables us to convert between the new and old formats easily.
    @SuppressWarnings("removal")
    private record LegacyConduitBundle(BlockPos pos, List<Holder<Conduit<?>>> conduits,
            Map<Direction, ConduitConnection> connections, ItemStack facadeItem,
            Map<Holder<Conduit<?>>, ConduitGraphObject> conduitNodes) {

        public static Codec<LegacyConduitBundle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(i -> i.pos),
                Conduit.CODEC.listOf().fieldOf("conduits").forGetter(i -> i.conduits),
                Codec.unboundedMap(Direction.CODEC, ConduitConnection.CODEC)
                        .fieldOf("connections")
                        .forGetter(i -> i.connections),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("facade", ItemStack.EMPTY).forGetter(i -> i.facadeItem),
                Codec.unboundedMap(Conduit.CODEC, ConduitGraphObject.CODEC)
                        .fieldOf("nodes")
                        .forGetter(i -> i.conduitNodes))
                .apply(instance, LegacyConduitBundle::new));

        public static LegacyConduitBundle parse(HolderLookup.Provider lookupProvider, Tag tag) {
            return CODEC.decode(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
                    .getOrThrow()
                    .getFirst();
        }

        public static final class ConduitConnection {

            public static Codec<ConduitConnection> CODEC = ConnectionState.CODEC.listOf(0, MAX_CONDUITS)
                    .xmap(ConduitConnection::new, i -> Arrays.stream(i.connectionStates).toList());

            private final ConnectionState[] connectionStates = Util.make(() -> {
                var states = new ConnectionState[MAX_CONDUITS];
                Arrays.fill(states, StaticConnectionStates.DISCONNECTED);
                return states;
            });

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
        }
    }
}
