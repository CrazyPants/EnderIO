package com.enderio.conduits.api;

import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.api.misc.RedstoneControl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface ConduitNode extends ConduitDataAccessor {
    Optional<IOState> getIOState(Direction direction);
    BlockPos getPos();

    @Nullable
    ResourceFilter getExtractFilter(Direction direction);

    @Nullable
    ResourceFilter getInsertFilter(Direction direction);

    @Nullable
    ConduitNetwork getParentGraph();

    record IOState(Optional<DyeColor> insert, Optional<DyeColor> extract, RedstoneControl control, DyeColor redstoneChannel) {

        public boolean isInsert() {
            return insert().isPresent();
        }

        public boolean isExtract() {
            return extract().isPresent();
        }

        public static IOState of(@Nullable DyeColor in, @Nullable DyeColor extract, RedstoneControl control, DyeColor redstoneChannel) {
            return new IOState(Optional.ofNullable(in), Optional.ofNullable(extract), control, redstoneChannel);
        }
    }
}
