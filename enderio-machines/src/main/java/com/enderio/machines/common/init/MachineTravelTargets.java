package com.enderio.machines.common.init;

import com.enderio.base.api.EnderIO;
import com.enderio.base.api.registry.EnderIORegistries;
import com.enderio.base.api.travel.TravelTarget;
import com.enderio.base.api.travel.TravelTargetSerializer;
import com.enderio.base.api.travel.TravelTargetType;
import com.enderio.machines.common.travel.AnchorTravelTarget;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MachineTravelTargets {
    public static final DeferredRegister<TravelTargetType<?>> TRAVEL_TARGET_TYPES = DeferredRegister
            .create(EnderIORegistries.TRAVEL_TARGET_TYPES, EnderIO.NAMESPACE);

    public static final DeferredRegister<TravelTargetSerializer<?>> TRAVEL_TARGET_SERIALIZERS = DeferredRegister
            .create(EnderIORegistries.TRAVEL_TARGET_SERIALIZERS, EnderIO.NAMESPACE);

    public static final Supplier<TravelTargetType<AnchorTravelTarget>> TRAVEL_ANCHOR_TYPE = registerType(
            "travel_anchor");
    public static final Supplier<TravelTargetSerializer<AnchorTravelTarget>> TRAVEL_ANCHOR_SERIALIZER = TRAVEL_TARGET_SERIALIZERS
            .register("travel_anchor", AnchorTravelTarget.Serializer::new);

    private static <T extends TravelTarget> Supplier<TravelTargetType<T>> registerType(String name) {
        return TRAVEL_TARGET_TYPES.register(name, TravelTargetType::simple);
    }

    public static void register(IEventBus bus) {
        TRAVEL_TARGET_TYPES.register(bus);
        TRAVEL_TARGET_SERIALIZERS.register(bus);
    }
}
