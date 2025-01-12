package com.enderio.conduits.client.gui.conduit;

import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.screen.ConduitScreenExtension;
import com.enderio.conduits.api.screen.RegisterConduitScreenExtensionsEvent;
import me.liliandev.ensure.ensures.EnsureSide;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Deprecated(forRemoval = true)
public class ConduitScreenExtensions {
    private static Map<ConduitType<?>, ConduitScreenExtension> EXTENSIONS;

    @EnsureSide(EnsureSide.Side.CLIENT)
    public static void init() {
        var event = new RegisterConduitScreenExtensionsEvent();
        ModLoader.postEvent(event);
        var factories = event.getExtensions();

        EXTENSIONS = new HashMap<>();
        factories.forEach((t, f) -> EXTENSIONS.put(t, f.createExtension()));
    }

    @EnsureSide(EnsureSide.Side.CLIENT)
    @Nullable
    public static ConduitScreenExtension get(ConduitType<?> conduitType) {
        return EXTENSIONS.get(conduitType);
    }
}
