package com.enderio.conduits.api.screen;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegisterConduitScreenTypesEvent extends Event implements IModBusEvent {
    private final Map<ConduitType<?>, ConduitScreenType<?>> screenTypes = new ConcurrentHashMap<>();

    public void register(ConduitType<? extends Conduit<?, ?>> conduitType, ConduitScreenType<?> screenType) {
        screenTypes.put(conduitType, screenType);
    }

    public Map<ConduitType<?>, ConduitScreenType<?>> getScreenTypes() {
        return Map.copyOf(screenTypes);
    }
}
