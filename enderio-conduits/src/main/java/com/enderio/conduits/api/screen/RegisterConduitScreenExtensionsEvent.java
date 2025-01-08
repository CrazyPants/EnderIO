package com.enderio.conduits.api.screen;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterConduitScreenExtensionsEvent extends Event implements IModBusEvent {
    public interface ConduitScreenExtensionFactory {
        ConduitScreenExtension createExtension();
    }

    private final Map<ConduitType<?>, ConduitScreenExtensionFactory> extensions = new ConcurrentHashMap<>();

    public void register(ConduitType<? extends Conduit<?, ?>> conduitType, ConduitScreenExtensionFactory extensionFactory) {
        extensions.put(conduitType, extensionFactory);
    }

    public Map<ConduitType<?>, ConduitScreenExtensionFactory> getExtensions() {
        return Map.copyOf(extensions);
    }
}
