package com.enderio.conduits.api.connection.config.redstone;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface RedstoneControlledConnection extends ConnectionConfig {
    RedstoneControl redstoneControl();
    DyeColor redstoneChannel();

    RedstoneControlledConnection withRedstoneControl(RedstoneControl redstoneControl);
    RedstoneControlledConnection withRedstoneChannel(DyeColor redstoneChannel);
}
