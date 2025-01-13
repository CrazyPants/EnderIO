package com.enderio.conduits.api;

import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.network.node.legacy.ConduitDataAccessor;
import com.enderio.conduits.api.ticker.ConduitTicker;
import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

public interface Conduit<TConduit extends Conduit<TConduit, TConnectionConfig>, TConnectionConfig extends ConnectionConfig>
        extends Comparable<TConduit>, TooltipProvider {

    Codec<Conduit<?, ?>> DIRECT_CODEC = EnderIOConduitsRegistries.CONDUIT_TYPE.byNameCodec()
            .dispatch(Conduit::type, ConduitType::codec);

    Codec<Holder<Conduit<?, ?>>> CODEC = RegistryFixedCodec.create(EnderIOConduitsRegistries.Keys.CONDUIT);

    StreamCodec<RegistryFriendlyByteBuf, Holder<Conduit<?, ?>>> STREAM_CODEC = ByteBufCodecs
            .holderRegistry(EnderIOConduitsRegistries.Keys.CONDUIT);

    /**
     * Gets the default conduit texture.
     */
    ResourceLocation texture();

    /**
     * Gets the conduit description, used for the conduit item.
     */
    Component description();

    /**
     * @implNote Must be >= 1 and <= 20
     * @return the number of ticks that should pass before the conduit graph ticks.
     */
    default int graphTickRate() {
        return 5;
    }

    /**
     * Gets the conduit type.
     * This is used to define serialization and exposing proxied capabilities.
     */
    ConduitType<TConduit> type();

    /**
     * Get the ticker for this conduit graph type.
     * @apiNote The ticker should never change, it can use the options to determine behaviour in its implementation.
     */
    ConduitTicker<TConduit> getTicker();

    /**
     * @implNote if a conduit has a menu, you must also register a {@link com.enderio.conduits.api.screen.ConduitScreenType} for it.
     * @return whether this conduit has a menu.
     */
    boolean hasMenu();

    // region Conduit Checks

    default boolean canBeInSameBundle(Holder<Conduit<?, ?>> otherConduit) {
        return true;
    }

    default boolean canBeReplacedBy(Holder<Conduit<?, ?>> otherConduit) {
        return false;
    }

    /**
     * @return true if both types are compatible
     */
    default boolean canConnectToConduit(Holder<Conduit<?, ?>> other) {
        return this.equals(other.value());
    }

    /**
     * If this conduit overrides {@link #canConnectConduits(ConduitNode, ConduitNode)}, return true.
     * This will avoid showing connections between conduits on the client until the server evaluates whether they can connect.
     * @apiNote Failing to override this properly could result in connection desyncs.
     * @return whether this conduit has additional server-side connection checks.
     */
    default boolean hasServerConnectionChecks() {
        return false;
    }

    /**
     * This can be used to prevent connection between nodes with incompatible data.
     * @apiNote Not called by the server if {@link #hasServerConnectionChecks()} does not return true.
     * @return true if both nodes are compatible.
     */
    default boolean canConnectConduits(ConduitNode selfNode, ConduitNode otherNode) {
        return true;
    }

    // endregion

    // region Connection Checks

    boolean canConnectToBlock(Level level, BlockPos conduitPos, Direction direction);

    default boolean canForceConnectToBlock(Level level, BlockPos conduitPos, Direction direction) {
        return canConnectToBlock(level, conduitPos, direction);
    }

    // endregion

    /**
     * @return if this is not always able to determine connectivity to its neighbours at time of placement, but the tick later
     */
    default boolean hasConnectionDelay() {
        return false;
    }

    default boolean canApplyFilter(SlotType slotType, ResourceFilter resourceFilter) {
        return false;
    }

    /**
     * Gets the conduit texture to display, given the data.
     * @param extraWorldData client data from {@link #getExtraWorldData(ConduitBundleReader, ConduitNode)}.
     */
    default ResourceLocation getTexture(@Nullable CompoundTag extraWorldData) {
        return texture();
    }

    // region Events

    default void onCreated(ConduitNode node, Level level, BlockPos pos, @Nullable Player player) {
    }

    default void onRemoved(ConduitNode node, Level level, BlockPos pos) {
    }

    default void onConnectionsUpdated(ConduitNode node, Level level, BlockPos pos, Set<Direction> connectedSides) {
    }

    default void onConnectTo(ConduitNode selfNode, ConduitNode otherNode) {
    }

    // endregion

    @Nullable
    default <TCapability, TContext> TCapability proxyCapability(Level level,
            ColoredRedstoneProvider coloredRedstoneProvider, ConduitNode node,
            BlockCapability<TCapability, TContext> capability, @Nullable TContext context) {
        return null;
    }

    // region Conduit Data

    /**
     * @return the expected conduit connection config type.
     */
    ConnectionConfigType<TConnectionConfig> connectionConfigType();

    /**
     * Convert old conduit connection data into the new connection config.
     * This is executed during world load, so no level is available to query.
     * @implNote Only needs to be implemented if the conduit existed in Ender IO 7.1 or earlier.
     * @deprecated Only for conversion of <7.1 conduit data. Will be removed in Ender IO 8.
     */
    @Deprecated(since = "7.2")
    default TConnectionConfig convertConnection(boolean isInsert, boolean isExtract, DyeColor inputChannel,
            DyeColor outputChannel, RedstoneControl redstoneControl, DyeColor redstoneChannel) {
        return connectionConfigType().getDefault();
    }

    /**
     * Copy legacy data from the old conduit data accessor to the new node however you wish.
     * @implNote The node is guaranteed to have a network at this point, so the context can be accessed.
     * @param node the node.
     * @param legacyDataAccessor the legacy data.
     */
    @Deprecated(since = "7.2")
    default void copyLegacyData(ConduitNode node, ConduitDataAccessor legacyDataAccessor) {
    }

    // endregion

    // region Custom Sync

    @Nullable
    default CompoundTag getExtraGuiData(ConduitBundleReader conduitBundle, ConduitNode node, Direction side) {
        return null;
    }

    /**
     * Create a custom tag for syncing data from node data or network context to the client for extra behaviours.
     * @return custom sync data.
     */
    @Nullable
    default CompoundTag getExtraWorldData(ConduitBundleReader conduitBundle, ConduitNode node) {
        return null;
    }

    // endregion

    @Override
    default void addToTooltip(Item.TooltipContext pContext, Consumer<Component> pTooltipAdder,
            TooltipFlag pTooltipFlag) {
    }

    /**
     * @return true if this conduit has more tooltips when shift is held.
     */
    default boolean hasAdvancedTooltip() {
        return false;
    }

    /**
     * @return true if this conduit should show graph debug tooltips (when shift is held).
     */
    default boolean showDebugTooltip() {
        return false;
    }
}
