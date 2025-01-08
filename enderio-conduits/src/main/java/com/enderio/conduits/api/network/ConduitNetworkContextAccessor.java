package com.enderio.conduits.api.network;

import org.jetbrains.annotations.Nullable;

public interface ConduitNetworkContextAccessor {
    boolean hasContext(ConduitNetworkContextType<?> type);

    @Nullable
    <T extends ConduitNetworkContext<T>> T getContext(ConduitNetworkContextType<T> type);

    <T extends ConduitNetworkContext<T>> T getOrCreateContext(ConduitNetworkContextType<T> type);
}
