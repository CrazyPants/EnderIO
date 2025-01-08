package com.enderio.conduits.common.recipe;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.init.ConduitBlocks;
import com.enderio.conduits.common.init.ConduitComponents;
import com.enderio.conduits.common.init.ConduitIngredientTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

public class ConduitIngredient implements ICustomIngredient {

    public static final MapCodec<ConduitIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(Conduit.CODEC.fieldOf("conduit_type").forGetter(ConduitIngredient::conduit))
                    .apply(builder, ConduitIngredient::new));

    private final Holder<Conduit<?, ?>> conduit;

    private ConduitIngredient(Holder<Conduit<?, ?>> conduit) {
        this.conduit = conduit;
    }

    public static Ingredient of(Holder<Conduit<?, ?>> conduit) {
        return new ConduitIngredient(conduit).toVanilla();
    }

    public Holder<Conduit<?, ?>> conduit() {
        return conduit;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!stack.is(ConduitBlocks.CONDUIT.asItem())) {
            return false;
        }

        if (!stack.has(ConduitComponents.CONDUIT)) {
            return false;
        }

        Holder<Conduit<?, ?>> conduit = stack.get(ConduitComponents.CONDUIT);
        if (conduit == null) {
            return false;
        }
        return this.conduit.is(conduit);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(ConduitBlockItem.getStackFor(conduit, 1));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return ConduitIngredientTypes.CONDUIT_INGREDIENT_TYPE.get();
    }
}
