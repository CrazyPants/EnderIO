package com.enderio.conduits.api.bundle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.ConnectionStatus;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Mutable access to a conduit bundle.
 */
@ApiStatus.Experimental
public interface ConduitBundleAccessor extends ConduitBundleReader {

    boolean canAddConduit(Holder<Conduit<?>> conduit);

    /**
     * Attempt to add a conduit to the bundle.
     * @param conduit the conduit to add
     * @param player the player adding the conduit, or null if performed from another source.
     * @return the result of the add operation.
     */
    AddConduitResult addConduit(Holder<Conduit<?>> conduit, @Nullable Player player);

    /**
     * Remove a conduit from the bundle.
     * @throws IllegalArgumentException if this conduit is not present (in dev only).
     */
    void removeConduit(Holder<Conduit<?>> conduit, @Nullable Player player);

    /**
     * @param conduit the conduit to get the inventory for.
     * @return the inventory for this conduit.
     */
    ConduitInventory getInventory(Holder<Conduit<?>> conduit);

    // region Connections

    /**
     * @throws IllegalStateException if {@link #getConnectionStatus} is not {@link ConnectionStatus#CONNECTED_BLOCK}.
     * @throws IllegalArgumentException if the connection config is not the right type for this conduit.
     * @param side
     * @param config
     */
    void setConnectionConfig(Direction side, Holder<Conduit<?>> conduit, ConnectionConfig config);

    /**
     * Attempt to connect this conduit something in the given direction.
     * @param side the direction to be connected to.
     * @param conduit the conduit type that is being connected.
     * @param isForcedConnection whether this is a forced connection or automated connection. (Wrench)
     * @return whether a new connection was made.
     */
    boolean tryConnectTo(Direction side, Holder<Conduit<?>> conduit, boolean isForcedConnection);

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
