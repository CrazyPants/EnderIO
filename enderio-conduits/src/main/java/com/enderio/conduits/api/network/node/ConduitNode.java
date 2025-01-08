package com.enderio.conduits.api.network.node;

import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.conduits.api.connection.config.ConnectionConfigAccessor;
import com.enderio.conduits.api.network.ConduitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface ConduitNode extends NodeDataAccessor, ConnectionConfigAccessor {
    // TODO: A better way to determine if a node's bundle is loaded.

    /**
     * @throws IllegalStateException if the node is not loaded in the world.
     * @return the world position of the node.
     */
    BlockPos getPos();

    @Nullable
    ResourceFilter getExtractFilter(Direction direction);

    @Nullable
    ResourceFilter getInsertFilter(Direction direction);

    // TODO: investigate nullability for this interface?
    @Nullable ConduitNetwork getNetwork();

    /**
     * @return whether this node's bundle is loaded and ticking in the world
     */
    boolean isLoaded();

    /**
     * Mark the node as dirty, causing the owning bundle to save and sync.
     */
    void markDirty();
}
