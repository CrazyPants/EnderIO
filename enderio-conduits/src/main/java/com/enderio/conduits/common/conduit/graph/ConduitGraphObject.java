package com.enderio.conduits.common.conduit.graph;

import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.network.node.NodeData;
import com.enderio.conduits.api.network.node.NodeDataType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

public class ConduitGraphObject implements GraphObject<ConduitGraphContext>, ConduitNode {

    private static final Codec<ConduitGraphObject> LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BlockPos.CODEC.fieldOf("pos").forGetter(ConduitGraphObject::getPos),
                    ConduitDataContainer.CODEC.fieldOf("data").forGetter(i -> i.legacyDataContainer))
            .apply(instance, ConduitGraphObject::new));

    private static final Codec<ConduitGraphObject> NEW_CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(BlockPos.CODEC.fieldOf("pos").forGetter(ConduitGraphObject::getPos),
                            NodeData.GENERIC_CODEC.optionalFieldOf("node_data")
                                    .forGetter(i -> Optional.ofNullable(i.nodeData)))
                    .apply(instance, ConduitGraphObject::new));

    public static final Codec<ConduitGraphObject> CODEC = Codec.withAlternative(NEW_CODEC, LEGACY_CODEC);

    private BlockPos pos;

    @Nullable
    private Graph<ConduitGraphContext> graph = null;

    @Nullable
    private WrappedConduitNetwork wrappedGraph = null;

    @Nullable
    private ConduitDataContainer legacyDataContainer = null;

    @Nullable
    private NodeData nodeData;

    // TODO: Instead of a special construct, we could just pass the type and bundle
    // in?
    @Nullable
    private ConduitConnectionHost connectionHost;

    public ConduitGraphObject(BlockPos pos) {
        this.pos = pos;
        this.nodeData = null;
    }

    public ConduitGraphObject(BlockPos pos, ConduitDataContainer conduitDataContainer) {
        this.pos = pos;

        // Convert the old data
        this.legacyDataContainer = conduitDataContainer;
        var oldData = legacyDataContainer.getData();
        if (oldData != null) {
            this.nodeData = oldData.toNodeData();
        } else {
            this.nodeData = null;
        }
    }

    public ConduitGraphObject(BlockPos pos, @Nullable NodeData nodeData) {
        this.pos = pos;
        this.nodeData = nodeData;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private ConduitGraphObject(BlockPos pos, Optional<NodeData> nodeData) {
        this.pos = pos;
        this.nodeData = nodeData.orElse(null);
    }

    @Nullable
    @Override
    public Graph<ConduitGraphContext> getGraph() {
        return graph;
    }

    @Override
    public void setGraph(@Nullable Graph<ConduitGraphContext> graph) {
        this.graph = graph;
        this.wrappedGraph = graph == null ? null : new WrappedConduitNetwork(graph);
        upgradeLegacyData();
    }

    @Nullable
    @Override
    public ConduitNetwork getNetwork() {
        return wrappedGraph;
    }

    public void attach(ConduitConnectionHost connectionHost) {
        if (!connectionHost.pos().equals(pos)) {
            throw new IllegalArgumentException("Connection host and node position do not match!");
        }

        this.connectionHost = connectionHost;
        upgradeLegacyData();
    }

    public void detach() {
        this.connectionHost = null;
    }

    public BlockPos getPos() {
        return pos;
    }

    // TODO: Remove in EnderIO 8.
    // Convert old conduit data to the new formats.
    private void upgradeLegacyData() {
        if (graph == null || connectionHost == null) {
            return;
        }

        if (legacyDataContainer == null || !legacyDataContainer.hasData()) {
            return;
        }

        // Upgrade with old data
        // noinspection deprecation
        connectionHost.conduit().value().copyLegacyData(this, legacyDataContainer);
        legacyDataContainer = null;
    }

    // region Node Data

    @Override
    public boolean hasNodeData(NodeDataType<?> type) {
        return nodeData != null && nodeData.type() == type;
    }

    @Override
    public @Nullable NodeData getNodeData() {
        return nodeData;
    }

    @Override
    public <T extends NodeData> @Nullable T getNodeData(NodeDataType<T> type) {
        if (nodeData != null && type == nodeData.type()) {
            // noinspection unchecked
            return (T) nodeData;
        }

        return null;
    }

    @Override
    public <T extends NodeData> T getOrCreateNodeData(NodeDataType<T> type) {
        if (nodeData != null && type == nodeData.type()) {
            // noinspection unchecked
            return (T) nodeData;
        }

        nodeData = type.factory().get();
        // noinspection unchecked
        return (T) nodeData;
    }

    @Override
    public <T extends NodeData> void setNodeData(@Nullable T data) {
        nodeData = data;
    }

    // endregion

    // region Connection Config

    @Override
    public boolean isConnectedTo(Direction side) {
        if (connectionHost == null) {
            throw new IllegalStateException("No connection host!");
        }

        return connectionHost.isConnectedTo(side);
    }

    @Override
    public ConnectionConfig getConnectionConfig(Direction side) {
        if (connectionHost == null) {
            throw new IllegalStateException("No connection host!");
        }

        return connectionHost.getConnectionConfig(side);
    }

    @Override
    public <T extends ConnectionConfig> T getConnectionConfig(Direction side, ConnectionConfigType<T> type) {
        var config = getConnectionConfig(side);

        if (config.type() != type) {
            throw new IllegalStateException(
                    "Connection config type mismatch! Conversion failed somewhere in the bundle.");
        }

        // noinspection unchecked
        return (T) config;
    }

    @Override
    public void setConnectionConfig(Direction side, ConnectionConfig config) {
        if (connectionHost == null) {
            throw new IllegalStateException("No connection host!");
        }

        if (config.type() != connectionHost.getConnectionConfig(side).type()) {
            throw new IllegalArgumentException("Connection config type mismatch!");
        }

        connectionHost.setConnectionConfig(side, config);
    }

    // endregion

    @Override
    public @Nullable ResourceFilter getExtractFilter(Direction direction) {
        if (connectionHost == null) {
            throw new IllegalStateException("No connection host!");
        }

        return connectionHost.inventory()
                .getStackInSlot(direction, SlotType.FILTER_EXTRACT)
                .getCapability(EIOCapabilities.Filter.ITEM);
    }

    @Override
    public @Nullable ResourceFilter getInsertFilter(Direction direction) {
        if (connectionHost == null) {
            throw new IllegalStateException("No connection host!");
        }

        return connectionHost.inventory()
                .getStackInSlot(direction, SlotType.FILTER_INSERT)
                .getCapability(EIOCapabilities.Filter.ITEM);
    }

    @Override
    public boolean isLoaded() {
        if (connectionHost == null) {
            return false;
        }

        return connectionHost.isLoaded();
    }

    @Override
    public boolean hasRedstoneSignal(@Nullable DyeColor signalColor) {
        if (!isLoaded()) {
            throw new IllegalStateException("Unable to query redstone signals when the node is not loaded.");
        }

        return connectionHost.hasRedstoneSignal(signalColor);
    }

    @Override
    public void markDirty() {
        if (connectionHost != null) {
            connectionHost.onNodeDirty();
        }
    }

    public Tag save(HolderLookup.Provider lookupProvider) {
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    public static ConduitGraphObject parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.decode(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow().getFirst();
    }
}
