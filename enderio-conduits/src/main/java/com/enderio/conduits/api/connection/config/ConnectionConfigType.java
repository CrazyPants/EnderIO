package com.enderio.conduits.api.connection.config;

import com.enderio.conduits.api.connection.config.io.ChanneledIOConnectionConfig;
import com.enderio.conduits.api.connection.config.io.IOConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public record ConnectionConfigType<T extends ConnectionConfig>(
    Class<T> clazz,
    MapCodec<T> codec,
    StreamCodec<RegistryFriendlyByteBuf, T> streamCodec,
    Supplier<T> defaultSupplier
) {
    public T getDefault() {
        return defaultSupplier.get();
    }

    @Deprecated
    public boolean supportsIO() {
        return IOConnectionConfig.class.isAssignableFrom(clazz);
    }

    @Deprecated
    public boolean supportsIOChannels() {
        return ChanneledIOConnectionConfig.class.isAssignableFrom(clazz);
    }

    @Deprecated
    public boolean supportsRedstoneControl() {
        return RedstoneControlledConnection.class.isAssignableFrom(clazz);
    }
}
