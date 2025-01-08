package com.enderio.conduits.data.recipe;

import com.enderio.base.api.EnderIO;
import com.enderio.base.common.init.EIOItems;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.init.ConduitItems;
import com.enderio.conduits.common.init.Conduits;
import com.enderio.conduits.common.recipe.ConduitIngredient;
import com.enderio.machines.EnderIOMachines;
import com.enderio.machines.common.blocks.painting.PaintingRecipe;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class ConduitRecipes extends RecipeProvider {

    private final CompletableFuture<HolderLookup.Provider> registries;

    public ConduitRecipes(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries);
        this.registries = registries;
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // We know that the registries are now available.
        HolderLookup.Provider lookupProvider = registries.resultNow();
        HolderGetter<Conduit<?, ?>> conduitRegistry = lookupProvider.lookupOrThrow(EnderIOConduitsRegistries.Keys.CONDUIT);

        var itemConduit = conduitRegistry.getOrThrow(Conduits.ITEM);
        var fluidConduit = conduitRegistry.getOrThrow(Conduits.FLUID);
        var pressurizedFluidConduit = conduitRegistry.getOrThrow(Conduits.PRESSURIZED_FLUID);
        var enderFluidConduit = conduitRegistry.getOrThrow(Conduits.ENDER_FLUID);
        var energyConduit = conduitRegistry.getOrThrow(Conduits.ENERGY);
        var enhancedEnergyConduit = conduitRegistry.getOrThrow(Conduits.ENHANCED_ENERGY);
        var enderEnergyConduit = conduitRegistry.getOrThrow(Conduits.ENDER_ENERGY);
        var redstoneConduit = conduitRegistry.getOrThrow(Conduits.REDSTONE);

        buildFilterRecipes(recipeOutput);
        buildFilterConversionRecipes(recipeOutput);
        buildFacadeCraftingRecipes(recipeOutput);
        buildFacadePaintingRecipes(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ConduitBlockItem.getStackFor(itemConduit, 8))
                .pattern("BBB")
                .pattern("PPP")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('P', EIOTags.Items.NUGGETS_PULSATING_ALLOY)
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(recipeOutput, EnderIO.loc("item_conduit"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ConduitBlockItem.getStackFor(fluidConduit, 8))
                .pattern("BBB")
                .pattern("GGG")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('G', EIOTags.Items.CLEAR_GLASS)
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(recipeOutput, EnderIO.loc("fluid_conduit"));

        ShapedRecipeBuilder
                .shaped(RecipeCategory.BUILDING_BLOCKS, ConduitBlockItem.getStackFor(pressurizedFluidConduit, 8))
                .pattern("BBB")
                .pattern("GGG")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('G', EIOTags.Items.FUSED_QUARTZ)
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(recipeOutput, EnderIO.loc("pressurized_fluid_conduit"));

        ShapedRecipeBuilder
                .shaped(RecipeCategory.BUILDING_BLOCKS, ConduitBlockItem.getStackFor(pressurizedFluidConduit, 8))
                .pattern("BBB")
                .pattern("GCG")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('G', EIOTags.Items.FUSED_QUARTZ)
                .define('C', ConduitIngredient.of(fluidConduit))
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(recipeOutput, EnderIO.loc("pressurized_fluid_conduit_upgrade"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ConduitBlockItem.getStackFor(enderFluidConduit, 8))
                .pattern("BBB")
                .pattern("IGI")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('G', EIOTags.Items.FUSED_QUARTZ)
                .define('I', EIOTags.Items.INGOTS_VIBRANT_ALLOY)
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(recipeOutput, EnderIO.loc("ender_fluid"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ConduitBlockItem.getStackFor(enderFluidConduit, 8))
                .pattern("BBB")
                .pattern("ICI")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('I', EIOItems.VIBRANT_ALLOY_INGOT)
                .define('C', ConduitIngredient.of(pressurizedFluidConduit))
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(recipeOutput, EnderIO.loc("ender_fluid_conduit_upgrade"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ConduitBlockItem.getStackFor(energyConduit, 8))
                .pattern("BBB")
                .pattern("III")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('I', EIOTags.Items.INGOTS_CONDUCTIVE_ALLOY)
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(recipeOutput, EnderIO.loc("energy_conduit"));

        ShapedRecipeBuilder
                .shaped(RecipeCategory.BUILDING_BLOCKS, ConduitBlockItem.getStackFor(enhancedEnergyConduit, 8))
                .pattern("BBB")
                .pattern("III")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('I', EIOTags.Items.INGOTS_ENERGETIC_ALLOY)
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(recipeOutput, EnderIO.loc("enhanced_energy_conduit"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ConduitBlockItem.getStackFor(enderEnergyConduit, 8))
                .pattern("BBB")
                .pattern("III")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('I', EIOTags.Items.INGOTS_VIBRANT_ALLOY)
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(recipeOutput, EnderIO.loc("ender_energy_conduit"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ConduitBlockItem.getStackFor(redstoneConduit, 8))
                .pattern("BBB")
                .pattern("III")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('I', EIOTags.Items.INGOTS_REDSTONE_ALLOY)
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(recipeOutput, EnderIO.loc("redstone_conduit"));

    }

    private void buildFilterRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.NOT_FILTER)
                .define('T', Items.REDSTONE_TORCH)
                .define('B', EIOItems.REDSTONE_FILTER_BASE)
                .define('I', EIOTags.Items.INGOTS_REDSTONE_ALLOY)
                .pattern("TBI")
                .unlockedBy("has_ingredient", has(EIOItems.REDSTONE_FILTER_BASE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.OR_FILTER)
                .define('B', EIOItems.REDSTONE_FILTER_BASE)
                .define('I', EIOTags.Items.INGOTS_REDSTONE_ALLOY)
                .pattern(" I ")
                .pattern(" B ")
                .pattern(" I ")
                .unlockedBy("has_ingredient", has(EIOItems.REDSTONE_FILTER_BASE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.AND_FILTER)
                .define('B', EIOItems.REDSTONE_FILTER_BASE)
                .define('T', Items.REDSTONE_TORCH)
                .pattern(" T ")
                .pattern(" B ")
                .pattern(" T ")
                .unlockedBy("has_ingredient", has(EIOItems.REDSTONE_FILTER_BASE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.XOR_FILTER)
                .define('B', EIOItems.REDSTONE_FILTER_BASE)
                .define('T', Items.REDSTONE_TORCH)
                .define('I', EIOTags.Items.INGOTS_REDSTONE_ALLOY)
                .pattern(" T ")
                .pattern("IBI")
                .pattern(" T ")
                .unlockedBy("has_ingredient", has(EIOItems.REDSTONE_FILTER_BASE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.TLATCH_FILTER)
                .define('B', EIOItems.REDSTONE_FILTER_BASE)
                .define('L', Items.LEVER)
                .define('I', EIOTags.Items.INGOTS_REDSTONE_ALLOY)
                .pattern("LBI")
                .unlockedBy("has_ingredient", has(EIOItems.REDSTONE_FILTER_BASE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.COUNT_FILTER)
                .define('B', EIOItems.REDSTONE_FILTER_BASE)
                .define('I', EIOTags.Items.INGOTS_REDSTONE_ALLOY)
                .pattern("I  ")
                .pattern("IBI")
                .pattern("I  ")
                .unlockedBy("has_ingredient", has(EIOItems.REDSTONE_FILTER_BASE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.SENSOR_FILTER)
                .define('B', EIOItems.REDSTONE_FILTER_BASE)
                .define('I', EIOTags.Items.INGOTS_REDSTONE_ALLOY)
                .define('C', Items.COMPARATOR)
                .pattern("CBI")
                .unlockedBy("has_ingredient", has(EIOItems.REDSTONE_FILTER_BASE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.TIMER_FILTER)
                .define('B', EIOItems.REDSTONE_FILTER_BASE)
                .define('I', EIOTags.Items.INGOTS_REDSTONE_ALLOY)
                .define('C', Items.CLOCK)
                .pattern("IBC")
                .unlockedBy("has_ingredient", has(EIOItems.REDSTONE_FILTER_BASE))
                .save(recipeOutput);
    }

    private void buildFilterConversionRecipes(RecipeOutput recipeOutput) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ConduitItems.OR_FILTER)
                .requires(Items.REDSTONE_TORCH)
                .requires(ConduitItems.NOR_FILTER)
                .unlockedBy("has_ingredient", has(ConduitItems.NOR_FILTER))
                .save(recipeOutput, EnderIO.loc("or_filter_from_nor_filter"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ConduitItems.NOR_FILTER)
                .requires(Items.REDSTONE_TORCH)
                .requires(ConduitItems.OR_FILTER)
                .unlockedBy("has_ingredient", has(ConduitItems.OR_FILTER))
                .save(recipeOutput, EnderIO.loc("nor_filter_from_or_filter"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ConduitItems.AND_FILTER)
                .requires(Items.REDSTONE_TORCH)
                .requires(ConduitItems.NAND_FILTER)
                .unlockedBy("has_ingredient", has(ConduitItems.NAND_FILTER))
                .save(recipeOutput, EnderIO.loc("and_filter_from_nand_filter"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ConduitItems.NAND_FILTER)
                .requires(Items.REDSTONE_TORCH)
                .requires(ConduitItems.AND_FILTER)
                .unlockedBy("has_ingredient", has(ConduitItems.AND_FILTER))
                .save(recipeOutput, EnderIO.loc("nand_filter_from_and_filter"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ConduitItems.XOR_FILTER)
                .requires(Items.REDSTONE_TORCH)
                .requires(ConduitItems.XNOR_FILTER)
                .unlockedBy("has_ingredient", has(ConduitItems.XNOR_FILTER))
                .save(recipeOutput, EnderIO.loc("xor_filter_from_xnor_filter"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ConduitItems.XNOR_FILTER)
                .requires(Items.REDSTONE_TORCH)
                .requires(ConduitItems.XOR_FILTER)
                .unlockedBy("has_ingredient", has(ConduitItems.XOR_FILTER))
                .save(recipeOutput, EnderIO.loc("xnor_filter_from_xor_filter"));
    }

    private void buildFacadeCraftingRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.CONDUIT_FACADE)
            .pattern("BBB")
            .pattern("B B")
            .pattern("BBB")
            .define('B', EIOItems.CONDUIT_BINDER)
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.HARDENED_CONDUIT_FACADE)
            .pattern(" O ")
            .pattern("OFO")
            .pattern(" O ")
            .define('O', EIOTags.Items.DUSTS_OBSIDIAN)
            .define('F', ConduitItems.CONDUIT_FACADE)
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.TRANSPARENT_CONDUIT_FACADE)
            .pattern("BBB")
            .pattern("BGB")
            .pattern("BBB")
            .define('B', EIOItems.CONDUIT_BINDER)
            .define('G', EIOTags.Items.CLEAR_GLASS)
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
            .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ConduitItems.TRANSPARENT_CONDUIT_FACADE)
            .requires(ConduitItems.CONDUIT_FACADE)
            .requires(EIOTags.Items.CLEAR_GLASS)
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
            .save(recipeOutput, EnderIO.loc("transparent_conduit_facade_from_conduit_facade"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ConduitItems.TRANSPARENT_HARDENED_CONDUIT_FACADE)
            .pattern(" O ")
            .pattern("OFO")
            .pattern(" O ")
            .define('O', EIOTags.Items.DUSTS_OBSIDIAN)
            .define('F', ConduitItems.TRANSPARENT_CONDUIT_FACADE)
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
            .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ConduitItems.TRANSPARENT_HARDENED_CONDUIT_FACADE)
            .requires(ConduitItems.HARDENED_CONDUIT_FACADE)
            .requires(EIOTags.Items.CLEAR_GLASS)
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
            .save(recipeOutput, EnderIO.loc("transparent_hardened_conduit_facade_from_hardened_conduit_facade"));
    }

    private void buildFacadePaintingRecipes(RecipeOutput recipeOutput) {
        paintingRecipe(ConduitItems.CONDUIT_FACADE, Ingredient.of(ConduitItems.CONDUIT_FACADE), recipeOutput);
        paintingRecipe(ConduitItems.HARDENED_CONDUIT_FACADE, Ingredient.of(ConduitItems.HARDENED_CONDUIT_FACADE),
                recipeOutput);
        paintingRecipe(ConduitItems.TRANSPARENT_CONDUIT_FACADE, Ingredient.of(ConduitItems.TRANSPARENT_CONDUIT_FACADE),
                recipeOutput);
        paintingRecipe(ConduitItems.TRANSPARENT_HARDENED_CONDUIT_FACADE,
                Ingredient.of(ConduitItems.TRANSPARENT_HARDENED_CONDUIT_FACADE), recipeOutput);
    }

    // TODO: I want to have a builder for all EIO recipes in the API.
    protected void paintingRecipe(ItemLike output, Ingredient input, RecipeOutput recipeOutput) {
        recipeOutput.accept(EnderIO.loc("painting/" + BuiltInRegistries.ITEM.getKey(output.asItem()).getPath()),
                new PaintingRecipe(input, output.asItem().getDefaultInstance()), null,
                new ModLoadedCondition(EnderIOMachines.MODULE_MOD_ID));
    }
}
