package com.enderio.machines.common.init;

import com.enderio.base.common.init.EIOBlocks;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.init.EIOItems;
import com.enderio.machines.EnderIOMachines;
import com.enderio.machines.client.rendering.blockentity.CapacitorBankBER;
import com.enderio.machines.client.rendering.blockentity.FluidTankBER;
import com.enderio.machines.client.rendering.blockentity.ObeliskBER;
import com.enderio.machines.client.rendering.blockentity.XPObeliskBER;
import com.enderio.machines.common.attachment.FluidTankUser;
import com.enderio.machines.common.blockentity.CreativePowerBlockEntity;
import com.enderio.machines.common.blockentity.base.LegacyMachineBlockEntity;
import com.enderio.machines.common.blockentity.base.LegacyPoweredMachineBlockEntity;
import com.enderio.machines.common.blockentity.capacitorbank.CapacitorBankBlockEntity;
import com.enderio.machines.common.blockentity.capacitorbank.CapacitorTier;
import com.enderio.machines.common.blockentity.solar.SolarPanelBlockEntity;
import com.enderio.machines.common.blockentity.solar.SolarPanelTier;
import com.enderio.machines.common.blocks.alloy.AlloySmelterBlockEntity;
import com.enderio.machines.common.blocks.alloy.PrimitiveAlloySmelterBlockEntity;
import com.enderio.machines.common.blocks.base.blockentity.MachineBlockEntity;
import com.enderio.machines.common.blocks.base.blockentity.PoweredMachineBlockEntity;
import com.enderio.machines.common.blocks.crafter.CrafterBlockEntity;
import com.enderio.machines.common.blocks.drain.DrainBlockEntity;
import com.enderio.machines.common.blocks.enchanter.EnchanterBlockEntity;
import com.enderio.machines.common.blocks.fluid_tank.FluidTankBlockEntity;
import com.enderio.machines.common.blocks.impulse_hopper.ImpulseHopperBlockEntity;
import com.enderio.machines.common.blocks.obelisks.attractor.AttractorObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.aversion.AversionObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.inhibitor.InhibitorObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.relocator.RelocatorObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.xp.XPObeliskBlockEntity;
import com.enderio.machines.common.blocks.painting.PaintingMachineBlockEntity;
import com.enderio.machines.common.blocks.powered_spawner.PoweredSpawnerBlockEntity;
import com.enderio.machines.common.blocks.sag_mill.SagMillBlockEntity;
import com.enderio.machines.common.blocks.slicer.SlicerBlockEntity;
import com.enderio.machines.common.blocks.soul_binder.SoulBinderBlockEntity;
import com.enderio.machines.common.blocks.soul_engine.SoulEngineBlockEntity;
import com.enderio.machines.common.blocks.stirling_generator.StirlingGeneratorBlockEntity;
import com.enderio.machines.common.blocks.travel_anchor.PaintedTravelAnchorBlockEntity;
import com.enderio.machines.common.blocks.travel_anchor.TravelAnchorBlockEntity;
import com.enderio.machines.common.blocks.vacuum.chest.VacuumChestBlockEntity;
import com.enderio.machines.common.blocks.vacuum.xp.XPVacuumBlockEntity;
import com.enderio.machines.common.blocks.vat.VatBlockEntity;
import com.enderio.machines.common.blocks.wired_charger.WiredChargerBlockEntity;
import com.enderio.machines.common.blocks.wireless_charger.WirelessChargerBlockEntity;
import com.enderio.regilite.holder.RegiliteBlockEntity;
import com.enderio.regilite.registry.BlockEntityRegistry;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;

public class MachineBlockEntities {
    private static final BlockEntityRegistry BLOCK_ENTITY_REGISTRY = EnderIOMachines.REGILITE.blockEntityRegistry();

    public static final RegiliteBlockEntity<FluidTankBlockEntity.Standard> FLUID_TANK = register("fluid_tank",
            FluidTankBlockEntity.Standard::new, MachineBlocks.FLUID_TANK).setRenderer(() -> FluidTankBER::new)
                    .apply(MachineBlockEntities::machineBlockEntityCapabilities)
                    .apply(MachineBlockEntities::fluidHandlerCapability);

    public static final RegiliteBlockEntity<FluidTankBlockEntity.Enhanced> PRESSURIZED_FLUID_TANK = register(
            "pressurized_fluid_tank", FluidTankBlockEntity.Enhanced::new, MachineBlocks.PRESSURIZED_FLUID_TANK)
                    .setRenderer(() -> FluidTankBER::new)
                    .apply(MachineBlockEntities::machineBlockEntityCapabilities)
                    .apply(MachineBlockEntities::fluidHandlerCapability);

    public static final RegiliteBlockEntity<EnchanterBlockEntity> ENCHANTER = register("enchanter",
            EnchanterBlockEntity::new, MachineBlocks.ENCHANTER);

    public static final RegiliteBlockEntity<PrimitiveAlloySmelterBlockEntity> PRIMITIVE_ALLOY_SMELTER = register(
            "primitive_alloy_smelter", PrimitiveAlloySmelterBlockEntity::new, MachineBlocks.PRIMITIVE_ALLOY_SMELTER)
                    .apply(MachineBlockEntities::machineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<AlloySmelterBlockEntity> ALLOY_SMELTER = register("alloy_smelter",
            AlloySmelterBlockEntity::factory, MachineBlocks.ALLOY_SMELTER)
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<CreativePowerBlockEntity> CREATIVE_POWER = register("creative_power",
            CreativePowerBlockEntity::new, MachineBlocks.CREATIVE_POWER)
                    .apply(MachineBlockEntities::legacyPoweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<StirlingGeneratorBlockEntity> STIRLING_GENERATOR = register(
            "stirling_generator", StirlingGeneratorBlockEntity::new, MachineBlocks.STIRLING_GENERATOR)
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<SagMillBlockEntity> SAG_MILL = register("sag_mill", SagMillBlockEntity::new,
            MachineBlocks.SAG_MILL).apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<SlicerBlockEntity> SLICE_AND_SPLICE = register("slice_and_splice",
            SlicerBlockEntity::new, MachineBlocks.SLICE_AND_SPLICE)
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<ImpulseHopperBlockEntity> IMPULSE_HOPPER = register("impulse_hopper",
            ImpulseHopperBlockEntity::new, MachineBlocks.IMPULSE_HOPPER)
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<VacuumChestBlockEntity> VACUUM_CHEST = register("vacuum_chest",
            VacuumChestBlockEntity::new, MachineBlocks.VACUUM_CHEST)
                    .apply(MachineBlockEntities::machineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<XPVacuumBlockEntity> XP_VACUUM = register("xp_vacuum",
            XPVacuumBlockEntity::new, MachineBlocks.XP_VACUUM)
                    .apply(MachineBlockEntities::machineBlockEntityCapabilities)
                    .apply(MachineBlockEntities::fluidHandlerCapability);

    public static final RegiliteBlockEntity<TravelAnchorBlockEntity> TRAVEL_ANCHOR = register("travel_anchor",
            TravelAnchorBlockEntity::new, MachineBlocks.TRAVEL_ANCHOR)
                    .apply(MachineBlockEntities::machineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<PaintedTravelAnchorBlockEntity> PAINTED_TRAVEL_ANCHOR = register(
            "painted_travel_anchor", PaintedTravelAnchorBlockEntity::new, MachineBlocks.PAINTED_TRAVEL_ANCHOR)
                    .apply(MachineBlockEntities::machineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<CrafterBlockEntity> CRAFTER = register("crafter", CrafterBlockEntity::new,
            MachineBlocks.CRAFTER).apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<DrainBlockEntity> DRAIN = register("drain", DrainBlockEntity::new,
            MachineBlocks.DRAIN).apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities)
                    .apply(MachineBlockEntities::fluidHandlerCapability);

    public static final RegiliteBlockEntity<SoulBinderBlockEntity> SOUL_BINDER = register("soul_binder",
            SoulBinderBlockEntity::new, MachineBlocks.SOUL_BINDER)
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities)
                    .apply(MachineBlockEntities::fluidHandlerCapability);

    public static final RegiliteBlockEntity<WiredChargerBlockEntity> WIRED_CHARGER = register("wired_charger",
            WiredChargerBlockEntity::new, MachineBlocks.WIRED_CHARGER)
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<WirelessChargerBlockEntity> WIRELESS_CHARGER = register("wireless_charger",
            WirelessChargerBlockEntity::new, MachineBlocks.WIRELESS_CHARGER)
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<PaintingMachineBlockEntity> PAINTING_MACHINE = register("painting_machine",
            PaintingMachineBlockEntity::new, MachineBlocks.PAINTING_MACHINE)
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<PoweredSpawnerBlockEntity> POWERED_SPAWNER = register("powered_spawner",
            PoweredSpawnerBlockEntity::new, MachineBlocks.POWERED_SPAWNER)
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final Map<SolarPanelTier, RegiliteBlockEntity<SolarPanelBlockEntity>> SOLAR_PANELS = Util.make(() -> {
        Map<SolarPanelTier, RegiliteBlockEntity<SolarPanelBlockEntity>> map = new HashMap<>();
        for (SolarPanelTier tier : SolarPanelTier.values()) {
            map.put(tier,
                    register(tier.name().toLowerCase(Locale.ROOT) + "_photovoltaic_cell",
                            (worldPosition, blockState) -> new SolarPanelBlockEntity(worldPosition, blockState, tier),
                            () -> MachineBlocks.SOLAR_PANELS.get(tier).get())
                                    .apply(MachineBlockEntities::legacyPoweredMachineBlockEntityCapabilities));
        }
        return ImmutableMap.copyOf(map);
    });

    public static final Map<CapacitorTier, RegiliteBlockEntity<CapacitorBankBlockEntity>> CAPACITOR_BANKS = Util
            .make(() -> {
                Map<CapacitorTier, RegiliteBlockEntity<CapacitorBankBlockEntity>> map = new HashMap<>();
                for (CapacitorTier tier : CapacitorTier.values()) {
                    map.put(tier, register(tier.name().toLowerCase(Locale.ROOT) + "_capacitor_bank",
                            (worldPosition, blockState) -> new CapacitorBankBlockEntity(worldPosition, blockState,
                                    tier),
                            () -> MachineBlocks.CAPACITOR_BANKS.get(tier).get())
                                    .setRenderer(() -> CapacitorBankBER::new)
                                    .apply(MachineBlockEntities::legacyPoweredMachineBlockEntityCapabilities));
                }
                return ImmutableMap.copyOf(map);
            });

    public static final RegiliteBlockEntity<SoulEngineBlockEntity> SOUL_ENGINE = register("soul_engine",
            SoulEngineBlockEntity::new, MachineBlocks.SOUL_ENGINE)
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities)
                    .apply(MachineBlockEntities::fluidHandlerCapability);

    public static final RegiliteBlockEntity<XPObeliskBlockEntity> XP_OBELISK = register("xp_obelisk",
            XPObeliskBlockEntity::new, MachineBlocks.XP_OBELISK).setRenderer(() -> XPObeliskBER::new)
                    // TODO: Make XP Obelisk use the common base class :)
                    // .setRenderer(() -> ObeliskBER.factory(EIOItems.EXPERIENCE_ROD::get))
                    .apply(MachineBlockEntities::machineBlockEntityCapabilities)
                    .apply(MachineBlockEntities::fluidHandlerCapability);

    public static final RegiliteBlockEntity<VatBlockEntity> VAT = register("vat", VatBlockEntity::new,
            MachineBlocks.VAT).apply(MachineBlockEntities::machineBlockEntityCapabilities)
                    .apply(MachineBlockEntities::fluidHandlerCapability);

    public static final RegiliteBlockEntity<InhibitorObeliskBlockEntity> INHIBITOR_OBELISK = register(
            "inhibitor_obelisk", InhibitorObeliskBlockEntity::new, MachineBlocks.INHIBITOR_OBELISK)
                    .setRenderer(() -> ObeliskBER.factory(() -> Items.ENDER_PEARL))
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<AversionObeliskBlockEntity> AVERSION_OBELISK = register("aversion_obelisk",
            AversionObeliskBlockEntity::new, MachineBlocks.AVERSION_OBELISK)
                    .setRenderer(() -> ObeliskBER.factory(EIOBlocks.ENDERMAN_HEAD::asItem))
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<RelocatorObeliskBlockEntity> RELOCATOR_OBELISK = register(
            "relocator_obelisk", RelocatorObeliskBlockEntity::new, MachineBlocks.RELOCATOR_OBELISK)
                    .setRenderer(() -> ObeliskBER.factory(() -> Items.PRISMARINE))
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    public static final RegiliteBlockEntity<AttractorObeliskBlockEntity> ATTRACTOR_OBELISK = register(
            "attractor_obelisk", AttractorObeliskBlockEntity::new, MachineBlocks.ATTRACTOR_OBELISK)
                    .setRenderer(() -> ObeliskBER.factory(EIOItems.ELECTROMAGNET::asItem))
                    .apply(MachineBlockEntities::poweredMachineBlockEntityCapabilities);

    @SafeVarargs
    private static <B extends BlockEntity> RegiliteBlockEntity<B> register(String name,
            BlockEntityType.BlockEntitySupplier<B> beFactory, Supplier<? extends Block>... blocks) {
        return BLOCK_ENTITY_REGISTRY.registerBlockEntity(name, beFactory, blocks);
    }

    private static void legacyMachineBlockEntityCapabilities(
            RegiliteBlockEntity<? extends LegacyMachineBlockEntity> blockEntity) {
        blockEntity.addCapability(EIOCapabilities.SideConfig.BLOCK, LegacyMachineBlockEntity.SIDE_CONFIG_PROVIDER);
        blockEntity.addCapability(Capabilities.ItemHandler.BLOCK, LegacyMachineBlockEntity.ITEM_HANDLER_PROVIDER);
    }

    private static void machineBlockEntityCapabilities(RegiliteBlockEntity<? extends MachineBlockEntity> blockEntity) {
        blockEntity.addCapability(EIOCapabilities.SideConfig.BLOCK, MachineBlockEntity.SIDE_CONFIG_PROVIDER);
        blockEntity.addCapability(Capabilities.ItemHandler.BLOCK, MachineBlockEntity.ITEM_HANDLER_PROVIDER);
    }

    private static void legacyPoweredMachineBlockEntityCapabilities(
            RegiliteBlockEntity<? extends LegacyPoweredMachineBlockEntity> blockEntity) {
        legacyMachineBlockEntityCapabilities(blockEntity);
        blockEntity.addCapability(Capabilities.EnergyStorage.BLOCK,
                LegacyPoweredMachineBlockEntity.ENERGY_STORAGE_PROVIDER);
    }

    private static void poweredMachineBlockEntityCapabilities(
            RegiliteBlockEntity<? extends PoweredMachineBlockEntity> blockEntity) {
        machineBlockEntityCapabilities(blockEntity);
        blockEntity.addCapability(Capabilities.EnergyStorage.BLOCK, PoweredMachineBlockEntity.ENERGY_STORAGE_PROVIDER);
    }

    private static void legacyFluidHandlerCapability(
            RegiliteBlockEntity<? extends LegacyMachineBlockEntity> blockEntity) {
        blockEntity.addCapability(Capabilities.FluidHandler.BLOCK, FluidTankUser.FLUID_HANDLER_PROVIDER);
    }

    private static void fluidHandlerCapability(RegiliteBlockEntity<? extends MachineBlockEntity> blockEntity) {
        blockEntity.addCapability(Capabilities.FluidHandler.BLOCK, FluidTankUser.FLUID_HANDLER_PROVIDER);
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITY_REGISTRY.register(bus);
    }
}
