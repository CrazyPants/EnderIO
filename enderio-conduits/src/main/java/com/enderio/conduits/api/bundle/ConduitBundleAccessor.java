package com.enderio.conduits.api.bundle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.common.conduit.RightClickAction;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
@ApiStatus.Experimental
public interface ConduitBundleAccessor extends ConduitBundleReader {

    // TODO: RightClickAction should have a better name. Maybe AddConduitResult

    boolean canAddConduit(Holder<Conduit<?>> conduit);

    /**
     *
     * @return
     */
    RightClickAction addConduit(Holder<Conduit<?>> conduit, @Nullable Player player);

    /**
     * Remove a conduit from the bundle.
     * @throws IllegalArgumentException if this conduit is not present (in dev only).
     */
    void removeConduit(Holder<Conduit<?>> conduit, @Nullable Player player);

    // region Connections

    // TODO: Should connections be accessible by API?
    // Answer: probably not lol.

    /**
     * Attempt to connect this conduit something in the given direction.
     * @param side the direction to be connected to.
     * @param conduit the conduit type that is being connected.
     * @param isForcedConnection whether this is a forced connection or automated connection. (Wrench)
     * @return whether a new connection was made.
     */
    boolean tryConnectTo(Direction side, Holder<Conduit<?>> conduit, boolean isForcedConnection);

//    void connectTo(Direction side, Holder<Conduit<?>> conduit);

//    void disconnect(Direction side, Holder<Conduit<?>> conduit);

//    void disableConnection(Direction side, Holder<Conduit<?>> conduit);

    // endregion

    // region Facades

    /**
     * Set the facade provider for this bundle.
     * @apiNote The item must have an exposed {@link com.enderio.conduits.api.facade.ConduitFacadeProvider} capability.
     * @param providerStack the stack providing the facade.
     */
    void setFacadeProvider(ItemStack providerStack);

    /**
     * Remove the facade from the bundle.
     */
    void clearFacade();

    // endregion

}
