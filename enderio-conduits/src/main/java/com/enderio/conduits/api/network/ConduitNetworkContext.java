package com.enderio.conduits.api.network;

public interface ConduitNetworkContext<T extends ConduitNetworkContext<T>> {
    T mergeWith(T other);

    T copy();

    ConduitNetworkContextType<T> type();
}
