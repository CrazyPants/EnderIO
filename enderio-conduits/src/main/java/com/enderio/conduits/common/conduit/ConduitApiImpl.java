package com.enderio.conduits.common.conduit;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitApi;
import com.enderio.conduits.common.recipe.ConduitIngredient;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ConduitApiImpl implements ConduitApi {

    @Override
    public ItemStack getStackForType(Holder<Conduit<?, ?>> conduit, int count) {
        return ConduitBlockItem.getStackFor(conduit, count);
    }

    @Override
    public Ingredient getIngredientForType(Holder<Conduit<?, ?>> conduit) {
        return ConduitIngredient.of(conduit);
    }

    @Override
    public int getConduitSortIndex(Holder<Conduit<?, ?>> conduit) {
        return ConduitSorter.getSortIndex(conduit);
    }
}
