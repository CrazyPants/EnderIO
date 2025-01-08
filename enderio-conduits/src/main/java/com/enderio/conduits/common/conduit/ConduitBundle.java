package com.enderio.conduits.common.conduit;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitCapabilities;
import com.enderio.conduits.api.bundle.AddConduitResult;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.facade.FacadeType;
import com.enderio.conduits.common.conduit.connection.ConnectionState;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.enderio.conduits.common.conduit.connection.StaticConnectionStates;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import com.enderio.core.common.network.NetworkDataSlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import me.liliandev.ensure.ensures.EnsureSide;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.Nullable;

public final class ConduitBundle {

    // Do not change this value unless you fix the OffsetHelper
    public static final int MAX_CONDUITS = 9;

    public static Codec<ConduitBundle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(i -> i.pos),
            Conduit.CODEC.listOf().fieldOf("conduits").forGetter(i -> i.conduits),
            Codec.unboundedMap(Direction.CODEC, ConduitConnection.CODEC)
                    .fieldOf("connections")
                    .forGetter(i -> i.connections),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("facade", ItemStack.EMPTY).forGetter(i -> i.facadeItem),
            Codec.unboundedMap(Conduit.CODEC, ConduitGraphObject.CODEC).fieldOf("nodes").forGetter(i -> i.conduitNodes))
            .apply(instance, ConduitBundle::new));

    public static StreamCodec<RegistryFriendlyByteBuf, ConduitBundle> STREAM_CODEC = null;

    public static NetworkDataSlot.CodecType<ConduitBundle> DATA_SLOT_TYPE = new NetworkDataSlot.CodecType<>(CODEC,
            STREAM_CODEC);

    private final Map<Direction, ConduitConnection> connections = new EnumMap<>(Direction.class);
    private final List<Holder<Conduit<?>>> conduits = new ArrayList<>();

    // fill back after world save
    private final Map<Holder<Conduit<?>>, ConduitGraphObject> conduitNodes = new HashMap<>();
    private final BlockPos pos;

    private ItemStack facadeItem = ItemStack.EMPTY;

    @Nullable
    private Runnable onChangedRunnable;

    public ConduitBundle(Runnable onChanged, BlockPos pos) {
        this.onChangedRunnable = onChanged;
        for (Direction value : Direction.values()) {
            connections.put(value, new ConduitConnection());
        }
        this.pos = pos;
    }

    // TODO: If we enable conversion from new to old, this will become public.
    private ConduitBundle(BlockPos pos, List<Holder<Conduit<?>>> conduits,
            Map<Direction, ConduitConnection> connections, ItemStack facadeItem,
            Map<Holder<Conduit<?>>, ConduitGraphObject> conduitNodes) {

        this.pos = pos;
        this.conduits.addAll(conduits);
        this.connections.putAll(connections);
        this.conduitNodes.putAll(conduitNodes);
        this.facadeItem = facadeItem;
    }

    // TODO: I kind of want to get rid of this.
    public void setOnChangedRunnable(Runnable onChangedRunnable) {
        this.onChangedRunnable = onChangedRunnable;
    }

    public void onChanged() {
        if (onChangedRunnable != null) {
            onChangedRunnable.run();
        }
    }

    /**
     * @return an action containing the conduit that is now not in this bundle
     */
    public AddConduitResult addConduit(Level level, Holder<Conduit<?>> conduit, Player player) {
        if (conduits.size() == MAX_CONDUITS) {
            return new AddConduitResult.Blocked();
        }

        if (conduits.contains(conduit)) {
            return new AddConduitResult.Blocked();
        }

        // New node
        ConduitGraphObject node = new ConduitGraphObject(pos);

        // upgrade a conduit
        Optional<? extends Holder<Conduit<?>>> first = conduits.stream()
                .filter(existingConduit -> existingConduit.value().canBeReplacedBy(conduit))
                .findFirst();
        if (first.isPresent()) {
            int index = conduits.indexOf(first.get());
            conduits.set(index, conduit);

            ConduitGraphObject prevNode = conduitNodes.remove(first.get());

            if (prevNode != null) {
                node = new ConduitGraphObject(pos, prevNode.getNodeData()); // new node with old data
                conduit.value().onRemoved(prevNode, level, pos);
                if (!level.isClientSide() && prevNode.getGraph() != null) {
                    prevNode.getGraph().remove(prevNode);
                }
            }

            conduitNodes.put(conduit, node);
            conduit.value().onCreated(node, level, pos, player);
            onChanged();

            return new AddConduitResult.Upgrade(first.get());
        }

        // some conduit says no (like higher energy conduit)
        if (conduits.stream()
                .anyMatch(existingConduit -> !existingConduit.value().canBeInSameBundle(conduit)
                        || !conduit.value().canBeInSameBundle(existingConduit))) {
            return new AddConduitResult.Blocked();
        }

        // sort the list, so order is consistent
        int id = ConduitSorter.getSortIndex(conduit);
        var addBefore = conduits.stream().filter(existing -> ConduitSorter.getSortIndex(existing) > id).findFirst();
        if (addBefore.isPresent()) {
            var value = conduits.indexOf(addBefore.get());
            conduits.add(value, conduit);
            conduitNodes.put(conduit, node);

            conduit.value().onCreated(node, level, pos, player);

            for (Direction direction : Direction.values()) {
                connections.get(direction).addType(value);
            }
        } else {
            conduits.add(conduit);
            conduitNodes.put(conduit, node);
            if (conduits.size() != 1) {
                // NeoForge contains a patch that calls onLoad after the conduit has been placed
                // if it's the first one, so onCreated would be called twice. it's easier to
                // detect here
                conduit.value().onCreated(node, level, pos, player);
            }
        }

        onChanged();
        return new AddConduitResult.Insert();
    }

    public void onLoad(Level level, BlockPos pos) {
        for (Holder<Conduit<?>> conduit : conduits) {
            var node = getNodeFor(conduit);
            conduit.value().onCreated(node, level, pos, null);
        }
    }

    /**
     * @return if this bundle is empty and the block has to be removed
     * @throws IllegalArgumentException if this conduit is not in the conduit bundle and we are in dev env
     */
    public boolean removeConduit(Level level, Holder<Conduit<?>> conduit) {
        int index = conduits.indexOf(conduit);
        if (index == -1) {
            if (!FMLLoader.isProduction()) {
                throw new IllegalArgumentException(
                        "Conduit: " + conduit.getRegisteredName() + " is not present in conduit bundle "
                                + Arrays.toString(conduits.stream().map(Holder::getRegisteredName).toArray()));
            }

            return conduits.isEmpty();
        }

        for (Direction direction : Direction.values()) {
            connections.get(direction).removeType(index);
        }

        if (EffectiveSide.get().isServer()) {
            var node = getNodeForTypeExact(conduit);
            if (node != null) {
                removeNode(level, conduit, node);
            }
        }

        conduits.remove(index);
        onChanged();
        return conduits.isEmpty();
    }

    // endregion

    public List<Holder<Conduit<?>>> getConduits() {
        return conduits;
    }

    // region Connections

    public List<Holder<Conduit<?>>> getConnectedConduits(Direction direction) {
        return connections.get(direction).getConnectedTypes(this);
    }

    // Not a fan of this.
    @Deprecated(forRemoval = true)
    public ConnectionState getConnectionState(Direction direction, int index) {
        return connections.get(direction).getConnectionState(index);
    }

    public ConnectionState getConnectionState(Direction direction, Holder<Conduit<?>> conduit) {
        return connections.get(direction).getConnectionState(getConduitIndex(conduit));
    }

    public void setConnectionState(Direction direction, Holder<Conduit<?>> conduit, ConnectionState state) {
        connections.get(direction).setConnectionState(getConduitIndex(conduit), state);
        onChanged();
    }

    public boolean isConnectionEnd(Direction direction) {
        return connections.get(direction).isEnd();
    }

    // Not a fan of this.
    @Deprecated(forRemoval = true)
    public void disableConduit(Direction direction, int index) {
        connections.get(direction).disableType(index);
        onChanged();
    }

    public void disableConduit(Direction direction, Holder<Conduit<?>> conduit) {
        disableConduit(direction, getConduitIndex(conduit));
    }

    public ItemStack getConnectionItem(Direction direction, int conduitIndex, SlotType slotType) {
        return connections.get(direction).getItem(slotType, conduitIndex);
    }

    public ItemStack getConnectionItem(Direction direction, Holder<Conduit<?>> conduit, SlotType slotType) {
        return getConnectionItem(direction, getConduitIndex(conduit), slotType);
    }

    public void setConnectionItem(Direction direction, int conduitIndex, SlotType slotType, ItemStack itemStack) {
        connections.get(direction).setItem(slotType, conduitIndex, itemStack);
        onChanged();
    }

    public void setConnectionItem(Direction direction, Holder<Conduit<?>> conduit, SlotType slotType,
            ItemStack itemStack) {
        setConnectionItem(direction, getConduitIndex(conduit), slotType, itemStack);
    }

    // endregion

    // region Facades

    public boolean hasFacade() {
        return !facadeItem.isEmpty() && facadeItem.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER) != null;
    }

    public ItemStack facadeItem() {
        return facadeItem.copy();
    }

    public Optional<Block> facade() {
        if (!hasFacade()) {
            return Optional.empty();
        }

        return Optional.of(
                Objects.requireNonNull(facadeItem.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER)).block());
    }

    public Optional<FacadeType> facadeType() {
        if (!hasFacade()) {
            return Optional.empty();
        }

        return Optional.of(
                Objects.requireNonNull(facadeItem.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER)).type());
    }

    public void facade(ItemStack facadeItem) {
        this.facadeItem = facadeItem.copyWithCount(1);
        onChanged();
    }

    public void clearFacade() {
        facadeItem = ItemStack.EMPTY;
        onChanged();
    }

    // endregion

    public void connectTo(Level level, BlockPos pos, Direction direction, Holder<Conduit<?>> conduit, boolean end) {
        connections.get(direction)
                .connectTo(level, pos, getNodeFor(conduit), direction, conduit, getConduitIndex(conduit), end);
        onChanged();
    }

    public boolean disconnectFrom(Direction direction, Holder<Conduit<?>> conduit) {
        for (int i = 0; i < conduits.size(); i++) {
            if (conduit.value().canConnectTo(conduits.get(i))) {
                connections.get(direction).tryDisconnect(i);
                onChanged();
                return true;
            }
        }
        return false;
    }

    @Nullable
    public ConduitGraphObject getNodeForTypeExact(Holder<Conduit<?>> conduit) {
        return conduitNodes.get(conduit);
    }

    public ConduitGraphObject getNodeFor(Holder<Conduit<?>> conduit) {
        for (var entry : conduitNodes.entrySet()) {
            if (entry.getKey().value().canConnectTo(conduit)) {
                return conduitNodes.get(entry.getKey());
            }
        }

        throw new IllegalStateException("no node matching original conduit");
    }

    public void setNodeFor(Holder<Conduit<?>> conduit, ConduitGraphObject node) {
        conduitNodes.put(conduit, node);
        for (var direction : Direction.values()) {
            ConduitConnection connection = connections.get(direction);
            int index = conduits.indexOf(conduit);
            if (index >= 0) {
                var state = connection.getConnectionState(index);
                if (state instanceof DynamicConnectionState dynamicState) {
//                    node.pushState(direction, dynamicState);
                }
            }
        }
    }

    private void removeNode(Level level, Holder<Conduit<?>> conduit, ConduitGraphObject node) {
        conduit.value().onRemoved(node, level, pos);
        if (node.getGraph() != null) {
            node.getGraph().remove(node);
        }

        conduitNodes.remove(conduit);
    }

    public boolean hasType(Holder<Conduit<?>> conduitToFind) {
        for (var conduit : conduits) {
            if (conduit.value().canConnectTo(conduitToFind)) {
                return true;
            }
        }

        return false;
    }

    public int getConduitIndex(Holder<Conduit<?>> conduit) {
        for (int i = 0; i < conduits.size(); i++) {
            if (conduits.get(i).value().canConnectTo(conduit)) {
                return i;
            }
        }
        throw new IllegalStateException("no matching conduit in bundle");
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(connections, conduits, facadeItem);

        // Manually hash the map, using hashContents instead of hashCode to avoid
        // breaking the graph.
        for (var entry : conduitNodes.entrySet()) {
            hash = 31 * hash + entry.getKey().hashCode();
        }

        return hash;
    }

    public Tag save(HolderLookup.Provider lookupProvider) {
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    public static ConduitBundle parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.decode(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow().getFirst();
    }

    @EnsureSide(EnsureSide.Side.CLIENT)
    public ConduitBundle deepCopy() {
        var bundle = new ConduitBundle(() -> {
        }, pos);
        bundle.conduits.addAll(conduits);
        connections.forEach((dir, connection) -> bundle.connections.put(dir, connection.deepCopy()));
//        conduitNodes.forEach((conduit, node) -> bundle.setNodeFor(conduit, node.deepCopy()));
        bundle.facadeItem = facadeItem.copy();
        return bundle;
    }

    // TODO: Clean this up
    public static final class ConduitConnection {

        public static Codec<ConduitConnection> CODEC = ConnectionState.CODEC.listOf(0, MAX_CONDUITS)
                .xmap(ConduitConnection::new, i -> Arrays.stream(i.connectionStates).toList());

        public static StreamCodec<RegistryFriendlyByteBuf, ConduitConnection> STREAM_CODEC = ConnectionState.STREAM_CODEC
                .apply(ByteBufCodecs.list())
                .map(ConduitConnection::new, i -> Arrays.stream(i.connectionStates).toList());

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

        /**
         * shift all behind that one to the back and set that index to null
         */
        public void addType(int index) {
            for (int i = MAX_CONDUITS - 1; i > index; i--) {
                connectionStates[i] = connectionStates[i - 1];
            }
            connectionStates[index] = StaticConnectionStates.DISCONNECTED;
        }

        public void connectTo(Level level, BlockPos pos, ConduitGraphObject conduitGraphObject, Direction direction,
                Holder<Conduit<?>> type, int typeIndex, boolean end) {
            if (end) {
                var state = DynamicConnectionState.defaultConnection(level, pos, direction, type);
                connectionStates[typeIndex] = state;
//                conduitGraphObject.pushState(direction, state);
            } else {
                connectionStates[typeIndex] = StaticConnectionStates.CONNECTED;
            }
        }

        public void tryDisconnect(int typeIndex) {
            if (connectionStates[typeIndex] != StaticConnectionStates.DISABLED) {
                connectionStates[typeIndex] = StaticConnectionStates.DISCONNECTED;
            }
        }

        // TODO: Come back and review use of the term "Type" here.

        /**
         * remove entry and shift all behind one to the front
         */
        public void removeType(int index) {
            connectionStates[index] = StaticConnectionStates.DISCONNECTED;
            for (int i = index + 1; i < MAX_CONDUITS; i++) {
                connectionStates[i - 1] = connectionStates[i];
            }
            connectionStates[MAX_CONDUITS - 1] = StaticConnectionStates.DISCONNECTED;
        }

        public void disconnectType(int index) {
            connectionStates[index] = StaticConnectionStates.DISCONNECTED;
        }

        public void disableType(int index) {
            connectionStates[index] = StaticConnectionStates.DISABLED;
        }

        public boolean isEnd() {
            return Arrays.stream(connectionStates).anyMatch(DynamicConnectionState.class::isInstance);
        }

        public List<Holder<Conduit<?>>> getConnectedTypes(ConduitBundle bundle) {
            List<Holder<Conduit<?>>> connected = new ArrayList<>();
            for (int i = 0; i < connectionStates.length; i++) {
                if (connectionStates[i].isConnection()) {
                    connected.add(bundle.getConduits().get(i));
                }
            }

            return connected;
        }

        public ConduitConnection deepCopy() {
            ConduitConnection connection = new ConduitConnection();
            // connection states are not mutable (enum/record), so reference is fine
            System.arraycopy(connectionStates, 0, connection.connectionStates, 0, MAX_CONDUITS);
            return connection;
        }

        public ConnectionState getConnectionState(int index) {
            return connectionStates[index];
        }

        public void setConnectionState(int i, ConnectionState state) {
            connectionStates[i] = state;
        }

        public ItemStack getItem(SlotType type, int conduitIndex) {
            if (connectionStates[conduitIndex] instanceof DynamicConnectionState dynamicConnectionState) {
                return dynamicConnectionState.getItem(type);
            }

            return ItemStack.EMPTY;
        }

        public void setItem(SlotType type, int conduitIndex, ItemStack stack) {
            if (connectionStates[conduitIndex] instanceof DynamicConnectionState dynamicConnectionState) {
                connectionStates[conduitIndex] = dynamicConnectionState.withItem(type, stack);
            }
        }

        @Override
        public int hashCode() {
            // return i++;
            return Objects.hash((Object[]) connectionStates);
        }
    }
}
