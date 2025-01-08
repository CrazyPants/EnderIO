package com.enderio.conduits.api.network.node;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface NodeDataAccessor {
    boolean hasNodeData(NodeDataType<?> type);

    /**
     * Get the data attached to this node, with no preferred type.
     * @return The attached data or null if there is no data.
     */
    @Nullable
    NodeData getNodeData();

    /**
     * Get the data attached to this node.
     * @param type The expected data type.
     * @return The attached data or null if there is no data -or- the data is of a different type.
     */
    @Nullable
    <T extends NodeData> T getNodeData(NodeDataType<T> type);

    /**
     * Gets the data attached to this node.
     * @param type The data type that is expected.
     * @return The stored data that matched this type, or a new instance of the data.
     * @implNote If a conduit data of a different type exists in this node, it will be replaced.
     */
    <T extends NodeData> T getOrCreateNodeData(NodeDataType<T> type);

    /**
     * Set the data attached to this node.
     *
     * @param data
     * @param <T>
     */
    <T extends NodeData> void setNodeData(@Nullable T data);
}
