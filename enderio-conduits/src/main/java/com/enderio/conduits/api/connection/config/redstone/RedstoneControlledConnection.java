package com.enderio.conduits.api.connection.config.redstone;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

/**
 * Used to represent a connection that can be controlled by redstone.
 * This will cause {@link com.enderio.conduits.api.network.node.ConduitNode#isActive(Direction)} to return false when the redstone condition is not met.
 */
@ApiStatus.Experimental
public interface RedstoneControlledConnection extends ConnectionConfig {
    RedstoneControl redstoneControl();
    DyeColor redstoneChannel();

    RedstoneControlledConnection withRedstoneControl(RedstoneControl redstoneControl);
    RedstoneControlledConnection withRedstoneChannel(DyeColor redstoneChannel);
}
