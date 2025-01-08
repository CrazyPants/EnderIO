package com.enderio.conduits.api.connection.config;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;

/**
 * Provides access to connection configuration.
 * Required to handle the potential of a conduit changing type and the data not being applicable anymore.
 */
@ApiStatus.Experimental
public interface ConnectionConfigAccessor {

    /**
     * @param side the side to query.
     * @return whether there is a connection on the given side.
     */
    boolean isConnectedTo(Direction side);

    /**
     * Get the connection config for the given side (generic).
     *
     * @param side the side to query.
     * @return the connection config.
     * @throws IllegalArgumentException if there is no connection on this side.
     */
    ConnectionConfig getConnectionConfig(Direction side);

    /**
     * Get the connection config for the given side (specific).
     *
     * @param side the side to query.
     * @param type the type of connection config to get.
     * @return the connection config.
     * @throws IllegalArgumentException if there is no connection on this side.
     * @throws IllegalStateException if the connection type does not match the requested type.
     */
    <T extends ConnectionConfig> T getConnectionConfig(Direction side, ConnectionConfigType<T> type);

    /**
     * Set the connection config for the given side.
     * @param side the side to set.
     * @param config the config to set.
     * @throws IllegalArgumentException if there is no connection on this side or if the connection config is not the right type for this conduit.
     */
    void setConnectionConfig(Direction side, ConnectionConfig config);
}
