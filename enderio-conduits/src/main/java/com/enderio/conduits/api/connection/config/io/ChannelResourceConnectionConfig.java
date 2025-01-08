package com.enderio.conduits.api.connection.config.io;

import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface ChannelResourceConnectionConfig extends ResourceConnectionConfig {
    ChannelResourceConnectionConfig withInputChannel(DyeColor inputChannel);
    ChannelResourceConnectionConfig withOutputChannel(DyeColor outputChannel);
}
