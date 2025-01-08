package com.enderio.conduits.client.model.conduit.modifier;

import com.enderio.conduits.api.model.ConduitModelModifier;
import net.minecraft.world.item.DyeColor;

public class EnergyConduitModelModifier implements ConduitModelModifier {
    @Override
    public DyeColor getDefaultArrowColor() {
        return DyeColor.RED;
    }
}
