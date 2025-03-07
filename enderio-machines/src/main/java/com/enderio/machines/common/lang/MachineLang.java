package com.enderio.machines.common.lang;

import com.enderio.base.api.EnderIO;
import com.enderio.machines.EnderIOMachines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class MachineLang {
    public static final MutableComponent PROGRESS_TOOLTIP = addTranslation("gui", EnderIO.loc("progress"),
            "Progress %s%%");

    public static final Component ALLOY_SMELTER_MODE = addTranslation("gui", EnderIO.loc("alloy_smelter.mode"),
            "Smelting Mode");
    public static final MutableComponent SAG_MILL_GRINDINGBALL_REMAINING = addTranslation("tooltip",
            EnderIO.loc("grinding_ball_remaining"), "Remaining: %s%%");
    public static final MutableComponent SAG_MILL_GRINDINGBALL_TITLE = addTranslation("tooltip",
            EnderIO.loc("grinding_ball_title"), "SAG Mill Grinding Ball");

    public static final Component TOOLTIP_ENERGY_EQUIVALENCE = addTranslation("gui", EnderIO.loc("energy_equivalence"),
            "A unit of energy, equivalent to FE.");
    public static final MutableComponent TOOLTIP_SAG_MILL_CHANCE_BALL = addTranslation("gui",
            EnderIO.loc("sag_mill_chance_ball"), "Chance: %s%% (modified by grinding ball)");
    public static final MutableComponent TOOLTIP_SAG_MILL_CHANCE = addTranslation("gui", EnderIO.loc("sag_mill_chance"),
            "Chance: %s%%");

    public static final MutableComponent TOOLTIP_ACTIVE = addTranslation("gui", EnderIO.loc("active"),
            "The machine is active");
    public static final MutableComponent TOOLTIP_IDLE = addTranslation("gui", EnderIO.loc("idle"),
            "The machine is idle");

    public static final MutableComponent TOOLTIP_NO_CAPACITOR = addTranslation("gui", EnderIO.loc("no_capacitor"),
            "Install a capacitor to be able to use the machine");
    public static final MutableComponent TOOLTIP_NO_POWER = addTranslation("gui", EnderIO.loc("no_power"),
            "There is not enough power to use the machine");
    public static final MutableComponent TOOLTIP_FULL_POWER = addTranslation("gui", EnderIO.loc("full_power"),
            "The energy storage is full");
    public static final MutableComponent TOOLTIP_NO_SOURCE = addTranslation("gui", EnderIO.loc("no_source"),
            "The Drain needs a source block under it to work");
    public static final MutableComponent TOOLTIP_EMPTY_TANK = addTranslation("gui", EnderIO.loc("empty_tank"),
            "The tank is empty");
    public static final MutableComponent TOOLTIP_FULL_TANK = addTranslation("gui", EnderIO.loc("full_tank"),
            "The tank is full");
    public static final MutableComponent TOOLTIP_BLOCKED_RESTONE = addTranslation("gui",
            EnderIO.loc("blocked_redstone"), "The machine is blocked by redstone");
    public static final MutableComponent TOOLTIP_OUTPUT_FULL = addTranslation("gui", EnderIO.loc("output_full"),
            "There is not enough room for the output");
    public static final MutableComponent TOOLTIP_INPUT_EMPTY = addTranslation("gui", EnderIO.loc("input_empty"),
            "There is no item in the input");

    // region JEI Categories

    public static final MutableComponent CATEGORY_ALLOY_SMELTING = addTranslation("gui",
            EnderIO.loc("category.alloy_smelting"), "Alloy Smelting");
    public static final MutableComponent CATEGORY_ENCHANTER = addTranslation("gui", EnderIO.loc("category.enchanter"),
            "Enchanting");
    public static final MutableComponent CATEGORY_PRIMITIVE_ALLOY_SMELTING = addTranslation("gui",
            EnderIO.loc("category.primitive_alloy_smelting"), "Primitive Alloy Smelting");
    public static final MutableComponent CATEGORY_SAG_MILL = addTranslation("gui", EnderIO.loc("category.sag_mill"),
            "SAG Mill");
    public static final MutableComponent CATEGORY_SLICING = addTranslation("gui", EnderIO.loc("category.slicing"),
            "Slicing");
    public static final MutableComponent CATEGORY_SOUL_BINDING = addTranslation("gui",
            EnderIO.loc("category.soul_binding"), "Soul Binding");
    public static final MutableComponent CATEGORY_TANK = addTranslation("gui", EnderIO.loc("category.tank"),
            "Fluid Tank");
    public static final MutableComponent CATEGORY_SOUL_ENGINE = addTranslation("gui",
            EnderIO.loc("category.soul_engine"), "Soul Engine");
    public static final MutableComponent CATEGORY_VAT = addTranslation("gui", EnderIO.loc("category.vat"), "VAT");

    // endregion

    public static final MutableComponent TOO_MANY_MOB = addTranslation("gui", EnderIO.loc("spawner.too_many_mob"),
            "Too many mobs");
    public static final MutableComponent TOO_MANY_SPAWNER = addTranslation("gui",
            EnderIO.loc("spawner.too_many_spawner"), "Too many spawners");
    public static final MutableComponent UNKNOWN = addTranslation("gui", EnderIO.loc("spawner.unknown"), "Unknown mob");
    public static final MutableComponent OTHER_MOD = addTranslation("gui", EnderIO.loc("spawner.other_mod"),
            "Blocked by another mod");
    public static final MutableComponent DISABLED = addTranslation("gui", EnderIO.loc("spawner.disabled"),
            "Disabled by config");
    public static final Component PHOTOVOLTAIC_CELL = addTranslation("tooltip", EnderIO.loc("photovoltaic_cell/main"),
            "Solar Power!");
    public static final Component PHOTOVOLTAIC_CELL_ADVANCED = addTranslation("tooltip",
            EnderIO.loc("photovoltaic_cell/advanced"), "Produces Power during daylight hours");
    public static final Component PHOTOVOLTAIC_CELL_ADVANCED2 = addTranslation("tooltip",
            EnderIO.loc("photovoltaic_cell/advanced2"), "Must have a clear line of sight to the sky");
    public static final MutableComponent PHOTOVOLTAIC_CELL_ADVANCED3 = addTranslation("tooltip",
            EnderIO.loc("photovoltaic_cell/advanced3"), "Max Output: ");
    public static final Component PLACE_CAPACITOR_BANK_ADVANCEMENT_TITLE = addTranslation("advancements",
            EnderIO.loc("place_capacitor_bank.title"), "Modular Power Storage");
    public static final Component PLACE_CAPACITOR_BANK_ADVANCEMENT_DESCRIPTION = addTranslation("advancements",
            EnderIO.loc("place_capacitor_bank.description"), "Build a Capacitor Bank");
    public static final Component MULTIBLOCK_CONNECTED_TEXTURES = addTranslation("hint",
            EnderIO.loc("connected_textures.text"),
            "If you are looking for connected textures on the capacitor bank, you might want to install Athena on your client");
    public static final MutableComponent OBELISK_UPKEEP = addTranslation("gui", EnderIO.loc("obelisk.upkeep_cost"),
            "Upkeep %s \u00B5I/t ");
    public static final MutableComponent GENERATING = addTranslation("gui", EnderIO.loc("generator.generating"),
            "Generating %s \u00B5I/t ");
    public static final MutableComponent FUEL_EFFICIENCY = addTranslation("gui", EnderIO.loc("generator.efficiency"),
            "Efficiency %s%%");
    public static final MutableComponent NO_SOUL_FILTER = addTranslation("gui", EnderIO.loc("obelisk.no_soul_filter"),
            "No Soul Filter Installed");

    // GUI BUTTONS
    public static final Component RETRIEVE_1 = addTranslation("gui", EnderIO.loc("button.retrieve_1_level"),
            "Retrieve 1 level of XP");
    public static final Component RETRIEVE_10 = addTranslation("gui", EnderIO.loc("button.retrieve_10_level"),
            "Retrieve 10 levels of XP");
    public static final Component RETRIEVE_ALL = addTranslation("gui", EnderIO.loc("button.retrieve_all_level"),
            "Retrieve all levels of XP");
    public static final Component STORE_1 = addTranslation("gui", EnderIO.loc("button.store_1_level"),
            "Store 1 level of XP");
    public static final Component STORE_10 = addTranslation("gui", EnderIO.loc("button.store_10_level"),
            "Store 10 levels of XP");
    public static final Component STORE_ALL = addTranslation("gui", EnderIO.loc("button.store_all_level"),
            "Store all levels of XP");
    public static final Component TRANSFER_TANK = addTranslation("gui", EnderIO.loc("button.transfer_tank"),
            "Transfer tank contents");
    public static final Component DUMP_TANK = addTranslation("gui", EnderIO.loc("button.dump_tank"),
            "Void tank contents");

    // TODO: NEO-PORT: Common lang base class?
    private static MutableComponent addTranslation(String prefix, ResourceLocation id, String translation) {
        return EnderIOMachines.REGILITE.addTranslation(prefix, id, translation);
    }

    private static MutableComponent addTranslation(String prefix, ResourceLocation path, String name,
            String translation) {
        return EnderIOMachines.REGILITE.addTranslation(prefix,
                ResourceLocation.fromNamespaceAndPath(path.getNamespace(), path.getPath() + "/" + name), translation);
    }

    public static void register() {
    }
}
