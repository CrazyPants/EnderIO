package com.enderio.conduits.api.connection.config.signal;

import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface ChannelSignalConnectionConfig extends SignalConnectionConfig {
    ChannelSignalConnectionConfig withInputChannel(DyeColor inputChannel);
    ChannelSignalConnectionConfig withOutputChannel(DyeColor outputChannel);
}
