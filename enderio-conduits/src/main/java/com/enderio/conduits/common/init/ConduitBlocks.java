package com.enderio.conduits.common.init;

import com.enderio.base.common.tag.EIOTags;
import com.enderio.conduits.EnderIOConduits;
import com.enderio.conduits.client.ConduitFacadeColor;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.conduit.bundle.NewConduitBundleBlock;
import com.enderio.conduits.data.model.ConduitBlockState;
import com.enderio.regilite.holder.RegiliteBlock;
import com.enderio.regilite.registry.BlockRegistry;
import com.enderio.regilite.registry.ItemRegistry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;

public class ConduitBlocks {
    private static final ItemRegistry ITEM_REGISTRY = EnderIOConduits.REGILITE.itemRegistry();
    private static final BlockRegistry BLOCK_REGISTRY = EnderIOConduits.REGILITE.blockRegistry();

    public static final RegiliteBlock<NewConduitBundleBlock> CONDUIT = BLOCK_REGISTRY
            .registerBlock("conduit", NewConduitBundleBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(1.5f, 10)
                            .noLootTable()
                            .noOcclusion()
                            .dynamicShape()
                            .mapColor(MapColor.STONE))
            .setColorSupplier(() -> ConduitFacadeColor::new)
            .setTranslation("Conduit Bundle")
            .setBlockStateProvider(ConduitBlockState::conduit)
            .addBlockTags(BlockTags.MINEABLE_WITH_PICKAXE)
            .createBlockItem(ITEM_REGISTRY, b -> new ConduitBlockItem(b, new Item.Properties()),
                    item -> item.setTranslation("<MISSING> Conduit").setModelProvider((prov, ctx) -> {
                    }).addItemTags(EIOTags.Items.HIDE_FACADES));

    public static void register(IEventBus bus) {
        BLOCK_REGISTRY.register(bus);
        ITEM_REGISTRY.register(bus);
    }
}
