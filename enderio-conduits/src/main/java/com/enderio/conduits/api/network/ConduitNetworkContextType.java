package com.enderio.conduits.api.network;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record ConduitNetworkContextType<T extends ConduitNetworkContext<T>>(@Nullable Codec<T> codec, Supplier<T> factory) {}
