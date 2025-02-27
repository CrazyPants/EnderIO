package com.enderio.machines.common.init;

import com.enderio.base.api.EnderIO;
import com.enderio.base.api.attachment.StoredEntityData;
import com.enderio.base.client.paint.PaintedBlockColor;
import com.enderio.base.common.init.EIOCreativeTabs;
import com.enderio.base.common.init.EIODataComponents;
import com.enderio.base.common.paint.item.PaintedBlockItem;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.base.data.loot.DecorLootTable;
import com.enderio.base.data.model.block.EIOBlockState;
import com.enderio.core.data.model.ModelHelper;
import com.enderio.machines.EnderIOMachines;
import com.enderio.machines.common.block.CapacitorBankBlock;
import com.enderio.machines.common.block.LegacyMachineBlock;
import com.enderio.machines.common.block.SolarPanelBlock;
import com.enderio.machines.common.blockentity.capacitorbank.CapacitorBankBlockEntity;
import com.enderio.machines.common.blockentity.capacitorbank.CapacitorTier;
import com.enderio.machines.common.blockentity.solar.SolarPanelBlockEntity;
import com.enderio.machines.common.blockentity.solar.SolarPanelTier;
import com.enderio.machines.common.blocks.base.block.MachineBlock;
import com.enderio.machines.common.blocks.base.block.ProgressMachineBlock;
import com.enderio.machines.common.blocks.base.blockentity.MachineBlockEntity;
import com.enderio.machines.common.blocks.block_detector.BlockDetectorBlock;
import com.enderio.machines.common.blocks.enchanter.EnchanterBlock;
import com.enderio.machines.common.blocks.fluid_tank.FluidTankBlock;
import com.enderio.machines.common.blocks.fluid_tank.FluidTankBlockItem;
import com.enderio.machines.common.blocks.obelisks.aversion.AversionObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.inhibitor.InhibitorObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.relocator.RelocatorObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.xp.XPObeliskBlockEntity;
import com.enderio.machines.common.blocks.powered_spawner.PoweredSpawnerBlockEntity;
import com.enderio.machines.common.blocks.soul_engine.SoulEngineBlockEntity;
import com.enderio.machines.common.blocks.travel_anchor.PaintedTravelAnchorBlock;
import com.enderio.machines.common.blocks.travel_anchor.TravelAnchorBlock;
import com.enderio.machines.common.blocks.travel_anchor.TravelAnchorBlockEntity;
import com.enderio.machines.common.blocks.vacuum.chest.VacuumChestBlockEntity;
import com.enderio.machines.common.blocks.vacuum.xp.XPVacuumBlockEntity;
import com.enderio.machines.common.blocks.vat.VatBlock;
import com.enderio.machines.common.blocks.wireless_charger.WirelessAntennaBlock;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.item.CapacitorBankItem;
import com.enderio.machines.data.loot.MachinesLootTable;
import com.enderio.machines.data.model.MachineModelUtil;
import com.enderio.regilite.data.DataGenContext;
import com.enderio.regilite.holder.RegiliteBlock;
import com.enderio.regilite.holder.RegiliteBlockEntity;
import com.enderio.regilite.registry.BlockRegistry;
import com.enderio.regilite.registry.ItemRegistry;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MachineBlocks {
    private static final BlockRegistry BLOCK_REGISTRY = EnderIOMachines.REGILITE.blockRegistry();
    private static final ItemRegistry ITEM_REGISTRY = EnderIOMachines.REGILITE.itemRegistry();

    public static final RegiliteBlock<FluidTankBlock> FLUID_TANK = BLOCK_REGISTRY
            .registerBlock("fluid_tank", props -> new FluidTankBlock(MachineBlockEntities.FLUID_TANK::get, props),
                    BlockBehaviour.Properties.of()
                            .strength(2.5f, 8)
                            .isViewBlocking((pState, pLevel, pPos) -> false)
                            .noOcclusion())
            .setLootTable(MachinesLootTable::copyComponents)
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setBlockStateProvider((prov, ctx) -> prov.horizontalBlock(ctx.get(), prov.models()
                    .getBuilder(ctx.getName())
                    .customLoader(CompositeModelBuilder::begin)
                    .child("tank",
                            ModelHelper.getExistingAsBuilder(prov.models(),
                                    EnderIO.loc(String.format("block/%s_body", ctx.getName()))))
                    .child("overlay", ModelHelper.getExistingAsBuilder(prov.models(), EnderIO.loc("block/io_overlay")))
                    .end()
                    .texture("particle", EnderIO.loc("block/machine_side"))))
            .createBlockItem(ITEM_REGISTRY, block -> new FluidTankBlockItem(block, new Item.Properties(), 16000),
                    item -> item.setModelProvider((prov, ctx) -> {
                    })
                            .setTab(EIOCreativeTabs.MACHINES)
                            .addCapability(Capabilities.FluidHandler.ITEM, FluidTankBlockItem.FLUID_HANDLER_PROVIDER));

    public static final RegiliteBlock<FluidTankBlock> PRESSURIZED_FLUID_TANK = BLOCK_REGISTRY
            .registerBlock("pressurized_fluid_tank",
                    props -> new FluidTankBlock(MachineBlockEntities.PRESSURIZED_FLUID_TANK::get, props),
                    BlockBehaviour.Properties.of()
                            .strength(2.5f, 8)
                            .isViewBlocking((pState, pLevel, pPos) -> false)
                            .noOcclusion())
            .setLootTable(MachinesLootTable::copyComponents)
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setBlockStateProvider((prov, ctx) -> prov.horizontalBlock(ctx.get(), prov.models()
                    .withExistingParent(ctx.getName(), prov.mcLoc("block/block"))
                    .customLoader(CompositeModelBuilder::begin)
                    .child("tank",
                            ModelHelper.getExistingAsBuilder(prov.models(),
                                    EnderIO.loc(String.format("block/%s_body", ctx.getName()))))
                    .child("overlay", ModelHelper.getExistingAsBuilder(prov.models(), EnderIO.loc("block/io_overlay")))
                    .end()
                    .texture("particle", EnderIO.loc("block/machine_side"))))
            .createBlockItem(ITEM_REGISTRY, (block) -> new FluidTankBlockItem(block, new Item.Properties(), 32000),
                    item -> item.setModelProvider((prov, ctx) -> {
                    })
                            .setTab(EIOCreativeTabs.MACHINES)
                            .addCapability(Capabilities.FluidHandler.ITEM, FluidTankBlockItem.FLUID_HANDLER_PROVIDER));

    public static final RegiliteBlock<EnchanterBlock> ENCHANTER = BLOCK_REGISTRY
            .registerBlock("enchanter", EnchanterBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(2.5f, 8)
                            .noOcclusion()
                            .isViewBlocking((pState, pLevel, pPos) -> false))
            .setLootTable(MachinesLootTable::copyComponents)
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setBlockStateProvider(MachineModelUtil::machineBlock)
            .createBlockItem(ITEM_REGISTRY, item -> item.setTab(EIOCreativeTabs.MACHINES));

    public static final RegiliteBlock<ProgressMachineBlock<?>> PRIMITIVE_ALLOY_SMELTER = progressMachine(
            "primitive_alloy_smelter", () -> MachineBlockEntities.PRIMITIVE_ALLOY_SMELTER);

    public static final RegiliteBlock<ProgressMachineBlock<?>> ALLOY_SMELTER = progressMachine("alloy_smelter",
            () -> MachineBlockEntities.ALLOY_SMELTER);

    public static final RegiliteBlock<ProgressMachineBlock<?>> PAINTING_MACHINE = progressMachine("painting_machine",
            () -> MachineBlockEntities.PAINTING_MACHINE);

    public static final RegiliteBlock<MachineBlock<?>> WIRED_CHARGER = machine("wired_charger",
            () -> MachineBlockEntities.WIRED_CHARGER);

    public static final RegiliteBlock<ProgressMachineBlock<?>> WIRELESS_CHARGER = progressMachine("wireless_charger",
            () -> MachineBlockEntities.WIRELESS_CHARGER);

    public static final RegiliteBlock<WirelessAntennaBlock> WIRELESS_CHARGER_ANTENNA = wirelessAntenna(
            "wireless_charger_antenna", "Pulsating Wireless Antenna",
            MachinesConfig.COMMON.WIRELESS_CHARGER_ANTENNA_RANGE);

    public static final RegiliteBlock<WirelessAntennaBlock> WIRELESS_CHARGER_ANTENNA_ADVANCED = wirelessAntenna(
            "wireless_charger_antenna_advanced", "Vibrant Wireless Antenna",
            MachinesConfig.COMMON.WIRELESS_CHARGER_ANTENNA_RANGE_2);

    public static final RegiliteBlock<LegacyMachineBlock> CREATIVE_POWER = BLOCK_REGISTRY
            .registerBlock("creative_power",
                    props -> new LegacyMachineBlock(MachineBlockEntities.CREATIVE_POWER, props),
                    BlockBehaviour.Properties.of())
            .createBlockItem(ITEM_REGISTRY, item -> item.setTab(EIOCreativeTabs.MACHINES));

    public static final RegiliteBlock<ProgressMachineBlock<?>> STIRLING_GENERATOR = progressMachine(
            "stirling_generator", () -> MachineBlockEntities.STIRLING_GENERATOR);

    public static final RegiliteBlock<ProgressMachineBlock<?>> SAG_MILL = progressMachine("sag_mill",
            () -> MachineBlockEntities.SAG_MILL).setTranslation("SAG Mill");

    public static final RegiliteBlock<ProgressMachineBlock<?>> SLICE_AND_SPLICE = progressMachine("slice_and_splice",
            () -> MachineBlockEntities.SLICE_AND_SPLICE).setTranslation("Slice'N'Splice");

    public static final RegiliteBlock<ProgressMachineBlock<?>> IMPULSE_HOPPER = progressMachine("impulse_hopper",
            () -> MachineBlockEntities.IMPULSE_HOPPER).setTranslation("Impulse Hopper");

    public static final RegiliteBlock<ProgressMachineBlock<?>> SOUL_BINDER = progressMachine("soul_binder",
            () -> MachineBlockEntities.SOUL_BINDER).setTranslation("Soul Binder");

    public static final RegiliteBlock<ProgressMachineBlock<PoweredSpawnerBlockEntity>> POWERED_SPAWNER = BLOCK_REGISTRY
            .registerBlock("powered_spawner",
                    properties -> new ProgressMachineBlock<>(MachineBlockEntities.POWERED_SPAWNER, properties),
                    BlockBehaviour.Properties.of().strength(2.5f, 8))
            .setLootTable(
                    (l, t) -> MachinesLootTable.copyStandardComponentsWith(l, t, EIODataComponents.STORED_ENTITY.get()))
            .setBlockStateProvider(MachineModelUtil::progressMachineBlock)
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .createBlockItem(ITEM_REGISTRY,
                    b -> new BlockItem(b,
                            new Item.Properties().component(EIODataComponents.STORED_ENTITY, StoredEntityData.EMPTY)),
                    item -> item.setTab(EIOCreativeTabs.MACHINES).addItemTags(EIOTags.Items.ENTITY_STORAGE));

    public static final RegiliteBlock<MachineBlock<VacuumChestBlockEntity>> VACUUM_CHEST = BLOCK_REGISTRY
            .registerBlock("vacuum_chest", p -> new MachineBlock<>(MachineBlockEntities.VACUUM_CHEST::get, p),
                    BlockBehaviour.Properties.of().strength(2.5f, 8).noOcclusion())
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setLootTable(MachinesLootTable::copyComponents)
            .setBlockStateProvider((prov, ctx) -> prov.simpleBlock(ctx.get(),
                    prov.models().getExistingFile(EnderIO.loc("block/" + ctx.getName()))))
            .createBlockItem(ITEM_REGISTRY, item -> item.setTab(EIOCreativeTabs.MACHINES));

    public static final RegiliteBlock<MachineBlock<XPVacuumBlockEntity>> XP_VACUUM = BLOCK_REGISTRY
            .registerBlock("xp_vacuum", p -> new MachineBlock<>(MachineBlockEntities.XP_VACUUM::get, p),
                    BlockBehaviour.Properties.of().strength(2.5f, 8).noOcclusion())
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setLootTable(MachinesLootTable::copyComponents)
            .setBlockStateProvider((prov, ctx) -> prov.simpleBlock(ctx.get(),
                    prov.models().getExistingFile(EnderIO.loc("block/" + ctx.getName()))))
            .setTranslation("XP Vacuum")
            .createBlockItem(ITEM_REGISTRY, item -> item.setTab(EIOCreativeTabs.MACHINES));

    public static final RegiliteBlock<TravelAnchorBlock<TravelAnchorBlockEntity>> TRAVEL_ANCHOR = BLOCK_REGISTRY
            .registerBlock("travel_anchor",
                    props -> new TravelAnchorBlock<>(MachineBlockEntities.TRAVEL_ANCHOR::get, props),
                    BlockBehaviour.Properties.of().strength(2.5f, 8).noOcclusion())
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setLootTable(MachinesLootTable::copyComponents)
            .setBlockStateProvider((prov, ctx) -> prov.simpleBlock(ctx.get(),
                    prov.models().getExistingFile(EnderIO.loc("block/" + ctx.getName()))))
            .createBlockItem(ITEM_REGISTRY, item -> item.setTab(EIOCreativeTabs.MACHINES));

    public static final RegiliteBlock<PaintedTravelAnchorBlock> PAINTED_TRAVEL_ANCHOR = BLOCK_REGISTRY
            .registerBlock("painted_travel_anchor", PaintedTravelAnchorBlock::new,
                    BlockBehaviour.Properties.of().strength(2.5f, 8).noOcclusion())
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setColorSupplier(() -> PaintedBlockColor::new)
            .setLootTable(DecorLootTable::withPaint)
            .setBlockStateProvider((prov, ctx) -> EIOBlockState.paintedBlock("painted_travel_anchor", prov, ctx.get(),
                    Blocks.DIRT, null)) // Any cube will do
            .createBlockItem(ITEM_REGISTRY, b -> new PaintedBlockItem(b, new Item.Properties()),
                    item -> item.setColorSupplier(() -> PaintedBlockColor::new));

    public static final Map<SolarPanelTier, RegiliteBlock<SolarPanelBlock>> SOLAR_PANELS = Util.make(() -> {
        Map<SolarPanelTier, RegiliteBlock<SolarPanelBlock>> panels = new HashMap<>();
        for (SolarPanelTier tier : SolarPanelTier.values()) {
            panels.put(tier, solarPanel(tier.name().toLowerCase(Locale.ROOT) + "_photovoltaic_module",
                    () -> MachineBlockEntities.SOLAR_PANELS.get(tier), tier));
        }
        return ImmutableMap.copyOf(panels);
    });

    public static final Map<CapacitorTier, RegiliteBlock<CapacitorBankBlock>> CAPACITOR_BANKS = Util.make(() -> {
        Map<CapacitorTier, RegiliteBlock<CapacitorBankBlock>> banks = new HashMap<>();
        for (CapacitorTier tier : CapacitorTier.values()) {
            banks.put(tier, capacitorBank(tier.name().toLowerCase(Locale.ROOT) + "_capacitor_bank",
                    () -> MachineBlockEntities.CAPACITOR_BANKS.get(tier), tier));
        }
        return ImmutableMap.copyOf(banks);
    });

    public static final RegiliteBlock<ProgressMachineBlock<?>> CRAFTER = progressMachine("crafter",
            () -> MachineBlockEntities.CRAFTER);

    public static final RegiliteBlock<ProgressMachineBlock<SoulEngineBlockEntity>> SOUL_ENGINE = BLOCK_REGISTRY
            .registerBlock("soul_engine", p -> new ProgressMachineBlock<>(MachineBlockEntities.SOUL_ENGINE, p),
                    BlockBehaviour.Properties.of().strength(2.5f, 8).noOcclusion())
            .setLootTable(MachinesLootTable::copyComponents)
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setBlockStateProvider(MachineModelUtil::progressMachineBlock)
            .createBlockItem(ITEM_REGISTRY,
                    b -> new BlockItem(b,
                            new Item.Properties().component(EIODataComponents.STORED_ENTITY, StoredEntityData.EMPTY)),
                    item -> item.setTab(EIOCreativeTabs.MACHINES).addItemTags(EIOTags.Items.ENTITY_STORAGE));

    public static final RegiliteBlock<ProgressMachineBlock<?>> DRAIN = progressMachine("drain",
            () -> MachineBlockEntities.DRAIN);

    public static final RegiliteBlock<VatBlock> VAT = baseMachine(BLOCK_REGISTRY.registerBlock("vat",
            props -> new VatBlock(MachineBlockEntities.VAT, props), BlockBehaviour.Properties.of().strength(2.5f, 8)),
            MachineModelUtil::machineBlock).setTranslation("VAT");

    public static final RegiliteBlock<BlockDetectorBlock> BLOCK_DETECTOR = BLOCK_REGISTRY
            .registerBlock("block_detector", BlockDetectorBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OBSERVER))
            .addBlockTags(BlockTags.MINEABLE_WITH_PICKAXE)
            .setBlockStateProvider((prov, ctx) -> prov.models().getExistingFile(EnderIO.loc("block/" + ctx.getName())))
            .createBlockItem(ITEM_REGISTRY, item -> item.setTab((EIOCreativeTabs.MACHINES)));

    public static final RegiliteBlock<MachineBlock<XPObeliskBlockEntity>> XP_OBELISK = BLOCK_REGISTRY
            .registerBlock("xp_obelisk", props -> new MachineBlock<>(MachineBlockEntities.XP_OBELISK::get, props),
                    BlockBehaviour.Properties.of()
                            .strength(2.5f, 8)
                            .isViewBlocking((pState, pLevel, pPos) -> false)
                            .noOcclusion())
            .setLootTable(MachinesLootTable::copyComponents)
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setBlockStateProvider((prov, ctx) -> prov.simpleBlock(ctx.get(),
                    prov.models().getExistingFile(EnderIO.loc("block/" + ctx.getName()))))
            .setTranslation("XP Obelisk")
            .createBlockItem(ITEM_REGISTRY, item -> item.setTab((EIOCreativeTabs.MACHINES)));

    public static final RegiliteBlock<MachineBlock<InhibitorObeliskBlockEntity>> INHIBITOR_OBELISK = BLOCK_REGISTRY
            .registerBlock("inhibitor_obelisk",
                    props -> new MachineBlock<>(MachineBlockEntities.INHIBITOR_OBELISK::get, props),
                    BlockBehaviour.Properties.of()
                            .strength(2.5f, 8)
                            .isViewBlocking((pState, pLevel, pPos) -> false)
                            .noOcclusion())
            .setLootTable(MachinesLootTable::copyComponents)
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setBlockStateProvider((prov, ctx) -> prov.simpleBlock(ctx.get(),
                    prov.models().getExistingFile(EnderIO.loc("block/" + ctx.getName()))))
            .createBlockItem(ITEM_REGISTRY, item -> item.setTab((EIOCreativeTabs.MACHINES)));

    public static final RegiliteBlock<MachineBlock<AversionObeliskBlockEntity>> AVERSION_OBELISK = BLOCK_REGISTRY
            .registerBlock("aversion_obelisk",
                    props -> new MachineBlock<>(MachineBlockEntities.AVERSION_OBELISK::get, props),
                    BlockBehaviour.Properties.of()
                            .strength(2.5f, 8)
                            .isViewBlocking((pState, pLevel, pPos) -> false)
                            .noOcclusion())
            .setLootTable(MachinesLootTable::copyComponents)
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setBlockStateProvider((prov, ctx) -> prov.simpleBlock(ctx.get(),
                    prov.models().getExistingFile(EnderIO.loc("block/" + ctx.getName()))))
            .createBlockItem(ITEM_REGISTRY, item -> item.setTab((EIOCreativeTabs.MACHINES)));

    public static final RegiliteBlock<MachineBlock<RelocatorObeliskBlockEntity>> RELOCATOR_OBELISK = BLOCK_REGISTRY
            .registerBlock("relocator_obelisk",
                    props -> new MachineBlock<>(MachineBlockEntities.RELOCATOR_OBELISK::get, props),
                    BlockBehaviour.Properties.of()
                            .strength(2.5f, 8)
                            .isViewBlocking((pState, pLevel, pPos) -> false)
                            .noOcclusion())
            .setLootTable(MachinesLootTable::copyComponents)
            .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
            .setBlockStateProvider((prov, ctx) -> prov.simpleBlock(ctx.get(),
                    prov.models().getExistingFile(EnderIO.loc("block/" + ctx.getName()))))
            .createBlockItem(ITEM_REGISTRY, item -> item.setTab((EIOCreativeTabs.MACHINES)));

    // used when single methods needs to be overridden in the block class
    private static <T extends MachineBlock<?>> RegiliteBlock<T> baseMachine(RegiliteBlock<T> machineBlock,
            BiConsumer<BlockStateProvider, DataGenContext<Block, T>> blockStateProvider) {
        return machineBlock.setLootTable(MachinesLootTable::copyComponents)
                .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
                .setBlockStateProvider(blockStateProvider)
                .createBlockItem(ITEM_REGISTRY, item -> item.setTab(EIOCreativeTabs.MACHINES));
    }

    private static RegiliteBlock<MachineBlock<?>> machine(String name,
            Supplier<RegiliteBlockEntity<? extends MachineBlockEntity>> regiliteBlockEntity) {
        return baseMachine(
                BLOCK_REGISTRY.registerBlock(name, props -> new MachineBlock<>(regiliteBlockEntity.get()::get, props),
                        BlockBehaviour.Properties.of().strength(2.5f, 8)),
                MachineModelUtil::machineBlock);
    }

    private static RegiliteBlock<ProgressMachineBlock<?>> progressMachine(String name,
            Supplier<RegiliteBlockEntity<? extends MachineBlockEntity>> regiliteBlockEntity) {
        return baseMachine(BLOCK_REGISTRY.registerBlock(name,
                props -> new ProgressMachineBlock<>(regiliteBlockEntity.get(), props),
                BlockBehaviour.Properties.of().strength(2.5f, 8)), MachineModelUtil::progressMachineBlock);
    }

    private static RegiliteBlock<SolarPanelBlock> solarPanel(String name,
            Supplier<RegiliteBlockEntity<? extends SolarPanelBlockEntity>> RegiliteBlockEntity, SolarPanelTier tier) {
        return BLOCK_REGISTRY
                .registerBlock(name, props -> new SolarPanelBlock(RegiliteBlockEntity.get(), props, tier),
                        BlockBehaviour.Properties.of().strength(2.5f, 8))
                .setBlockStateProvider((prov, ctx) -> MachineModelUtil.solarPanel(prov, ctx, tier))
                .addBlockTags(BlockTags.MINEABLE_WITH_PICKAXE)
                .setLootTable(MachinesLootTable::copyComponents)
                .createBlockItem(ITEM_REGISTRY,
                        item -> item.setModelProvider((prov, ctx) -> MachineModelUtil.solarPanel(prov, ctx, tier))
                                .setTab(EIOCreativeTabs.MACHINES)
                                .addItemTags(EIOTags.Items.ENTITY_STORAGE));
    }

    private static RegiliteBlock<CapacitorBankBlock> capacitorBank(String name,
            Supplier<RegiliteBlockEntity<? extends CapacitorBankBlockEntity>> RegiliteBlockEntity, CapacitorTier tier) {
        return BLOCK_REGISTRY
                .registerBlock(name, props -> new CapacitorBankBlock(props, RegiliteBlockEntity.get(), tier),
                        BlockBehaviour.Properties.of().strength(2.5f, 8))
                .setBlockStateProvider((prov, ctx) -> prov.simpleBlock(ctx.get(),
                        prov.models().getExistingFile(EnderIO.loc(ctx.getName()))))
                .setLootTable(MachinesLootTable::copyComponents)
                .addBlockTags(BlockTags.MINEABLE_WITH_PICKAXE)
                .createBlockItem(ITEM_REGISTRY, block -> new CapacitorBankItem(block, new Item.Properties()),
                        item -> item.setModelProvider((prov, ctx) -> {
                        })
                                .setTab(EIOCreativeTabs.MACHINES)
                                .addCapability(Capabilities.EnergyStorage.ITEM,
                                        CapacitorBankItem.ENERGY_STORAGE_PROVIDER));
    }

    private static RegiliteBlock<WirelessAntennaBlock> wirelessAntenna(String name, String translation,
            ModConfigSpec.ConfigValue<Integer> range) {
        return BLOCK_REGISTRY
                .registerBlock(name, props -> (new WirelessAntennaBlock(props, range)),
                        BlockBehaviour.Properties.of()
                                .strength(2.5f, 8)
                                .isViewBlocking((pState, pLevel, pPos) -> false)
                                .noOcclusion())
                .setLootTable(MachinesLootTable::copyComponents)
                .addBlockTags(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE)
                .setBlockStateProvider((prov, ctx) -> prov.simpleBlock(ctx.get(),
                        prov.models().getExistingFile(EnderIO.loc("block/" + ctx.getName()))))
                .setTranslation(translation)
                .createBlockItem(ITEM_REGISTRY, item -> item.setTab((EIOCreativeTabs.MACHINES)));
    }

    public static void register(IEventBus bus) {
        BLOCK_REGISTRY.register(bus);
        ITEM_REGISTRY.register(bus);
    }
}
