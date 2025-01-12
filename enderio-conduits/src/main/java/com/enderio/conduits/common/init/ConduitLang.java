package com.enderio.conduits.common.init;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.EnderIOConduits;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class ConduitLang {

    // region Conduit Types

    public static final Component ENERGY_CONDUIT = addTranslation("item", EnderIO.loc("conduit.energy"),
            "Energy Conduit");
    public static final Component ENHANCED_ENERGY_CONDUIT = addTranslation("item",
            EnderIO.loc("conduit.enhanced_energy"), "Enhanced Energy Conduit");
    public static final Component ENDER_ENERGY_CONDUIT = addTranslation("item", EnderIO.loc("conduit.ender_energy"),
            "Ender Energy Conduit");
    public static final Component REDSTONE_CONDUIT = addTranslation("item", EnderIO.loc("conduit.redstone"),
            "Redstone Conduit");
    public static final Component FLUID_CONDUIT = addTranslation("item", EnderIO.loc("conduit.fluid"),
            "Fluid Conduit");
    public static final Component PRESSURIZED_FLUID_CONDUIT = addTranslation("item",
            EnderIO.loc("conduit.pressurized_fluid"), "Pressurized Fluid Conduit");
    public static final Component ENDER_FLUID_CONDUIT = addTranslation("item", EnderIO.loc("conduit.ender_fluid"),
            "Ender Fluid Conduit");
    public static final Component ITEM_CONDUIT = addTranslation("item", EnderIO.loc("conduit.item"),
            "Item Conduit");
    public static final Component ENHANCED_ITEM_CONDUIT = addTranslation("item",
            EnderIO.loc("conduit.enhanced_item"), "Enhanced Item Conduit");
    public static final Component ENDER_ITEM_CONDUIT = addTranslation("item", EnderIO.loc("conduit.ender_item"),
            "Ender Item Conduit");

    // endregion

    // region Conduit Screen Tooltips

    public static final Component CONDUIT_CHANNEL = addTranslation("gui", EnderIO.loc("conduit_channel"),
        "Channel");
    public static final Component REDSTONE_CHANNEL = addTranslation("gui", EnderIO.loc("redstone_channel"),
        "Signal Color");

    public static final Component ROUND_ROBIN_ENABLED = addTranslation("gui", EnderIO.loc("round_robin.enabled"),
        "Round Robin Enabled");
    public static final Component ROUND_ROBIN_DISABLED = addTranslation("gui", EnderIO.loc("round_robin.disabled"),
        "Round Robin Disabled");
    public static final Component SELF_FEED_ENABLED = addTranslation("gui", EnderIO.loc("self_feed.enabled"),
        "Self Feed Enabled");
    public static final Component SELF_FEED_DISABLED = addTranslation("gui", EnderIO.loc("self_feed.disabled"),
        "Self Feed Disabled");
    public static final Component FLUID_CONDUIT_CHANGE_FLUID1 = addTranslation("gui",
        EnderIO.loc("fluid_conduit.change_fluid1"), "Locked Fluid:");
    public static final Component FLUID_CONDUIT_CHANGE_FLUID2 = addTranslation("gui",
        EnderIO.loc("fluid_conduit.change_fluid2"), "Click to reset!");
    public static final MutableComponent FLUID_CONDUIT_CHANGE_FLUID3 = addTranslation("gui",
        EnderIO.loc("fluid_conduit.change_fluid3"), "Fluid: %s");

    // endregion

    public static final MutableComponent GRAPH_TICK_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.debug.tick_rate"), "Graph Ticks: %s/sec");

    public static final MutableComponent ENERGY_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.energy.rate"), "Max Output %s \u00B5I/t");

    public static final MutableComponent FLUID_RAW_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.fluid.raw_rate"), "Rate: %s mB/graph tick");
    public static final MutableComponent FLUID_EFFECTIVE_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.fluid.effective_rate"), "Effective Rate: %s mB/t");

    public static final Component MULTI_FLUID_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.fluid.multi"), "Allows multiple fluids to be transported on the same line");

    public static final MutableComponent ITEM_RAW_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.item.raw_rate"), "Rate: %s Items/graph tick");
    public static final MutableComponent ITEM_EFFECTIVE_RATE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit.item.effective_rate"), "Effective Rate: %s Items/sec");

    public static final Component CONDUIT_ERROR_NO_SCREEN_TYPE = addTranslation("gui", EnderIO.loc("conduit.error.no_screen_type"),
        "Error: No screen type defined");

    public static final Component CONDUIT_ENABLED = addTranslation("gui", EnderIO.loc("conduit.enabled"), "Enabled");
    public static final Component CONDUIT_INSERT = addTranslation("gui", EnderIO.loc("conduit.insert"), "Insert");
    public static final Component CONDUIT_EXTRACT = addTranslation("gui", EnderIO.loc("conduit.extract"),
            "Extract");
    public static final Component CONDUIT_INPUT = addTranslation("gui", EnderIO.loc("conduit.input"), "Input");
    public static final Component CONDUIT_OUTPUT = addTranslation("gui", EnderIO.loc("conduit.output"),
            "Output");

    // Redstone Conduit
    public static final Component CONDUIT_REDSTONE_SIGNAL_COLOR = addTranslation("gui", EnderIO.loc("conduit.redstone.signal_color"),
            "Signal Color");
    public static final Component CONDUIT_REDSTONE_STRONG_SIGNAL = addTranslation("gui", EnderIO.loc("conduit.redstone.strong_signal"),
            "Strong Signal");

    public static final MutableComponent TRANSPARENT_FACADE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit_facade.transparent"),
            "Transparent: Hides conduits when painted with a translucent block");
    public static final MutableComponent BLAST_RESIST_FACADE_TOOLTIP = addTranslation("tooltip",
            EnderIO.loc("conduit_facade.blast_resist"), "Hardened: Resists breaking and explosions");

    private static MutableComponent addTranslation(String prefix, ResourceLocation id, String translation) {
        return EnderIOConduits.REGILITE.addTranslation(prefix, id, translation);
    }

    private static MutableComponent addTranslation(String prefix, ResourceLocation path, String name,
            String translation) {
        return EnderIOConduits.REGILITE.addTranslation(prefix,
                ResourceLocation.fromNamespaceAndPath(path.getNamespace(), path.getPath() + "/" + name), translation);
    }

    public static void register() {
    }
}
