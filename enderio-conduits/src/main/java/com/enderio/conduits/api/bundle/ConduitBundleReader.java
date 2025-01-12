package com.enderio.conduits.api.bundle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.connection.ConnectionStatus;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.facade.FacadeType;
import com.enderio.conduits.api.network.node.ConduitNode;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable access to a conduit bundle.
 */
@ApiStatus.Experimental
public interface ConduitBundleReader {

    /**
     * @implNote Must be sorted according to {@link com.enderio.conduits.api.ConduitApi#getConduitSortIndex(Holder)}
     * @return a list of all conduits in the bundle.
     */
    List<Holder<Conduit<?, ?>>> getConduits();

    /**
     * @param conduit the conduit to get data for.
     * @return the client data tag, or null if there is none or the conduit doesn't sync extra data.
     */
    @Nullable
    CompoundTag getConduitExtraWorldData(Holder<Conduit<?, ?>> conduit);

    // TODO: Docs
    @Nullable
    CompoundTag getConduitExtraGuiData(Direction side, Holder<Conduit<?, ?>> conduit);

    /**
     * @implNote compare conduits using {@link Conduit#canConnectToConduit(Holder)}
     * @param conduit the conduit to check for
     * @return whether the bundle has this conduit, or another which is compatible.
     */
    boolean hasConduitByType(Holder<Conduit<?, ?>> conduit);

    // TODO: Docs
    boolean hasConduitByType(ConduitType<?> conduitType);

    // TODO: Docs
    Holder<Conduit<?, ?>> getConduitByType(ConduitType<?> conduitType);

    /**
     * @param conduit the conduit to check for
     * @return whether the bundle has this specific conduit.
     */
    boolean hasConduitStrict(Holder<Conduit<?, ?>> conduit);

    /**
     * @return whether the bundle has no conduits and no facade.
     */
    boolean isEmpty();

    /**
     * @return whether the bundle has the maximum number of conduits.
     */
    boolean isFull();

    // region Connections

    /**
     * @implNote Must be sorted according to {@link com.enderio.conduits.api.ConduitApi#getConduitSortIndex(Holder)}
     * @param side the side to check for.
     * @return a list of all conduits connected on this side.
     */
    List<Holder<Conduit<?, ?>>> getConnectedConduits(Direction side);

    /**
     *
     * @param side
     * @param conduit
     * @return
     */
    ConnectionStatus getConnectionStatus(Direction side, Holder<Conduit<?, ?>> conduit);

    /**
     * @param side
     * @param conduit
     * @return
     */
    ConnectionConfig getConnectionConfig(Direction side, Holder<Conduit<?, ?>> conduit);

    /**
     * @param side
     * @param conduit
     * @return
     */
    <T extends ConnectionConfig> T getConnectionConfig(Direction side, Holder<Conduit<?, ?>> conduit,
            ConnectionConfigType<T> type);

    /**
     * An endpoint is a side which has a "connection plate" to another block, rather than to continued line of bundles.
     * @param side the side being checked.
     * @return whether this side is an endpoint.
     */
    boolean isEndpoint(Direction side);

    // endregion

    // region Facades

    /**
     * @return whether the bundle has a facade.
     */
    boolean hasFacade();

    /**
     * @throws IllegalStateException if {@link #hasFacade} is false.
     * @return the block this bundle is mimicing.
     */
    Block getFacadeBlock();

    /**
     * @throws IllegalStateException if {@link #hasFacade} is false.
     * @return the type of facade this bundle has.
     */
    FacadeType getFacadeType();

    /**
     * @return the item providing this bundle's facade.
     */
    ItemStack getFacadeProvider();

    // endregion

}
