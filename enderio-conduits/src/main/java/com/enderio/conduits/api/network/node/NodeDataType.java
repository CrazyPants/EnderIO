package com.enderio.conduits.api.network.node;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;

public record NodeDataType<T extends NodeData>(MapCodec<T> codec, Supplier<T> factory) {
}
