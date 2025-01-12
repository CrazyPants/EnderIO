package com.enderio.conduits.client.gui.screen.types;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.screen.ConduitScreenType;
import com.enderio.conduits.api.screen.RegisterConduitScreenTypesEvent;
import com.enderio.conduits.common.init.ConduitTypes;
import me.liliandev.ensure.ensures.EnsureSide;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ConduitScreenTypes {
    private static Map<ConduitType<?>, ConduitScreenType<?>> SCREEN_TYPES;

    @EnsureSide(EnsureSide.Side.CLIENT)
    public static void init() {
        var event = new RegisterConduitScreenTypesEvent();
        ModLoader.postEvent(event);
        SCREEN_TYPES = Map.copyOf(event.getScreenTypes());
    }

    @EnsureSide(EnsureSide.Side.CLIENT)
    @Nullable
    public static <T extends Conduit<T, U>, U extends ConnectionConfig> ConduitScreenType<U> get(ConduitType<T> conduitType) {
        //noinspection unchecked
        return (ConduitScreenType<U>) SCREEN_TYPES.get(conduitType);
    }
}
