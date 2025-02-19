package com.enderio.machines.common.init;

import com.enderio.machines.EnderIOMachines;
import com.enderio.machines.client.gui.screen.AlloySmelterScreen;
import com.enderio.machines.client.gui.screen.AversionObeliskScreen;
import com.enderio.machines.client.gui.screen.CapacitorBankScreen;
import com.enderio.machines.client.gui.screen.CrafterScreen;
import com.enderio.machines.client.gui.screen.DrainScreen;
import com.enderio.machines.client.gui.screen.EnchanterScreen;
import com.enderio.machines.client.gui.screen.FluidTankScreen;
import com.enderio.machines.client.gui.screen.ImpulseHopperScreen;
import com.enderio.machines.client.gui.screen.InhibitorObeliskScreen;
import com.enderio.machines.client.gui.screen.PaintingMachineScreen;
import com.enderio.machines.client.gui.screen.PoweredSpawnerScreen;
import com.enderio.machines.client.gui.screen.PrimitiveAlloySmelterScreen;
import com.enderio.machines.client.gui.screen.RelocatorObeliskScreen;
import com.enderio.machines.client.gui.screen.SagMillScreen;
import com.enderio.machines.client.gui.screen.SlicerScreen;
import com.enderio.machines.client.gui.screen.SoulBinderScreen;
import com.enderio.machines.client.gui.screen.SoulEngineScreen;
import com.enderio.machines.client.gui.screen.StirlingGeneratorScreen;
import com.enderio.machines.client.gui.screen.TravelAnchorScreen;
import com.enderio.machines.client.gui.screen.VacuumChestScreen;
import com.enderio.machines.client.gui.screen.VatScreen;
import com.enderio.machines.client.gui.screen.WiredChargerScreen;
import com.enderio.machines.client.gui.screen.WirelessChargerScreen;
import com.enderio.machines.client.gui.screen.XPObeliskScreen;
import com.enderio.machines.client.gui.screen.XPVacuumScreen;
import com.enderio.machines.common.blocks.alloy.AlloySmelterMenu;
import com.enderio.machines.common.blocks.alloy.PrimitiveAlloySmelterMenu;
import com.enderio.machines.common.blocks.crafter.CrafterMenu;
import com.enderio.machines.common.blocks.drain.DrainMenu;
import com.enderio.machines.common.blocks.enchanter.EnchanterMenu;
import com.enderio.machines.common.blocks.fluid_tank.FluidTankMenu;
import com.enderio.machines.common.blocks.impulse_hopper.ImpulseHopperMenu;
import com.enderio.machines.common.blocks.obelisks.aversion.AversionObeliskMenu;
import com.enderio.machines.common.blocks.obelisks.inhibitor.InhibitorObeliskMenu;
import com.enderio.machines.common.blocks.obelisks.relocator.RelocatorObeliskMenu;
import com.enderio.machines.common.blocks.obelisks.xp.XPObeliskMenu;
import com.enderio.machines.common.blocks.painting.PaintingMachineMenu;
import com.enderio.machines.common.blocks.powered_spawner.PoweredSpawnerMenu;
import com.enderio.machines.common.blocks.sag_mill.SagMillMenu;
import com.enderio.machines.common.blocks.slicer.SlicerMenu;
import com.enderio.machines.common.blocks.soul_binder.SoulBinderMenu;
import com.enderio.machines.common.blocks.soul_engine.SoulEngineMenu;
import com.enderio.machines.common.blocks.stirling_generator.StirlingGeneratorMenu;
import com.enderio.machines.common.blocks.travel_anchor.TravelAnchorMenu;
import com.enderio.machines.common.blocks.vacuum.chest.VacuumChestMenu;
import com.enderio.machines.common.blocks.vacuum.xp.XPVacuumMenu;
import com.enderio.machines.common.blocks.vat.VatMenu;
import com.enderio.machines.common.blocks.wired_charger.WiredChargerMenu;
import com.enderio.machines.common.blocks.wireless_charger.WirelessChargerMenu;
import com.enderio.machines.common.menu.CapacitorBankMenu;
import com.enderio.regilite.holder.RegiliteMenu;
import com.enderio.regilite.registry.MenuRegistry;
import net.neoforged.bus.api.IEventBus;

public class MachineMenus {
    private MachineMenus() {
    }

    private static final MenuRegistry MENU_REGISTRY = EnderIOMachines.REGILITE.menuRegistry();

    public static final RegiliteMenu<FluidTankMenu> FLUID_TANK = MENU_REGISTRY.registerMenu("fluid_tank",
            FluidTankMenu::new, () -> FluidTankScreen::new);
    public static final RegiliteMenu<EnchanterMenu> ENCHANTER = MENU_REGISTRY.registerMenu("enchanter",
            EnchanterMenu::new, () -> EnchanterScreen::new);
    public static final RegiliteMenu<PrimitiveAlloySmelterMenu> PRIMITIVE_ALLOY_SMELTER = MENU_REGISTRY.registerMenu(
            "primitive_alloy_smelter", PrimitiveAlloySmelterMenu::new, () -> PrimitiveAlloySmelterScreen::new);
    public static final RegiliteMenu<AlloySmelterMenu> ALLOY_SMELTER = MENU_REGISTRY.registerMenu("alloy_smelter",
            AlloySmelterMenu::new, () -> AlloySmelterScreen::new);
    public static final RegiliteMenu<SagMillMenu> SAG_MILL = MENU_REGISTRY.registerMenu("sag_mill", SagMillMenu::new,
            () -> SagMillScreen::new);
    public static final RegiliteMenu<StirlingGeneratorMenu> STIRLING_GENERATOR = MENU_REGISTRY
            .registerMenu("stirling_generator", StirlingGeneratorMenu::new, () -> StirlingGeneratorScreen::new);
    public static final RegiliteMenu<SlicerMenu> SLICE_N_SPLICE = MENU_REGISTRY.registerMenu("slice_n_splice",
            SlicerMenu::new, () -> SlicerScreen::new);
    public static final RegiliteMenu<ImpulseHopperMenu> IMPULSE_HOPPER = MENU_REGISTRY.registerMenu("impulse_hopper",
            ImpulseHopperMenu::new, () -> ImpulseHopperScreen::new);
    public static final RegiliteMenu<SoulBinderMenu> SOUL_BINDER = MENU_REGISTRY.registerMenu("soul_binder",
            SoulBinderMenu::new, () -> SoulBinderScreen::new);
    public static final RegiliteMenu<PoweredSpawnerMenu> POWERED_SPAWNER = MENU_REGISTRY.registerMenu("powered_spawner",
            PoweredSpawnerMenu::new, () -> PoweredSpawnerScreen::new);
    public static final RegiliteMenu<VacuumChestMenu> VACUUM_CHEST = MENU_REGISTRY.registerMenu("vacuum_chest",
            VacuumChestMenu::new, () -> VacuumChestScreen::new);
    public static final RegiliteMenu<XPVacuumMenu> XP_VACUUM = MENU_REGISTRY.registerMenu("xp_vacuum",
            XPVacuumMenu::new, () -> XPVacuumScreen::new);
    public static final RegiliteMenu<CrafterMenu> CRAFTER = MENU_REGISTRY.registerMenu("crafter", CrafterMenu::new,
            () -> CrafterScreen::new);
    public static final RegiliteMenu<DrainMenu> DRAIN = MENU_REGISTRY.registerMenu("drain", DrainMenu::new,
            () -> DrainScreen::new);
    public static final RegiliteMenu<WiredChargerMenu> WIRED_CHARGER = MENU_REGISTRY.registerMenu("wired_charger",
            WiredChargerMenu::new, () -> WiredChargerScreen::new);
    public static final RegiliteMenu<WirelessChargerMenu> WIRELESS_CHARGER = MENU_REGISTRY
            .registerMenu("wireless_charger", WirelessChargerMenu::new, () -> WirelessChargerScreen::new);
    public static final RegiliteMenu<PaintingMachineMenu> PAINTING_MACHINE = MENU_REGISTRY
            .registerMenu("painting_machine", PaintingMachineMenu::new, () -> PaintingMachineScreen::new);
    public static final RegiliteMenu<CapacitorBankMenu> CAPACITOR_BANK = MENU_REGISTRY.registerMenu("capacitor_bank",
            CapacitorBankMenu::factory, () -> CapacitorBankScreen::new);
    public static final RegiliteMenu<SoulEngineMenu> SOUL_ENGINE = MENU_REGISTRY.registerMenu("soul_engine",
            SoulEngineMenu::new, () -> SoulEngineScreen::new);
    public static final RegiliteMenu<TravelAnchorMenu> TRAVEL_ANCHOR = MENU_REGISTRY.registerMenu("travel_anchor",
            TravelAnchorMenu::new, () -> TravelAnchorScreen::new);
    public static final RegiliteMenu<XPObeliskMenu> XP_OBELISK = MENU_REGISTRY.registerMenu("xp_obelisk",
            XPObeliskMenu::new, () -> XPObeliskScreen::new);
    public static final RegiliteMenu<InhibitorObeliskMenu> INHIBITOR_OBELISK = MENU_REGISTRY
            .registerMenu("inhibitor_obelisk", InhibitorObeliskMenu::new, () -> InhibitorObeliskScreen::new);
    public static final RegiliteMenu<AversionObeliskMenu> AVERSION_OBELISK = MENU_REGISTRY
            .registerMenu("aversion_obelisk", AversionObeliskMenu::new, () -> AversionObeliskScreen::new);
    public static final RegiliteMenu<RelocatorObeliskMenu> RELOCATOR_OBELISK = MENU_REGISTRY
            .registerMenu("relocator_obelisk", RelocatorObeliskMenu::new, () -> RelocatorObeliskScreen::new);
    public static final RegiliteMenu<VatMenu> VAT = MENU_REGISTRY.registerMenu("vat", VatMenu::new,
            () -> VatScreen::new);

    public static void register(IEventBus bus) {
        MENU_REGISTRY.register(bus);
    }
}
