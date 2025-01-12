package com.enderio.conduits.api.connection.config.io;

import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface ChanneledIOConnectionConfig extends IOConnectionConfig {
    DyeColor insertChannel();

    DyeColor extractChannel();

    ChanneledIOConnectionConfig withInputChannel(DyeColor inputChannel);

    ChanneledIOConnectionConfig withOutputChannel(DyeColor outputChannel);
}
