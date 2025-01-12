package com.enderio.conduits.api.connection.config;

import java.util.List;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

/**
 * Get the list of redstone signal colors that this connection is sensitive to.
 * This is exclusively used for conduit connection rendering.
 */
@ApiStatus.Experimental
public interface RedstoneSensitiveConnectionConfig {
    // TODO: Update this when we support 2 redstone colors.
    /**
     * @apiNote currently the conduit bundle model is only capable of rendering one signal color. In future this will expand to two.
     * @return the redstone signal(s) this conduit is sensitive to.
     */
    List<DyeColor> getRedstoneSignalColors();
}
