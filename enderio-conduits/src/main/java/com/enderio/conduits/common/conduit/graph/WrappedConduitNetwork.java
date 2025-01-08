package com.enderio.conduits.common.conduit.graph;

import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.ConduitNetworkContext;
import com.enderio.conduits.api.network.ConduitNetworkContextType;
import com.enderio.conduits.api.network.node.ConduitNode;
import dev.gigaherz.graph3.Graph;
import java.util.Collection;
import org.jetbrains.annotations.Nullable;

/**
 * Wrap the graph for public API consumption.
 */
public record WrappedConduitNetwork(Graph<ConduitGraphContext> graph) implements ConduitNetwork {

    @Override
    public Collection<ConduitNode> getNodes() {
        // noinspection unchecked
        return graph.getObjects().stream().map(object -> (ConduitNode) object).toList();
    }

    @Override
    public boolean hasContext(ConduitNetworkContextType<?> type) {
        return graph.getContextData().hasContext(type);
    }

    @Override
    @Nullable
    public <T extends ConduitNetworkContext<T>> T getContext(ConduitNetworkContextType<T> type) {
        return graph.getContextData().getContext(type);
    }

    @Override
    public <T extends ConduitNetworkContext<T>> T getOrCreateContext(ConduitNetworkContextType<T> type) {
        return graph.getContextData().getOrCreateContext(type);
    }
}
