package com.enderio.conduits;

import com.enderio.base.api.EnderIO;
import com.enderio.base.data.EIODataProvider;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.common.init.ConduitBlockEntities;
import com.enderio.conduits.common.init.ConduitBlocks;
import com.enderio.conduits.common.init.ConduitComponents;
import com.enderio.conduits.common.init.ConduitIngredientTypes;
import com.enderio.conduits.common.init.ConduitItems;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.init.ConduitMenus;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.conduits.common.init.Conduits;
import com.enderio.conduits.common.integrations.Integrations;
import com.enderio.conduits.data.ConduitTagProvider;
import com.enderio.conduits.data.recipe.ConduitRecipes;
import com.enderio.regilite.Regilite;
import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@EventBusSubscriber(modid = EnderIOConduits.MODULE_MOD_ID, bus = EventBusSubscriber.Bus.MOD)
@Mod(EnderIOConduits.MODULE_MOD_ID)
public class EnderIOConduits {

    public static final String MODULE_MOD_ID = "enderio_conduits";

    public static Regilite REGILITE = new Regilite(EnderIO.NAMESPACE);

    public EnderIOConduits(IEventBus modEventBus, ModContainer modContainer) {
        Conduits.register();
        ConduitTypes.register(modEventBus);
        ConduitBlockEntities.register(modEventBus);
        ConduitMenus.register(modEventBus);
        ConduitBlocks.register(modEventBus);
        ConduitItems.register(modEventBus);
        ConduitComponents.register(modEventBus);
        ConduitIngredientTypes.register(modEventBus);
        Integrations.register();
        ConduitLang.register();
        REGILITE.register(modEventBus);
    }

    @SubscribeEvent
    public static void onNewRegistries(NewRegistryEvent event) {
        event.register(EnderIOConduitsRegistries.CONDUIT_TYPE);
        event.register(EnderIOConduitsRegistries.CONDUIT_DATA_TYPE);
        event.register(EnderIOConduitsRegistries.CONDUIT_CONNECTION_CONFIG_TYPE);
        event.register(EnderIOConduitsRegistries.CONDUIT_NODE_DATA_TYPE);
        event.register(EnderIOConduitsRegistries.CONDUIT_NETWORK_CONTEXT_TYPE);
    }

    @SubscribeEvent
    private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(EnderIOConduitsRegistries.Keys.CONDUIT, Conduit.DIRECT_CODEC, Conduit.DIRECT_CODEC);
    }

    @SubscribeEvent
    public static void onData(GatherDataEvent event) {
        var pack = event.getGenerator().getVanillaPack(true);
        var registries = event.getLookupProvider();

        var datapackEntriesProvider = pack.addProvider(output -> new DatapackBuiltinEntriesProvider(output, registries,
                createDatapackEntriesBuilder(), Set.of(EnderIO.NAMESPACE, MODULE_MOD_ID)));

        PackOutput packOutput = event.getGenerator().getPackOutput();

        EIODataProvider provider = new EIODataProvider("conduits");

        provider.addSubProvider(event.includeServer(),
                new ConduitTagProvider(packOutput, registries, event.getExistingFileHelper()));
        provider.addSubProvider(event.includeServer(),
                new ConduitRecipes(packOutput, datapackEntriesProvider.getRegistryProvider()));

        event.getGenerator().addProvider(true, provider);
    }

    private static RegistrySetBuilder createDatapackEntriesBuilder() {
        return new RegistrySetBuilder().add(EnderIOConduitsRegistries.Keys.CONDUIT, Conduits::bootstrap);
    }
}
