package com.enderio.conduits.api;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ServiceLoader;

public interface ConduitApi {

    ConduitApi INSTANCE = ServiceLoader.load(ConduitApi.class).findFirst().orElseThrow();

    default ItemStack getStackForType(Holder<Conduit<?>> conduit) {
        return getStackForType(conduit, 1);
    }

    ItemStack getStackForType(Holder<Conduit<?>> conduit, int count);

    Ingredient getIngredientForType(Holder<Conduit<?>> conduit);

    int getConduitSortIndex(Holder<Conduit<?>> conduit);
}
