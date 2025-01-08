package com.enderio.conduits.api.connection.config.signal;

import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.io.ResourceConnectionConfig;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface SignalConnectionConfig extends ConnectionConfig {
    /**
     * Whether this connection can receive signals.
     */
    boolean canReceive();

    /**
     * Whether this connection can emit signals
     */
    boolean canEmit();

    DyeColor receiveChannel();
    DyeColor emitChannel();

    ResourceConnectionConfig withReceive(boolean canReceive);
    ResourceConnectionConfig withEmit(boolean canEmit);

    @Override
    default boolean isConnected() {
        return canReceive() && canEmit();
    }
}
