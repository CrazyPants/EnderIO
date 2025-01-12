package com.enderio.conduits.api.screen;

import com.enderio.conduits.api.connection.config.ConnectionConfig;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@ApiStatus.Experimental
public interface ConduitMenuDataAccess<T extends ConnectionConfig> {
    T getConnectionConfig();
    void updateConnectionConfig(Function<T, T> configModifier);

    @Nullable
    CompoundTag getExtraGuiData();
}
