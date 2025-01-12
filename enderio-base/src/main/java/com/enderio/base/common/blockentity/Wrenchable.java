package com.enderio.base.common.blockentity;

import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.context.UseOnContext;

// TODO: Move to API.

/**
 * An interface that block entities may implement in order to implement special behaviours(other than to rotate the block) when right-clicked with the Yeta wrench.
 */
public interface Wrenchable {
    ItemInteractionResult onWrenched(UseOnContext context);
}
