package com.enderio.modconduits;

import com.enderio.conduits.api.Conduit;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.function.BiConsumer;

public interface ConduitModule {
    void register(IEventBus modEventBus);
    void bootstrapConduits(BootstrapContext<Conduit<?, ?>> context);
    void buildConduitConditions(BiConsumer<ResourceKey<?>, ICondition> conditions);
    void buildRecipes(HolderLookup.Provider lookupProvider, RecipeOutput recipeOutput);
}
