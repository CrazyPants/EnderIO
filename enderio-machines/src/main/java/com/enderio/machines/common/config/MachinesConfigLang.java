package com.enderio.machines.common.config;

import com.enderio.machines.EnderIOMachines;

public class MachinesConfigLang {
    public static void register() {
        // TODO: can we add a config module to new Regilite?

        // -- Common --

        // Energy
        addTranslation("energy", "Energy");
        addTranslation("throttleEnergyUsage", "Throttle Energy Use");
        addTranslation("capacity", "Capacity (\u00B5I)");
        addTranslation("usage", "Consumption Rate (\u00B5I/t)");
        addTranslation("energyCost", "Energy Cost (\u00B5I)");

        addTranslation("alloySmelter", "Alloy Smelter");
        addTranslation("vanillaItemEnergy", "Vanilla Smelting Consumption");

        addTranslation("crafter", "Crafter");
        addTranslation("impulseHopper", "Impulse Hopper");
        addTranslation("poweredSpawner", "Powered Spawner");
        addTranslation("sagMill", "Sag Mill");
        addTranslation("slicer", "Slice 'n' Splice");
        addTranslation("soulBinder", "Soul Binder");

        addTranslation("stirlingGenerator", "Stirling Generator");
        addTranslation("burnSpeed", "Burn Speed");
        addTranslation("fuelEfficiencyBase", "Base Fuel Efficiency");
        addTranslation("fuelEfficiencyStep", "Fuel Efficiency Step");
        addTranslation("generation", "Generation Rate (\u00B5I/t)");

        addTranslation("paintingMachine", "Painting Machine");
        addTranslation("photovoltaicCellRates", "Photovoltaic Cell Rates");
        addTranslation("energetic", "Energetic");
        addTranslation("pulsating", "Pulsating");
        addTranslation("vibrant", "Vibrant");

        addTranslation("capacitorBankCapacity", "Capacitor Bank Capacity");
        addTranslation("basic", "Basic");
        addTranslation("advanced", "Advanced");

        addTranslation("wiredCharger", "Wired Charger");
        addTranslation("soulEngine", "Soul Engine");
        addTranslation("drain", "Drain");
        addTranslation("inhibitor", "Inhibitor Obelisk");
        addTranslation("aversion", "Aversion Obelisk");
        addTranslation("relocator", "Relocator Obelisk");
        addTranslation("attractor", "Attractor Obelisk");

        // Enchanter
        addTranslation("enchanter", "Enchanter");
        addTranslation("lapisCostFactor", "Lapis Cost Multiplier");
        addTranslation("levelCostFactor", "Level Cost Multiplier");
        addTranslation("baseLevelCost", "Base Level Cost");

        // Powered Spawner
        addTranslation("spawnAmount", "Spawn Amount");
        addTranslation("maxEntities", "Max Entities");
        addTranslation("spawnType", "Spawn Type");
        addTranslation("maxSpawners", "Max Spawners");

        // Wireless Charger
        addTranslation("wirelessCharger", "Wireless Charger");
        addTranslation("baseRange", "Base Range");
        addTranslation("energyUpkeep", "Upkeep Cost (\u00B5I/t)");
        addTranslation("chargeRate", "Charge Speed (\u00B5I/t)");
        addTranslation("pulsatingRangeExtension", "Pulsating Antenna range extension");
        addTranslation("vibrantRangeExtension", "Vibrant Antenna range extension");

        // Obelisks
        addTranslation("obelisks", "Obelisks");

        // -- Client --

        // Blocks
        addTranslation("blocks", "Blocks");
        addTranslation("vacuumChestRangeColor", "Vaccum Chest Range Color");
        addTranslation("vacuumXpRangeColor", "XP Vacuum Range Color");
        addTranslation("poweredSpawnerRangeColor", "Powered Spawner Range Color");
        addTranslation("drainRangeColor", "Drain Range Color");
        addTranslation("inhibitorRangeColor", "Inhibitor Obelisk Range Color");
        addTranslation("relocatorRangeColor", "Relocator Obelisk Range Color");
        addTranslation("aversionRangeColor", "Aversion Obelisk Range Color");

        // IO Config
        addTranslation("ioconfig", "IO Config");
        addTranslation("neighbourTransparency", "Neighbour Transparency");
    }

    private static void addTranslation(String key, String translation) {
        // TODO: More translation options in Regilite
        EnderIOMachines.REGILITE.addTranslation(() -> EnderIOMachines.MODULE_MOD_ID + "." + "configuration" + "." + key,
                translation);
    }
}
