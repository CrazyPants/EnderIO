package com.enderio.conduits.api.screen;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface ConduitMenuDataAccess<T extends ConnectionConfig> {
    Conduit<?, T> conduit();

    BlockPos getBlockPos();

    T getConnectionConfig();

    void updateConnectionConfig(Function<T, T> configModifier);

    @Nullable
    CompoundTag getExtraGuiData();
}
