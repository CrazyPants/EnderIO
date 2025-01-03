package com.enderio.conduits.api;

import java.util.ServiceLoader;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface ConduitApi {

    ConduitApi INSTANCE = ServiceLoader.load(ConduitApi.class).findFirst().orElseThrow();

    default ItemStack getStackForType(Holder<Conduit<?>> conduit) {
        return getStackForType(conduit, 1);
    }

    ItemStack getStackForType(Holder<Conduit<?>> conduit, int count);

    Ingredient getIngredientForType(Holder<Conduit<?>> conduit);

    int getConduitSortIndex(Holder<Conduit<?>> conduit);
}
