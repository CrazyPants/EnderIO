package com.enderio.conduits.api.connection.config.io;

import com.enderio.conduits.api.connection.config.ConnectionConfig;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface IOConnectionConfig extends ConnectionConfig {

    // TODO: canSend/canReceive? Might be clearer when used with Redstone signals?

    boolean canInsert();
    boolean canExtract();

    IOConnectionConfig withInsert(boolean canInsert);
    IOConnectionConfig withExtract(boolean canExtract);

    @Override
    default boolean isConnected() {
        return canInsert() || canExtract();
    }
}
