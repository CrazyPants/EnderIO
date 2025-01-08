package com.enderio.conduits.common.conduit;

import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.EnderIOConduits;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to sort conduit types for display.
 * This is needed, so upgrading conduits doesn't require shifting of types, but just recalculating the current connection
 */
@SuppressWarnings("unused")
@EventBusSubscriber(modid = EnderIOConduits.MODULE_MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ConduitSorter {
    private static final List<Holder<Conduit<?, ?>>> SORTED_CONDUITS = new ArrayList<>();

    @SubscribeEvent
    public static void serverSortTypes(ServerStartedEvent event) {
        var conduitRegistry = event.getServer().registryAccess().registryOrThrow(EnderIOConduitsRegistries.Keys.CONDUIT);
        sortTypes(conduitRegistry);
    }

    @SubscribeEvent
    public static void clientSortTypes(ClientPlayerNetworkEvent.LoggingIn event) {
        var conduitRegistry = event.getPlayer().registryAccess().registryOrThrow(EnderIOConduitsRegistries.Keys.CONDUIT);
        sortTypes(conduitRegistry);
    }

    private static void sortTypes(Registry<Conduit<?, ?>> registry) {
        SORTED_CONDUITS.clear();

        // Group like types together.
        List<ConduitType<?>> conduitTypes = EnderIOConduitsRegistries.CONDUIT_TYPE
            .stream()
            .sorted(Comparator.comparing(i -> Objects.requireNonNull(EnderIOConduitsRegistries.CONDUIT_TYPE.getKey(i)).toString()))
            .toList();

        List<Holder<Conduit<?, ?>>> sortedConduits = new ArrayList<>();
        for (ConduitType<?> conduitType : conduitTypes) {
             sortedConduits.addAll(gatherConduitsForType(registry, conduitType));
        }
        SORTED_CONDUITS.addAll(sortedConduits);
    }

    private static <T extends Conduit<T, ?>> List<Holder<Conduit<?, ?>>> gatherConduitsForType(Registry<Conduit<?, ?>> registry, ConduitType<T> conduitType) {
        return registry.holders()
            .filter(i -> i.value().type() == conduitType)
            // Group by tier, then by name
            .sorted(new Comparator<Holder<Conduit<?, ?>>>() {
                @Override
                public int compare(Holder<Conduit<?, ?>> o1, Holder<Conduit<?, ?>> o2) {
                     return ((T)o1.value()).compareTo((T)o2.value());
                }
            }.thenComparing(Holder::getRegisteredName))
            .map(i -> (Holder<Conduit<?, ?>>)i)
            .toList();
    }

    public static int getSortIndex(Holder<Conduit<?, ?>> conduit) {
        return SORTED_CONDUITS.indexOf(conduit);
    }
}
