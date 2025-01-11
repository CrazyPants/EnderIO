package com.enderio.conduits.common.conduit;

import com.enderio.conduits.EnderIOConduits;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.api.ticker.ConduitTicker;
import com.enderio.conduits.common.conduit.bundle.ConduitBundleBlockEntity;
import com.enderio.conduits.common.conduit.graph.ConduitGraphContext;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import com.enderio.conduits.common.conduit.graph.ConduitGraphUtility;
import com.enderio.conduits.common.conduit.graph.WrappedConduitNetwork;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitNetworkContext;
import com.enderio.conduits.common.init.Conduits;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import dev.gigaherz.graph3.Mergeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@EventBusSubscriber(modid = EnderIOConduits.MODULE_MOD_ID)
public class ConduitSavedData extends SavedData {

    private final Map<Holder<Conduit<?, ?>>, List<Graph<ConduitGraphContext>>> networks = new HashMap<>();

    // Used to find the ConduitGraphObject(s) of a conduit when it is loaded
    private final Map<Holder<Conduit<?, ?>>, Map<ChunkPos, Map<BlockPos, ConduitGraphObject>>> deserializedNodes = new HashMap<>();

    private static final Logger LOGGER = LogUtils.getLogger();

    public static ConduitSavedData get(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(new Factory<>(ConduitSavedData::new, ConduitSavedData::new),
                        "enderio_conduit_network");
    }

    private ConduitSavedData() {
    }

    // region Serialization

    private static final String KEY_GRAPHS = "Graphs";
    private static final String KEY_TYPE = "Type";
    private static final String KEY_GRAPH_OBJECTS = "GraphObjects";
    private static final String KEY_GRAPH_CONNECTIONS = "GraphConnections";
    private static final String KEY_GRAPH_CONTEXT = "GraphContext";

    // Deserialization
    private ConduitSavedData(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
        ListTag graphsTag = nbt.getList(KEY_GRAPHS, Tag.TAG_COMPOUND);
        for (Tag tag : graphsTag) {
            CompoundTag typedGraphTag = (CompoundTag) tag;
            ResourceKey<Conduit<?, ?>> conduitKey = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
                    ResourceLocation.parse(typedGraphTag.getString(KEY_TYPE)));

            var registry = lookupProvider.lookupOrThrow(EnderIOConduitsRegistries.Keys.CONDUIT);

            Optional<Holder.Reference<Conduit<?, ?>>> conduit = registry.get(conduitKey);

            if (conduit.isPresent()) {
                ListTag graphsForTypeTag = typedGraphTag.getList(KEY_GRAPHS, Tag.TAG_COMPOUND);
                deserializeGraphs(lookupProvider, conduit.get(), graphsForTypeTag);
            } else {
                LOGGER.warn("Skipping graph for missing conduit: " + conduitKey);
            }
        }
    }

    private void deserializeGraphs(HolderLookup.Provider lookupProvider, Holder<Conduit<?, ?>> conduit, ListTag graphs) {
        for (Tag tag1 : graphs) {
            CompoundTag graphTag = (CompoundTag) tag1;

            ListTag graphObjectsTag = graphTag.getList(KEY_GRAPH_OBJECTS, Tag.TAG_COMPOUND);
            ListTag graphConnectionsTag = graphTag.getList(KEY_GRAPH_CONNECTIONS, Tag.TAG_COMPOUND);

            List<ConduitGraphObject> graphObjects = new ArrayList<>();
            List<Pair<ConduitGraphObject, ConduitGraphObject>> connections = new ArrayList<>();

            for (int i = 0; i < graphObjectsTag.size(); i++) {
                CompoundTag nodeTag = graphObjectsTag.getCompound(i);
                var node = ConduitGraphObject.CODEC
                        .decode(lookupProvider.createSerializationContext(NbtOps.INSTANCE), nodeTag)
                        .getOrThrow()
                        .getFirst();

                graphObjects.add(node);
                putUnloadedNodeIdentifier(conduit, node.getPos(), node);
            }

            for (Tag tag2 : graphConnectionsTag) {
                CompoundTag connectionTag = (CompoundTag) tag2;
                connections.add(new Pair<>(graphObjects.get(connectionTag.getInt("0")),
                        graphObjects.get(connectionTag.getInt("1"))));
            }

            ConduitGraphObject graphObject = graphObjects.get(0);
            if (graphTag.contains(KEY_GRAPH_CONTEXT)) {
                ConduitGraphUtility.integrateWithLoad(conduit, graphObject, List.of(), lookupProvider,
                        graphTag.getCompound(KEY_GRAPH_CONTEXT));
            } else {
                ConduitGraphUtility.integrate(conduit, graphObject, List.of());
            }

            merge(conduit, graphObject, connections);

            networks.computeIfAbsent(conduit, t -> new ArrayList<>()).add(graphObject.getGraph());
        }
    }

    // Serialization

    // @formatter:off
    /* NBT layout
    data
      ┖ graphs (list)
          ┖ [index]
              ┠ type: [conduit type] (ex. "enderio:power3")
              ┖ graphs (list) // One type of conduits' graphs
                  ┠ [element]
                  ┃   ┠ graphConnections (list)
                  ┃   ┃   ┖ [element]
                  ┃   ┃       ┠ 0: [first object's index]
                  ┃   ┃       ┖ 1: [second object's index]
                  ┃   ┖ graphObjects (list)
                  ┃       ┖ [element]
                  ┃         ┠ pos:
                  ┃         ┃   ┠ x:
                  ┃         ┃   ┠ y:
                  ┃         ┃   ┖ z:
                  ┃         ┖ data: Compound based on type
                  ┖ [next element]
                      ┖ ...
     */
    // @formatter:on

    // Serialization
    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider lookupProvider) {
        ListTag graphsTag = new ListTag();
        for (var entry : networks.entrySet()) {
            Holder<Conduit<?, ?>> type = entry.getKey();
            List<Graph<ConduitGraphContext>> graphs = entry.getValue();
            if (graphs.isEmpty() || !type.isBound()) {
                continue;
            }

            CompoundTag typedGraphTag = new CompoundTag();
            typedGraphTag.putString(KEY_TYPE, type.getRegisteredName());

            ListTag graphsForTypeTag = new ListTag();

            for (Graph<ConduitGraphContext> graph : graphs) {
                if (!graph.getObjects().isEmpty()) {
                    graphsForTypeTag.add(serializeGraph(lookupProvider, graph));
                }
            }

            if (!graphsForTypeTag.isEmpty()) {
                typedGraphTag.put(KEY_GRAPHS, graphsForTypeTag);
                graphsTag.add(typedGraphTag);
            }
        }

        nbt.put(KEY_GRAPHS, graphsTag);
        return nbt;
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    private static CompoundTag serializeGraph(HolderLookup.Provider lookupProvider, Graph<ConduitGraphContext> graph) {
        List<GraphObject<ConduitGraphContext>> graphObjects = new ArrayList<>(graph.getObjects());
        List<Pair<GraphObject<ConduitGraphContext>, GraphObject<ConduitGraphContext>>> connections = new ArrayList<>();

        CompoundTag graphTag = new CompoundTag();

        var context = graph.getContextData();
        if (context != null) {
            CompoundTag tag = context.save(lookupProvider);
            if (tag != null) {
                graphTag.put(KEY_GRAPH_CONTEXT, tag);
            }
        }

        ListTag graphObjectsTag = new ListTag();
        ListTag graphConnectionsTag = new ListTag();

        for (GraphObject<ConduitGraphContext> graphObject : graphObjects) {
            for (GraphObject<ConduitGraphContext> neighbour : graph.getNeighbours(graphObject)) {
                var connection = new Pair<>(graphObject, neighbour);
                if (!containsConnection(connections, connection)) {
                    connections.add(connection);
                }
            }

            if (graphObject instanceof ConduitGraphObject conduitGraphObject) {
                var tag = ConduitGraphObject.CODEC
                        .encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), conduitGraphObject)
                        .getOrThrow();

                graphObjectsTag.add(tag);
            } else {
                throw new ClassCastException("graphObject was not of type nodeIdentifier");
            }
        }

        for (var connection : connections) {
            CompoundTag connectionTag = new CompoundTag();

            connectionTag.put("0", IntTag.valueOf(graphObjects.indexOf(connection.getFirst())));
            connectionTag.put("1", IntTag.valueOf(graphObjects.indexOf(connection.getSecond())));

            graphConnectionsTag.add(connectionTag);
        }

        graphTag.put(KEY_GRAPH_OBJECTS, graphObjectsTag);
        graphTag.put(KEY_GRAPH_CONNECTIONS, graphConnectionsTag);

        return graphTag;
    }

    // endregion

    private void merge(Holder<Conduit<?, ?>> conduit, GraphObject<ConduitGraphContext> object,
            List<Pair<ConduitGraphObject, ConduitGraphObject>> connections) {
        var filteredConnections = connections.stream()
                .filter(pair -> (pair.getFirst() == object || pair.getSecond() == object))
                .toList();

        List<? extends ConduitGraphObject> neighbors = filteredConnections.stream()
                .map(pair -> pair.getFirst() == object ? pair.getSecond() : pair.getFirst())
                .toList();

        for (var neighbor : neighbors) {
            ConduitGraphUtility.connect(conduit, object, neighbor);
        }

        connections = connections.stream().filter(v -> !filteredConnections.contains(v)).toList();
        if (!connections.isEmpty()) {
            merge(conduit, connections.get(0).getFirst(), connections);
        }
    }

    @Nullable
    public ConduitGraphObject takeUnloadedNodeIdentifier(Holder<Conduit<?, ?>> conduit, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);

        Map<ChunkPos, Map<BlockPos, ConduitGraphObject>> typeMap = deserializedNodes.get(conduit);
        if (typeMap == null) {
            LOGGER.warn("Conduit data is missing!");
            return null;
        }

        Map<BlockPos, ConduitGraphObject> chunkMap = typeMap.get(chunkPos);
        if (chunkMap == null) {
            LOGGER.warn("Conduit data is missing!");
            return null;
        }
        ConduitGraphObject node = chunkMap.get(pos);

        chunkMap.remove(pos);
        if (chunkMap.isEmpty()) {
            typeMap.remove(chunkPos);
        }

        if (typeMap.isEmpty()) {
            deserializedNodes.remove(conduit);
        }

        return node;
    }

    public void putUnloadedNodeIdentifier(Holder<Conduit<?, ?>> conduit, BlockPos pos, ConduitGraphObject node) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Map<ChunkPos, Map<BlockPos, ConduitGraphObject>> typeMap = deserializedNodes.computeIfAbsent(conduit,
                k -> new HashMap<>());
        Map<BlockPos, ConduitGraphObject> chunkMap = typeMap.computeIfAbsent(chunkPos, k -> new HashMap<>());
        chunkMap.put(pos, node);
    }

    private static <T extends Mergeable<T>> boolean containsConnection(
            List<Pair<GraphObject<T>, GraphObject<T>>> connections, Pair<GraphObject<T>, GraphObject<T>> connection) {
        return connections.contains(connection) || connections.contains(connection.swap());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            get(serverLevel).tick(serverLevel);
        }
    }

    private void tick(ServerLevel serverLevel) {
        setDirty();
        for (var entry : networks.entrySet()) {
            entry.getValue()
                    .removeIf(graph -> graph.getObjects().isEmpty()
                            || graph.getObjects().iterator().next().getGraph() != graph);
        }

        Registry<Conduit<?, ?>> conduitRegistry = serverLevel.registryAccess()
                .registryOrThrow(EnderIOConduitsRegistries.Keys.CONDUIT);

        for (var entry : networks.entrySet()) {
            var conduit = entry.getKey();
            int conduitId = conduitRegistry.getId(conduit.value());
            var conduitTicker = conduit.value().getTicker();

            for (var graph : entry.getValue()) {
                tickConduitGraph(serverLevel, entry.getKey(), conduitId, conduitTicker, graph);
            }
        }
    }

    private <T extends Conduit<T, ?>> void tickConduitGraph(ServerLevel serverLevel, Holder<Conduit<?, ?>> conduit,
            int conduitId, ConduitTicker<T> ticker, Graph<ConduitGraphContext> graph) {
        int conduitTickRate = conduit.value().graphTickRate();

        // TODO: Offsets for networks so they don't all tick on the same tick.
        if (serverLevel.getGameTime() % conduitTickRate == conduitId % conduitTickRate) {
            // noinspection unchecked
            ticker.tickGraph(serverLevel, (T) conduit.value(), new WrappedConduitNetwork(graph),
                    ConduitSavedData::isRedstoneActive);
        }
    }

    public static boolean isRedstoneActive(Level level, BlockPos pos, DyeColor color) {
        if (!level.isLoaded(pos) || !level.shouldTickBlocksAt(pos)) {
            return false;
        }

        if (!(level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBundle)) {
            return false;
        }

        // TODO: Decouple from hard-coded REDSTONE conduit like we have for the block.
        var registry = level.holderLookup(EnderIOConduitsRegistries.Keys.CONDUIT);
        var redstoneConduit = registry.get(Conduits.REDSTONE);

        if (redstoneConduit.isEmpty() || !conduitBundle.getConduits().contains(redstoneConduit.get())) {
            return false;
        }

        var node = conduitBundle.getConduitNode(redstoneConduit.get());
        if (node.getNetwork() == null) {
            return false;
        }

        var context = node.getNetwork().getContext(RedstoneConduitNetworkContext.TYPE);
        return context != null && context.isActive(color);
    }

    public static void addPotentialGraph(Holder<Conduit<?, ?>> conduit, Graph<ConduitGraphContext> graph,
            ServerLevel level) {
        get(level).addPotentialGraph(conduit, graph);
    }

    private void addPotentialGraph(Holder<Conduit<?, ?>> conduit, Graph<ConduitGraphContext> graph) {
        if (!networks.computeIfAbsent(conduit, unused -> new ArrayList<>()).contains(graph)) {
            networks.get(conduit).add(graph);
        }
    }
}
