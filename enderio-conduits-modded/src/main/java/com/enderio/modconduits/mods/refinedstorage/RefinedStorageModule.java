package com.enderio.modconduits.mods.refinedstorage;

import com.enderio.base.api.EnderIO;
import com.enderio.base.common.init.EIOItems;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitApi;
import com.enderio.conduits.api.network.node.legacy.ConduitDataType;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.modconduits.ConduitModule;
import com.enderio.modconduits.ModdedConduits;
import com.refinedmods.refinedstorage.common.content.Tags;
import com.refinedmods.refinedstorage.neoforge.RefinedStorageNeoForgeApiImpl;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class RefinedStorageModule implements ConduitModule {

    public static final DeferredRegister<ConduitType<?>> CONDUIT_TYPES = DeferredRegister
            .create(EnderIOConduitsRegistries.CONDUIT_TYPE, EnderIO.NAMESPACE);
    public static final DeferredRegister<ConduitDataType<?>> CONDUIT_DATA_TYPES = DeferredRegister
            .create(EnderIOConduitsRegistries.CONDUIT_DATA_TYPE, EnderIO.NAMESPACE);
    public static final ConduitModule INSTANCE = new RefinedStorageModule();

    private static final ModLoadedCondition CONDITION = new ModLoadedCondition("refinedstorage");

    public static final Supplier<ConduitType<RSConduit>> RS_CONDUIT = CONDUIT_TYPES.register("rs",
            () -> ConduitType.builder(RSConduit.CODEC)
                    .exposeCapability(
                            RefinedStorageNeoForgeApiImpl.INSTANCE.getNetworkNodeContainerProviderCapability())
                    .build());

    public static ResourceKey<Conduit<?, ?>> RS = ResourceKey.create(EnderIOConduitsRegistries.Keys.CONDUIT,
        EnderIO.loc("rs"));

    public static final Supplier<ConduitDataType<RSNetworkHost>> DATA = CONDUIT_DATA_TYPES.register("rs",
            () -> new ConduitDataType<>(RSNetworkHost.CODEC, RSNetworkHost.STREAM_CODEC, RSNetworkHost::new));

    private static final Component LANG_RS_CONDUIT = addTranslation("item", EnderIO.loc("rs"),
            "Refined Storage Conduit");

    private static MutableComponent addTranslation(String prefix, ResourceLocation id, String translation) {
        return ModdedConduits.REGILITE.addTranslation(prefix, id, translation);
    }

    @Override
    public void register(IEventBus modEventBus) {
        CONDUIT_TYPES.register(modEventBus);
        CONDUIT_DATA_TYPES.register(modEventBus);
    }

    @Override
    public void bootstrapConduits(BootstrapContext<Conduit<?, ?>> context) {
        context.register(RS, new RSConduit(EnderIO.loc("block/conduit/rs"), LANG_RS_CONDUIT));
    }

    @Override
    public void buildConduitConditions(BiConsumer<ResourceKey<?>, ICondition> conditions) {
        conditions.accept(RS, CONDITION);
    }

    @Override
    public void buildRecipes(HolderLookup.Provider lookupProvider, RecipeOutput recipeOutput) {
        var rsRecipeOutput = recipeOutput.withConditions(CONDITION);

        var conduit = lookupProvider.holderOrThrow(RS);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ConduitApi.INSTANCE.getStackForType(conduit, 3))
                .pattern("BBB")
                .pattern("III")
                .pattern("BBB")
                .define('B', EIOItems.CONDUIT_BINDER)
                .define('I', Tags.CABLES)
                .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.CONDUIT_BINDER))
                .save(rsRecipeOutput, EnderIO.loc("rs_conduit"));
    }
}
