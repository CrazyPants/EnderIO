package com.enderio.conduits.api.network.node.legacy;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@Deprecated(forRemoval = true, since = "7.2")
public record ConduitDataType<T extends ConduitData<T>>(MapCodec<T> codec,
        StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, Supplier<T> factory) {
}
