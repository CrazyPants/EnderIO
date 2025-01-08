package com.enderio.conduits.api.connection.config;

import com.enderio.conduits.api.connection.config.io.ChannelResourceConnectionConfig;
import com.enderio.conduits.api.connection.config.io.ResourceConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public record ConnectionConfigType<T extends ConnectionConfig>(Class<T> clazz, MapCodec<T> codec, Supplier<T> defaultSupplier) {
    public ConnectionConfigType {

    }

    public T getDefault() {
        return defaultSupplier.get();
    }

    public boolean supportsIO() {
        return ResourceConnectionConfig.class.isAssignableFrom(clazz);
    }

    public boolean supportsIOChannels() {
        return ChannelResourceConnectionConfig.class.isAssignableFrom(clazz);
    }

    public boolean supportsRedstoneControl() {
        return RedstoneControlledConnection.class.isAssignableFrom(clazz);
    }
}
