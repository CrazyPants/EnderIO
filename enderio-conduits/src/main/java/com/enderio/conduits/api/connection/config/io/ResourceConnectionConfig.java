package com.enderio.conduits.api.connection.config.io;

import com.enderio.conduits.api.connection.config.ConnectionConfig;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface ResourceConnectionConfig extends ConnectionConfig {
    boolean canInsert();
    boolean canExtract();

    // TODO: A different way to make the arrows a different colour without having this in the interface?
    DyeColor insertChannel();
    DyeColor extractChannel();

    ResourceConnectionConfig withInsert(boolean canInsert);
    ResourceConnectionConfig withExtract(boolean canExtract);

    @Override
    default boolean isConnected() {
        return canInsert() || canExtract();
    }
}
