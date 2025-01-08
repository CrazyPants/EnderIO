package com.enderio.conduits.common.init;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.network.ConduitNetworkContextType;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.common.conduit.type.energy.EnergyConduit;
import com.enderio.conduits.common.conduit.type.energy.EnergyConduitNetworkContext;
import com.enderio.conduits.common.conduit.type.fluid.FluidConduit;
import com.enderio.conduits.common.conduit.type.item.ItemConduit;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduit;
import java.util.function.Supplier;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Conduits {

    public static ResourceKey<Conduit<?>> ENERGY = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
            EnderIO.loc("energy"));
    public static ResourceKey<Conduit<?>> ENHANCED_ENERGY = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
            EnderIO.loc("enhanced_energy"));
    public static ResourceKey<Conduit<?>> ENDER_ENERGY = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
            EnderIO.loc("ender_energy"));
    public static ResourceKey<Conduit<?>> REDSTONE = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
            EnderIO.loc("redstone"));
    public static ResourceKey<Conduit<?>> FLUID = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
            EnderIO.loc("fluid"));
    public static ResourceKey<Conduit<?>> PRESSURIZED_FLUID = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
            EnderIO.loc("pressurized_fluid"));
    public static ResourceKey<Conduit<?>> ENDER_FLUID = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
            EnderIO.loc("ender_fluid"));
    public static ResourceKey<Conduit<?>> ITEM = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
            EnderIO.loc("item"));
    public static ResourceKey<Conduit<?>> ENHANCED_ITEM = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
            EnderIO.loc("enhanced_item"));
    public static ResourceKey<Conduit<?>> ENDER_ITEM = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
            EnderIO.loc("ender_item"));

    public static void bootstrap(BootstrapContext<Conduit<?>> context) {
        // TODO: These rates are still up for change, but will refine through testing.
        context.register(ENERGY,
                new EnergyConduit(EnderIO.loc("block/conduit/energy"), ConduitLang.ENERGY_CONDUIT, 1_000));
        context.register(ENHANCED_ENERGY, new EnergyConduit(EnderIO.loc("block/conduit/enhanced_energy"),
                ConduitLang.ENHANCED_ENERGY_CONDUIT, 12_000));
        context.register(ENDER_ENERGY, new EnergyConduit(EnderIO.loc("block/conduit/ender_energy"),
                ConduitLang.ENDER_ENERGY_CONDUIT, 48_000));

        context.register(REDSTONE, new RedstoneConduit(EnderIO.loc("block/conduit/redstone"),
                EnderIO.loc("block/conduit/redstone_active"), ConduitLang.REDSTONE_CONDUIT));

        context.register(FLUID,
                new FluidConduit(EnderIO.loc("block/conduit/fluid"), ConduitLang.FLUID_CONDUIT, 50, false));
        context.register(PRESSURIZED_FLUID, new FluidConduit(EnderIO.loc("block/conduit/pressurized_fluid"),
                ConduitLang.PRESSURIZED_FLUID_CONDUIT, 100, false));
        context.register(ENDER_FLUID, new FluidConduit(EnderIO.loc("block/conduit/ender_fluid"),
                ConduitLang.ENDER_FLUID_CONDUIT, 200, true));

        context.register(ITEM, new ItemConduit(EnderIO.loc("block/conduit/item"), ConduitLang.ITEM_CONDUIT, 4, 20));

        // TODO: Implement the new item conduit tiers.
        // context.register(ENHANCED_ITEM, new
        // ItemConduit(EnderIO.loc("block/conduit/item"),
        // ConduitLang.ENHANCED_ITEM_CONDUIT, 4, 10));
        // context.register(ENDER_ITEM, new
        // ItemConduit(EnderIO.loc("block/conduit/item"),
        // ConduitLang.ENDER_ITEM_CONDUIT, 4, 5));
    }

    public static void register() {
    }
}
