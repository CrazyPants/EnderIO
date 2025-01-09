package com.enderio.conduits.api;

import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ConduitRedstoneSignalAware {
    /**
     * Whether the block has a redstone signal.
     * @param signalColor if set, will also check for a signal in the attached redstone conduit network.
     * @return whether there is a redstone signal to this block.
     */
    boolean hasRedstoneSignal(@Nullable DyeColor signalColor);
}
