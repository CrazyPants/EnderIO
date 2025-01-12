package com.enderio.conduits.api.network;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public record ConduitNetworkContextType<T extends ConduitNetworkContext<T>>(@Nullable Codec<T> codec,
        Supplier<T> factory) {
}
