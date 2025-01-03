package com.enderio.conduits.api.bundle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.ConduitConnection;
import com.enderio.conduits.api.connection.ConduitConnectionType;
import com.enderio.conduits.api.facade.FacadeType;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

/**
 * Immutable access to a conduit bundle.
 */
@ApiStatus.Experimental
public interface ConduitBundleReader {

    /**
     * @implNote Must be sorted according to {@link com.enderio.conduits.api.ConduitApi#getConduitSortIndex(Holder)}
     * @return a list of all conduits in the bundle.
     */
    List<Holder<Conduit<?>>> getConduits();

    // TEMP

    ConduitGraphObject getConduitNode(Holder<Conduit<?>> conduit);

    /**
     * @implNote compare conduits using {@link Conduit#canConnectTo(Holder)}
     * @param conduit the conduit to check for
     * @return whether the bundle has this conduit, or another which is compatible.
     */
    boolean hasConduitByType(Holder<Conduit<?>> conduit);

    /**
     * @param conduit the conduit to check for
     * @return whether the bundle has this specific conduit.
     */
    boolean hasConduitStrict(Holder<Conduit<?>> conduit);

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
    List<Holder<Conduit<?>>> getConnectedConduits(Direction side);

    /**
     *
     * @param side
     * @param conduit
     * @return
     */
    ConduitConnectionType getConnectionType(Direction side, Holder<Conduit<?>> conduit);

    /**
     * @throws IllegalStateException if {@link #getConnectionType} is not {@link ConduitConnectionType#CONNECTED_BLOCK}.
     * @param side
     * @param conduit
     * @return
     */
    ConduitConnection getConnection(Direction side, Holder<Conduit<?>> conduit);

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
