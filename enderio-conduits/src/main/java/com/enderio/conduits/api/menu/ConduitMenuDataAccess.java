package com.enderio.conduits.api.menu;

import com.enderio.conduits.api.connection.config.ConnectionConfig;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

@ApiStatus.Experimental
public interface ConduitMenuDataAccess<T extends ConnectionConfig> {
    T getConnectionConfig();
    void updateConnectionConfig(Function<T, T> configModifier);

    CompoundTag getClientDataTag();
}
