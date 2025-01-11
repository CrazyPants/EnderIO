package com.enderio.conduits.client.gui.screen.types;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.screen.ConduitScreenType;
import com.enderio.conduits.common.init.ConduitTypes;
import me.liliandev.ensure.ensures.EnsureSide;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ConduitScreenTypes {
    private static Map<ConduitType<?>, ConduitScreenType<?>> SCREEN_TYPES;

    @EnsureSide(EnsureSide.Side.CLIENT)
    public static void init() {
        SCREEN_TYPES = Map.of(
            ConduitTypes.ITEM.get(), new ItemConduitScreenType(),
            ConduitTypes.REDSTONE.get(), new RedstoneConduitScreenType()
        );
    }

    @EnsureSide(EnsureSide.Side.CLIENT)
    @Nullable
    public static <T extends Conduit<T, U>, U extends ConnectionConfig> ConduitScreenType<U> get(ConduitType<T> conduitType) {
        //noinspection unchecked
        return (ConduitScreenType<U>) SCREEN_TYPES.get(conduitType);
    }
}
