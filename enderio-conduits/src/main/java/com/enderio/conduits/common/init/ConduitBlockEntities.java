package com.enderio.conduits.common.init;

import com.enderio.conduits.EnderIOConduits;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.common.conduit.bundle.NewConduitBundleBlockEntity;
import com.enderio.regilite.holder.RegiliteBlockEntity;
import com.enderio.regilite.registry.BlockEntityRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = EnderIOConduits.MODULE_MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ConduitBlockEntities {
    private static final BlockEntityRegistry BLOCK_ENTITY_REGISTRY = EnderIOConduits.REGILITE.blockEntityRegistry();

    public static final RegiliteBlockEntity<NewConduitBundleBlockEntity> CONDUIT = BLOCK_ENTITY_REGISTRY
            .registerBlockEntity("conduit", NewConduitBundleBlockEntity::new, ConduitBlocks.CONDUIT);

    @SubscribeEvent
    public static void registerConduitCapabilities(RegisterCapabilitiesEvent event) {
        EnderIOConduitsRegistries.CONDUIT_TYPE.entrySet()
                .stream()
                .flatMap(e -> e.getValue().exposedCapabilities().stream())
                .forEach(e -> registerConduitCapability(event, e));
    }

    private static <TCap, TContext> void registerConduitCapability(RegisterCapabilitiesEvent event,
            BlockCapability<TCap, TContext> capability) {
        event.registerBlockEntity(capability, CONDUIT.get(),
                NewConduitBundleBlockEntity.createCapabilityProvider(capability));
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITY_REGISTRY.register(bus);
    }
}
