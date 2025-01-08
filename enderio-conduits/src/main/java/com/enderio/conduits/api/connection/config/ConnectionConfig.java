package com.enderio.conduits.api.connection.config;

import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.api.network.node.legacy.ConduitData;
import com.mojang.serialization.Codec;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.ApiStatus;

/**
 * Replacement for {@link ConduitData} that is purely focussed on sided connection context.
 * Any data stored in the entire node should now be stored in the network, an individual node should not have connectionless context.
 */
@ApiStatus.Experimental
public interface ConnectionConfig {

    Codec<ConnectionConfig> GENERIC_CODEC = EnderIOConduitsRegistries.CONDUIT_CONNECTION_CONFIG_TYPE.byNameCodec()
        .dispatch(ConnectionConfig::type, ConnectionConfigType::codec);

    /**
     * @return whether the conduit should still be connected with this configuration.
     */
    default boolean isConnected() {
        return true;
    }

    /**
     * Modify the config such that isConnected() is true again.
     * This will ensure that when the connection is revived, it isn't invalid.
     */
    default ConnectionConfig reconnected() {
        if (this.isConnected()) {
            return this;
        }

        throw new NotImplementedException("This connection config type needs to implement reconnected().");
    }

    ConnectionConfigType<?> type();
}
