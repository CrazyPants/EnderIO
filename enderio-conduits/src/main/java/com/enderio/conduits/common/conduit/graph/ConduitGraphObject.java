package com.enderio.conduits.common.conduit.graph;

import com.enderio.base.api.UseOnly;
import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.conduits.api.ConduitCapabilities;
import com.enderio.conduits.api.ConduitData;
import com.enderio.conduits.api.ConduitDataType;
import com.enderio.conduits.api.ConduitNetwork;
import com.enderio.conduits.api.ConduitNode;
import com.enderio.conduits.api.bundle.ConduitInventory;
import com.enderio.conduits.api.connection.ConduitConnection;
import com.enderio.conduits.api.upgrade.ConduitUpgrade;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.fml.LogicalSide;
import org.jetbrains.annotations.Nullable;

public class ConduitGraphObject implements GraphObject<ConduitGraphContext>, ConduitNode {

    public static final Codec<ConduitGraphObject> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BlockPos.CODEC.fieldOf("pos").forGetter(ConduitGraphObject::getPos),
                    ConduitDataContainer.CODEC.fieldOf("data").forGetter(i -> i.conduitDataContainer))
            .apply(instance, ConduitGraphObject::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConduitGraphObject> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ConduitGraphObject::getPos, ConduitDataContainer.STREAM_CODEC,
            i -> i.conduitDataContainer, ConduitGraphObject::new);

    private final BlockPos pos;

    @Nullable
    private Graph<ConduitGraphContext> graph = null;
    @Nullable
    private WrappedConduitNetwork wrappedGraph = null;

    private final Map<Direction, IOState> ioStates = new EnumMap<>(Direction.class);
    private final ConduitDataContainer conduitDataContainer;
    private final Map<Direction, DynamicConnectionState> connectionStates = new EnumMap<>(Direction.class);

    private final Map<Direction, ConduitConnection> connections = new EnumMap<>(Direction.class);

    private @Nullable ConduitInventory inventory;

    public ConduitGraphObject(BlockPos pos) {
        this.pos = pos;
        this.conduitDataContainer = new ConduitDataContainer();
    }

    public ConduitGraphObject(BlockPos pos, ConduitDataContainer conduitDataContainer) {
        this.pos = pos;
        this.conduitDataContainer = conduitDataContainer;
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
    }

    @Nullable
    @Override
    public ConduitNetwork getParentGraph() {
        return wrappedGraph;
    }

    public void setInventory(@Nullable ConduitInventory inventory) {
        this.inventory = inventory;
    }

    public void setConnection(Direction side, @Nullable ConduitConnection connection) {
        if (connection != null) {
            connections.put(side, connection);
            ioStates.put(side,
                    IOState.of(connection.canInput() ? connection.inputChannel() : null,
                            connection.canOutput() ? connection.outputChannel() : null, connection.redstoneControl(),
                            connection.redstoneChannel()));
        } else {
            connections.remove(side);
            ioStates.remove(side);
        }
    }

    public void pushState(Direction direction, DynamicConnectionState connectionState) {
        this.connectionStates.put(direction, connectionState);
        ioStates.put(direction,
                IOState.of(connectionState.isInsert() ? connectionState.insertChannel() : null,
                        connectionState.isExtract() ? connectionState.extractChannel() : null,
                        connectionState.control(), connectionState.redstoneChannel()));
    }

    public Optional<IOState> getIOState(Direction direction) {
        return Optional.ofNullable(ioStates.get(direction));
    }

    public void clearState(Direction direction) {
        ioStates.remove(direction);
    }

    public BlockPos getPos() {
        return pos;
    }

    // region Conduit Data

    // We're implementing ConduitDataAccessor for ease here, but we just pass
    // through to the container.

    @Override
    public boolean hasData(ConduitDataType<?> type) {
        return conduitDataContainer.hasData(type);
    }

    @Override
    public <T extends ConduitData<T>> @Nullable T getData(ConduitDataType<T> type) {
        return conduitDataContainer.getData(type);
    }

    @Override
    public <T extends ConduitData<T>> T getOrCreateData(ConduitDataType<T> type) {
        return conduitDataContainer.getOrCreateData(type);
    }

    public ConduitDataContainer conduitDataContainer() {
        return conduitDataContainer;
    }

    public void handleClientChanges(ConduitDataContainer clientDataContainer) {
        conduitDataContainer.handleClientChanges(clientDataContainer);
    }

    // endregion

    @Override
    public @Nullable ConduitUpgrade getUpgrade(Direction direction) {
        return connectionStates.get(direction).upgradeExtract().getCapability(ConduitCapabilities.CONDUIT_UPGRADE);
    }

    @Override
    public @Nullable ResourceFilter getExtractFilter(Direction direction) {
        return connectionStates.get(direction).filterExtract().getCapability(EIOCapabilities.Filter.ITEM);
    }

    @Override
    public @Nullable ResourceFilter getInsertFilter(Direction direction) {
        return connectionStates.get(direction).filterInsert().getCapability(EIOCapabilities.Filter.ITEM);
    }

    @UseOnly(LogicalSide.CLIENT)
    public ConduitGraphObject deepCopy() {
        return new ConduitGraphObject(pos, conduitDataContainer.deepCopy());
    }

    public Tag save(HolderLookup.Provider lookupProvider) {
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    public static ConduitGraphObject parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.decode(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow().getFirst();
    }

    // Separate method to avoid breaking the graph
    public int hashContents() {
        return Objects.hash(pos, conduitDataContainer, ioStates, connectionStates);
    }
}
