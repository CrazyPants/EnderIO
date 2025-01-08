package com.enderio.conduits.api.network;

import com.enderio.conduits.api.network.node.ConduitNode;

import java.util.Collection;

public interface ConduitNetwork extends ConduitNetworkContextAccessor {
    Collection<ConduitNode> getNodes();
}
