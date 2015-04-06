package crazypants.enderio.material;

import static crazypants.util.OreDictionaryHelper.INGOT_TIN;
import static crazypants.util.OreDictionaryHelper.hasCopper;
import static crazypants.util.OreDictionaryHelper.hasEnderPearlDust;
import static crazypants.util.OreDictionaryHelper.hasTin;

import java.util.ArrayList;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;
import crazypants.enderio.config.Config;
import crazypants.enderio.init.EIOBlocks;
import crazypants.enderio.init.EIOItems;
import crazypants.enderio.machine.weather.TileWeatherObelisk.WeatherTask;
import crazypants.util.OreDictionaryHelper;


public class MaterialRecipes {

  public static void registerDependantOresInDictionary() {
    if(hasCopper()) {
      OreDictionary.registerOre("dustCopper", new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_COPPER.ordinal()));
    }
    if(hasTin()) {
      OreDictionary.registerOre("dustTin", new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_TIN.ordinal()));
    }
    if(hasEnderPearlDust()) {
      OreDictionary.registerOre("dustEnderPearl", new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_ENDER.ordinal()));
    }
    //Enderium Base
    if(OreDictionaryHelper.hasEnderium()) {
      OreDictionary.registerOre("ingotEnderiumBase",
          new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.INGOT_ENDERIUM_BASE.ordinal()));
    }
  }

  public static void registerOresInDictionary() {
    //Ore Dictionary Registeration
    OreDictionary.registerOre("dustCoal", new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_COAL.ordinal()));
    OreDictionary.registerOre("dustIron", new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_IRON.ordinal()));
    OreDictionary.registerOre("dustGold", new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_GOLD.ordinal()));
    OreDictionary.registerOre("dustObsidian", new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_OBSIDIAN.ordinal()));
    
    OreDictionary.registerOre("gearStone", new ItemStack(EIOItems.itemMachinePart, 1, MachinePart.BASIC_GEAR.ordinal()));
    OreDictionary.registerOre("itemSilicon", new ItemStack(EIOItems.itemMaterial, 1, Material.SILICON.ordinal()));

    for (Alloy alloy : Alloy.values()) {
      OreDictionary.registerOre(alloy.oredictIngotName, new ItemStack(EIOItems.itemAlloy, 1, alloy.ordinal()));
      OreDictionary.registerOre(alloy.oredictBlockName, new ItemStack(EIOBlocks.blockIngotStorage, 1, alloy.ordinal()));
    }

    OreDictionary
        .registerOre("nuggetPulsatingIron", new ItemStack(EIOItems.itemMaterial, 1, Material.PHASED_IRON_NUGGET.ordinal()));
    OreDictionary.registerOre("nuggetVibrantAlloy", new ItemStack(EIOItems.itemMaterial, 1, Material.VIBRANT_NUGGET.ordinal()));

    OreDictionary.registerOre("glass", Blocks.glass);
    OreDictionary.registerOre("stickWood", Items.stick);
    OreDictionary.registerOre("woodStick", Items.stick);
    OreDictionary.registerOre("sand", new ItemStack(Blocks.sand, 1, OreDictionary.WILDCARD_VALUE));
    OreDictionary.registerOre("ingotIron", Items.iron_ingot);
    OreDictionary.registerOre("ingotGold", Items.gold_ingot);

    ItemStack pureGlass = new ItemStack(EIOBlocks.blockFusedQuartz, 1, BlockFusedQuartz.Type.GLASS.ordinal());
    OreDictionary.registerOre("glass", pureGlass);
    OreDictionary.registerOre("blockGlass", pureGlass);
    OreDictionary.registerOre("blockGlassHardened",
        new ItemStack(EIOBlocks.blockFusedQuartz, 1, BlockFusedQuartz.Type.FUSED_QUARTZ.ordinal()));

    //Skulls
    ItemStack skull = new ItemStack(Items.skull, 1, OreDictionary.WILDCARD_VALUE);
    OreDictionary.registerOre("itemSkull", skull);
    OreDictionary.registerOre("itemSkull", new ItemStack(EIOBlocks.blockEndermanSkull));

    //Glass stuff for compatability
    GameRegistry.addShapedRecipe(new ItemStack(Blocks.glass_pane, 16, 0), "   ", "eee", "eee", 'e', pureGlass);
    GameRegistry.addShapelessRecipe(new ItemStack(Blocks.glass), pureGlass);
    GameRegistry.addShapedRecipe(new ItemStack(Items.glass_bottle, 3, 0), "   ", "g g", " g ", 'g', pureGlass);

    Material.registerOres(EIOItems.itemMaterial);
  }

  public static void addRecipes() {

    //Common Ingredients
    ItemStack conduitBinder = new ItemStack(EIOItems.itemMaterial, 4, Material.CONDUIT_BINDER.ordinal());
    ItemStack basicGear = new ItemStack(EIOItems.itemMachinePart, 1, MachinePart.BASIC_GEAR.ordinal());
    ItemStack binderComposite = new ItemStack(EIOItems.itemMaterial, 1, Material.BINDER_COMPOSITE.ordinal());

    ItemStack fusedQuartzFrame = new ItemStack(EIOItems.itemFusedQuartzFrame, 1, 0);
    ItemStack machineChassi = new ItemStack(EIOItems.itemMachinePart, 1, MachinePart.MACHINE_CHASSI.ordinal());

    ItemStack phasedGold = new ItemStack(EIOItems.itemAlloy, 1, Alloy.PHASED_GOLD.ordinal());
    ItemStack phasedIron = new ItemStack(EIOItems.itemAlloy, 1, Alloy.PHASED_IRON.ordinal());
    ItemStack electricalSteel = new ItemStack(EIOItems.itemAlloy, 1, Alloy.ELECTRICAL_STEEL.ordinal());
    ItemStack darkSteel = new ItemStack(EIOItems.itemAlloy, 1, Alloy.DARK_STEEL.ordinal());

    ItemStack capacitor = new ItemStack(EIOItems.itemBasicCapacitor, 1, 0);

    //Conduit Binder
    ItemStack cbc = binderComposite.copy();
    cbc.stackSize = 8;
    if(Config.useAlternateBinderRecipe) {
      GameRegistry.addShapedRecipe(cbc, "gcg", "sgs", "gcg", 'g', Blocks.gravel, 's', Blocks.sand, 'c', Items.clay_ball);
    } else {
      GameRegistry.addShapedRecipe(cbc, "ggg", "scs", "ggg", 'g', Blocks.gravel, 's', Blocks.sand, 'c', Items.clay_ball);
    }
    FurnaceRecipes.smelting().func_151394_a(binderComposite, conduitBinder, 0);

    //Nuggets
    ItemStack phasedIronNugget = new ItemStack(EIOItems.itemMaterial, 9, Material.PHASED_IRON_NUGGET.ordinal());
    GameRegistry.addShapelessRecipe(phasedIronNugget, phasedIron);
    phasedIronNugget = phasedIronNugget.copy();
    phasedIronNugget.stackSize = 1;
    GameRegistry.addShapedRecipe(phasedIron, "eee", "eee", "eee", 'e', phasedIronNugget);

    ItemStack vibrantNugget = new ItemStack(EIOItems.itemMaterial, 9, Material.VIBRANT_NUGGET.ordinal());
    GameRegistry.addShapelessRecipe(vibrantNugget, phasedGold);
    vibrantNugget = vibrantNugget.copy();
    vibrantNugget.stackSize = 1;
    GameRegistry.addShapedRecipe(phasedGold, "eee", "eee", "eee", 'e', vibrantNugget);

    //Crystals
    ItemStack pulsCry = new ItemStack(EIOItems.itemMaterial, 1, Material.PULSATING_CYSTAL.ordinal());
    GameRegistry.addShapedRecipe(pulsCry, "nnn", "ngn", "nnn", 'n', phasedIronNugget, 'g', Items.diamond);

    ItemStack vibCry = new ItemStack(EIOItems.itemMaterial, 1, Material.VIBRANT_CYSTAL.ordinal());
    GameRegistry.addShapedRecipe(vibCry, "nnn", "ngn", "nnn", 'n', vibrantNugget, 'g', Items.emerald);

    //Balls
    ItemStack darkBall = new ItemStack(EIOItems.itemMaterial, 5, Material.DRAK_GRINDING_BALL.ordinal());
    GameRegistry.addShapedRecipe(darkBall, " s ", "sss", " s ", 's', darkSteel);

    //Smelting
    FurnaceRecipes.smelting().func_151394_a(new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_IRON.ordinal()),
        new ItemStack(Items.iron_ingot), 0);
    FurnaceRecipes.smelting().func_151394_a(new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_GOLD.ordinal()),
        new ItemStack(Items.gold_ingot), 0);

    //Ender Dusts
    ItemStack enderDust = new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_ENDER.ordinal());
    GameRegistry.addShapedRecipe(new ItemStack(Items.ender_pearl), "eee", "eee", "eee", 'e', enderDust);

    //Dark Iron Bars
    ItemStack diBars = new ItemStack(EIOBlocks.blockDarkIronBars, 16, 0);
    GameRegistry.addShapedRecipe(diBars, "ddd", "ddd", 'd', darkSteel);

    // Fused Quartz Frame
    GameRegistry.addRecipe(new ShapedOreRecipe(fusedQuartzFrame, "bsb", "s s", "bsb", 'b', conduitBinder, 's', "stickWood"));
    GameRegistry.addRecipe(new ShapedOreRecipe(fusedQuartzFrame, "bsb", "s s", "bsb", 'b', conduitBinder, 's', "woodStick"));

    // Machine Chassi

    ArrayList<ItemStack> steelIngots = OreDictionary.getOres("ingotSteel");

    if(Config.useSteelInChassi == true && steelIngots != null && !steelIngots.isEmpty()) {
      GameRegistry.addRecipe(new ShapedOreRecipe(machineChassi, "fif", "ici", "fif", 'f', Blocks.iron_bars, 'i', "ingotSteel", 'c', capacitor));
    } else {
      GameRegistry.addShapedRecipe(machineChassi, "fif", "ici", "fif", 'f', Blocks.iron_bars, 'i', Items.iron_ingot, 'c', capacitor);
    }

    // Basic Gear
    GameRegistry.addRecipe(new ShapedOreRecipe(basicGear, "scs", "c c", "scs", 's', "stickWood", 'c', Blocks.cobblestone));
    GameRegistry.addRecipe(new ShapedOreRecipe(basicGear, "scs", "c c", "scs", 's', "woodStick", 'c', Blocks.cobblestone));

    //Ender Capacitor
    ItemStack enderCapacitor = new ItemStack(EIOItems.itemBasicCapacitor, 1, 2);
    ItemStack activatedCapacitor = new ItemStack(EIOItems.itemBasicCapacitor, 1, 1);
    if(Config.useHardRecipes) {
      GameRegistry.addShapedRecipe(enderCapacitor, "eee", "cgc", "eee", 'e', phasedGold, 'c', activatedCapacitor, 'g', Blocks.glowstone);
    } else {
      GameRegistry.addShapedRecipe(enderCapacitor, " e ", "cgc", " e ", 'e', phasedGold, 'c', activatedCapacitor, 'g', Blocks.glowstone);
    }

    // Weather Crystal
    ItemStack main = Config.useHardRecipes ? new ItemStack(EIOItems.itemMaterial, 1, Material.VIBRANT_CYSTAL.ordinal())
        : new ItemStack(Items.diamond);
    GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(EIOItems.itemMaterial, 1, Material.WEATHER_CRYSTAL.ordinal()),
        main, WeatherTask.CLEAR
        .requiredItem(), WeatherTask.RAIN.requiredItem(), WeatherTask.STORM.requiredItem()));

    if(Config.reinforcedObsidianEnabled) {
      ItemStack reinfObs = new ItemStack(EIOBlocks.blockReinforcedObsidian);
      ItemStack corners = darkSteel;
      if(Config.reinforcedObsidianUseDarkSteelBlocks) {
        corners = new ItemStack(EIOBlocks.blockIngotStorage, 1, Alloy.DARK_STEEL.ordinal());
      }
      GameRegistry.addShapedRecipe(reinfObs, "dbd", "bob", "dbd", 'd', corners, 'b', EIOBlocks.blockDarkIronBars, 'o',
          Blocks.obsidian);
    }

    GameRegistry.addRecipe(new ShapedOreRecipe(EIOBlocks.blockDarkSteelAnvil,
        "bbb",
        " i ",
        "iii",

        'b', "blockDarkSteel",
        'i', "ingotDarkSteel"
        ));

    GameRegistry.addRecipe(new ItemStack(EIOBlocks.blockDarkSteelLadder, 12), "b", "b", "b", 'b',
        crazypants.enderio.init.EIOBlocks.blockDarkIronBars);

    for (Alloy alloy : Alloy.values()) {
      GameRegistry
.addRecipe(new ShapedOreRecipe(new ItemStack(EIOBlocks.blockIngotStorage, 1, alloy.ordinal()), "iii", "iii",
          "iii", 'i', alloy.oredictIngotName));
      GameRegistry.addShapelessRecipe(new ItemStack(EIOItems.itemAlloy, 9, alloy.ordinal()), new ItemStack(
          crazypants.enderio.init.EIOBlocks.blockIngotStorage, 1, alloy.ordinal()));
    }

    //Food
    ItemStack flour = new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.FLOUR.ordinal());
    ItemStack bread = new ItemStack(Items.bread, 1, 0);

    GameRegistry.addSmelting(flour, bread, (float) 0.35);

  }

  public static void addOreDictionaryRecipes() {
    if(OreDictionaryHelper.hasCopper()) {
      FurnaceRecipes.smelting().func_151394_a(new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_COPPER.ordinal()),
          OreDictionaryPreferences.instance.getPreferred(OreDictionaryHelper.INGOT_COPPER), 0);
    }
    if(hasTin()) {
      FurnaceRecipes.smelting().func_151394_a(new ItemStack(EIOItems.itemPowderIngot, 1, PowderIngot.POWDER_TIN.ordinal()),
          OreDictionaryPreferences.instance.getPreferred(INGOT_TIN), 0);
    }

    ItemStack capacitor = new ItemStack(EIOItems.itemBasicCapacitor, 1, 0);
    ArrayList<ItemStack> copperIngots = OreDictionary.getOres("ingotCopper");
    Item gold;
    if(Config.useHardRecipes) {
      gold = Items.gold_ingot;
    } else {
      gold = Items.gold_nugget;
    }
    if(copperIngots != null && !copperIngots.isEmpty() && Config.useModMetals) {
      GameRegistry.addRecipe(new ShapedOreRecipe(capacitor, " gr", "gcg", "rg ", 'r', Items.redstone, 'g', gold, 'c', "ingotCopper"));
    } else {
      GameRegistry.
          addShapedRecipe(capacitor, " gr", "gig", "rg ", 'r', Items.redstone, 'g', gold, 'i', Items.iron_ingot);
    }

    int dustCoal = OreDictionary.getOreID("dustCoal");
    ItemStack activatedCapacitor = new ItemStack(EIOItems.itemBasicCapacitor, 1, 1);
    ItemStack energeticAlloy = new ItemStack(EIOItems.itemAlloy, 1, Alloy.ENERGETIC_ALLOY.ordinal());
    if(Config.useHardRecipes) {
      GameRegistry.addRecipe(new ShapedOreRecipe(activatedCapacitor, "eee", "cCc", "eee", 'e', energeticAlloy, 'c', capacitor, 'C', "dustCoal"));
    } else {
      GameRegistry.addRecipe(new ShapedOreRecipe(activatedCapacitor, " e ", "cCc", " e ", 'e', energeticAlloy, 'c', capacitor, 'C', "dustCoal"));
    }
  }
}