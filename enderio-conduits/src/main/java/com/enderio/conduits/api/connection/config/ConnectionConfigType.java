package com.enderio.conduits.api.connection.config;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ConnectionConfigType<T extends ConnectionConfig>(MapCodec<T> codec,
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, Supplier<T> defaultSupplier) {
    public T getDefault() {
        return defaultSupplier.get();
    }
}
