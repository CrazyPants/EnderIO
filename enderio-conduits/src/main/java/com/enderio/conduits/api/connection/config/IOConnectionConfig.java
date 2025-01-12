package com.enderio.conduits.api.connection.config;

import com.enderio.conduits.api.ConduitRedstoneSignalAware;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface IOConnectionConfig extends ConnectionConfig {
    /**
     * @return whether the connection is sending resources to the connected block
     */
    boolean isSend();

    /**
     * @return whether the connection is receiving resources from the connected block
     */
    boolean isReceive();

    /**
     * These colors are used for send separation in the ticker.
     * If no channel separation is required, always return the same colour for any connection.
     * In this scenario, this is now simply the color of the arrow on the model.
     * @return the send color channel.
     */
    DyeColor sendColor();

    /**
     * These colors are used for send separation in the ticker.
     * If no channel separation is required, always return the same colour for any connection.
     * In this scenario, this is now simply the color of the arrow on the model.
     * @return the receive color channel.
     */
    DyeColor receiveColor();

    default boolean canSend(ConduitRedstoneSignalAware signalAware) {
        return isSend();
    }

    default boolean canReceive(ConduitRedstoneSignalAware signalAware) {
        return isReceive();
    }

    @Override
    default boolean isConnected() {
        return isSend() || isReceive();
    }
}
