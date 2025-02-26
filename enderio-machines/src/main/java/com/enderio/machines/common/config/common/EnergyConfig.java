package com.enderio.machines.common.config.common;

import net.neoforged.neoforge.common.ModConfigSpec;

public class EnergyConfig {
    public final ModConfigSpec.BooleanValue THROTTLE_ENERGY_INPUT;

    public final ModConfigSpec.ConfigValue<Integer> ALLOY_SMELTER_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> ALLOY_SMELTER_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> ALLOY_SMELTER_VANILLA_ITEM_ENERGY;
    public final ModConfigSpec.ConfigValue<Integer> CRAFTER_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> CRAFTER_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> CRAFTING_RECIPE_COST;
    public final ModConfigSpec.ConfigValue<Integer> IMPULSE_HOPPER_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> IMPULSE_HOPPER_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> POWERED_SPAWNER_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> POWERED_SPAWNER_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> SAG_MILL_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> SAG_MILL_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> SLICER_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> SLICER_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> SOUL_BINDER_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> SOUL_BINDER_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> STIRLING_GENERATOR_CAPACITY;
    public final ModConfigSpec.ConfigValue<Double> STIRLING_GENERATOR_BURN_SPEED;
    public final ModConfigSpec.ConfigValue<Integer> STIRLING_GENERATOR_FUEL_EFFICIENCY_BASE;
    public final ModConfigSpec.ConfigValue<Integer> STIRLING_GENERATOR_FUEL_EFFICIENCY_STEP;
    public final ModConfigSpec.ConfigValue<Integer> STIRLING_GENERATOR_PRODUCTION;
    public final ModConfigSpec.ConfigValue<Integer> PAINTING_MACHINE_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> PAINTING_MACHINE_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> PAINTING_MACHINE_ENERGY_COST;
    public final ModConfigSpec.ConfigValue<Integer> ENERGETIC_SOLAR_PANEL_MAX_PRODUCTION;
    public final ModConfigSpec.ConfigValue<Integer> PULSATING_SOLAR_PANEL_MAX_PRODUCTION;
    public final ModConfigSpec.ConfigValue<Integer> VIBRANT_SOLAR_PANEL_MAX_PRODUCTION;
    public final ModConfigSpec.ConfigValue<Integer> BASIC_CAPACITOR_BANK_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> ADVANCED_CAPACITOR_BANK_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> VIBRANT_CAPACITOR_BANK_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> WIRED_CHARGER_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> WIRED_CHARGER_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> WIRELESS_CHARGER_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> WIRELESS_CHARGER_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> WIRELESS_CHARGER_UPKEEP;
    public final ModConfigSpec.ConfigValue<Integer> SOUL_ENGINE_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> SOUL_ENGINE_BURN_SPEED;
    public final ModConfigSpec.ConfigValue<Double> SOUL_ENGINE_GENERATION;
    public final ModConfigSpec.ConfigValue<Integer> DRAIN_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> DRAIN_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> INHIBITOR_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> INHIBITOR_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> AVERSION_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> AVERSION_USAGE;
    public final ModConfigSpec.ConfigValue<Integer> RELOCATOR_CAPACITY;
    public final ModConfigSpec.ConfigValue<Integer> RELOCATOR_USAGE;

    public EnergyConfig(ModConfigSpec.Builder builder) {
        builder.push("energy");

        THROTTLE_ENERGY_INPUT = builder
                .comment("Whether or not the machine should throttle energy input to 2x it's consumption rate")
                .define("throttleEnergyUsage", true);

        builder.push("alloySmelter");
        ALLOY_SMELTER_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 64_000, 1, Integer.MAX_VALUE);
        ALLOY_SMELTER_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 20, 1, Integer.MAX_VALUE);

        // coal burn time = 1600
        // expected burn rate = 0.375
        // coal -> fe/t = 40
        // number of items per coal = 16
        // 1600 * 0.375 * 40 / 16
        ALLOY_SMELTER_VANILLA_ITEM_ENERGY = builder
                .comment("The amount of energy to consume per vanilla smelting item in uI.")
                .defineInRange("vanillaItemEnergy", 1_500, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("crafter");
        CRAFTER_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 64_000, 1, Integer.MAX_VALUE);
        CRAFTER_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 80, 1, Integer.MAX_VALUE);
        CRAFTING_RECIPE_COST = builder.comment("The energy cost in uI for a crafting recipe.")
                .defineInRange("usage", 3_200, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("impulseHopper");
        IMPULSE_HOPPER_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 64_000, 1, Integer.MAX_VALUE);
        IMPULSE_HOPPER_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 16, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("poweredSpawner");
        POWERED_SPAWNER_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 100_000, 1, Integer.MAX_VALUE);
        POWERED_SPAWNER_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 20, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("sagMill");
        SAG_MILL_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 64_000, 1, Integer.MAX_VALUE);
        SAG_MILL_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 20, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("slicer");
        SLICER_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 100_000, 1, Integer.MAX_VALUE);
        SLICER_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 30, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("soulBinder");
        SOUL_BINDER_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 128_000, 1, Integer.MAX_VALUE);
        SOUL_BINDER_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 60, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("stirlingGenerator");
        STIRLING_GENERATOR_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 64_000, 1, Integer.MAX_VALUE);
        STIRLING_GENERATOR_BURN_SPEED = builder.comment("The base number of 'burn ticks' performed per machine tick.")
                .defineInRange("burnSpeed", 0.375d, 0.001d, Double.MAX_VALUE);
        STIRLING_GENERATOR_FUEL_EFFICIENCY_BASE = builder
                .comment("The base percentage efficiency, used to determine burn times.")
                .defineInRange("fuelEfficiencyBase", 80, 1, 200);
        STIRLING_GENERATOR_FUEL_EFFICIENCY_STEP = builder.comment("The efficiency increase per capacitor level.")
                .defineInRange("fuelEfficiencyStep", 20, 1, 200);
        STIRLING_GENERATOR_PRODUCTION = builder.comment("The base amount of energy produced in uI/t.")
                .defineInRange("generation", 40, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("paintingMachine");
        PAINTING_MACHINE_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 64_000, 1, Integer.MAX_VALUE);
        PAINTING_MACHINE_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 30, 1, Integer.MAX_VALUE);
        PAINTING_MACHINE_ENERGY_COST = builder.comment("The energy required for each painting operation")
                .defineInRange("energyCost", 2_400, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("photovoltaicCellRates");
        builder.comment("Production rate at midday without rain or thunder");
        ENERGETIC_SOLAR_PANEL_MAX_PRODUCTION = builder.defineInRange("energetic", 4, 1, Integer.MAX_VALUE);
        PULSATING_SOLAR_PANEL_MAX_PRODUCTION = builder.defineInRange("pulsating", 16, 1, Integer.MAX_VALUE);
        VIBRANT_SOLAR_PANEL_MAX_PRODUCTION = builder.defineInRange("vibrant", 64, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("capacitorBankCapacity");
        builder.comment("Capacity for capacitor banks");
        BASIC_CAPACITOR_BANK_CAPACITY = builder.defineInRange("basic", 500_000, 1, Integer.MAX_VALUE);
        ADVANCED_CAPACITOR_BANK_CAPACITY = builder.defineInRange("advanced", 2_000_000, 1, Integer.MAX_VALUE);
        VIBRANT_CAPACITOR_BANK_CAPACITY = builder.defineInRange("vibrant", 4_000_000, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("wiredCharger");
        WIRED_CHARGER_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 128_000, 1, Integer.MAX_VALUE);
        WIRED_CHARGER_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 60, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("wirelessCharger");
        WIRELESS_CHARGER_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 128_000, 1, Integer.MAX_VALUE);
        WIRELESS_CHARGER_UPKEEP = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("energyUpkeep", 2, 1, Integer.MAX_VALUE);
        WIRELESS_CHARGER_USAGE = builder.comment("The charge speed in uI/t.")
                .defineInRange("chargeRate", 60, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("soulEngine");
        SOUL_ENGINE_CAPACITY = builder.defineInRange("capacity", 100000, 1, Integer.MAX_VALUE);
        SOUL_ENGINE_BURN_SPEED = builder.comment("The base burn-rate the soul engine.")
                .defineInRange("burnSpeed", 1, 1, Integer.MAX_VALUE);
        SOUL_ENGINE_GENERATION = builder.comment("Percentage increase in uI produced.")
                .defineInRange("generation", 1.0d, 0.001d, Double.MAX_VALUE);

        builder.pop();

        builder.push("drain");
        DRAIN_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 64_000, 1, Integer.MAX_VALUE);
        DRAIN_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 10, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("inhibitor");
        INHIBITOR_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 64_000, 1, Integer.MAX_VALUE);
        INHIBITOR_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 10, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("aversion");
        AVERSION_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 64_000, 1, Integer.MAX_VALUE);
        AVERSION_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 10, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("relocator");
        RELOCATOR_CAPACITY = builder.comment("The base energy capacity in uI.")
                .defineInRange("capacity", 64_000, 1, Integer.MAX_VALUE);
        RELOCATOR_USAGE = builder.comment("The base energy consumption in uI/t.")
                .defineInRange("usage", 10, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.pop();
    }
}
