package com.enderio.conduits.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface ColoredRedstoneProvider {
    boolean isRedstoneActive(Level level, BlockPos pos, DyeColor color);
}
