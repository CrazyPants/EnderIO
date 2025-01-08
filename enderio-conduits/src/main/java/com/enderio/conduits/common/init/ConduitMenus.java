package com.enderio.conduits.common.init;

import com.enderio.conduits.EnderIOConduits;
import com.enderio.conduits.client.gui.ConduitScreen;
import com.enderio.conduits.client.gui.RedstoneCountFilterScreen;
import com.enderio.conduits.client.gui.RedstoneDoubleChannelFilterScreen;
import com.enderio.conduits.client.gui.RedstoneTimerFilterScreen;
import com.enderio.conduits.client.gui.screen.NewConduitScreen;
import com.enderio.conduits.common.conduit.menu.NewConduitMenu;
import com.enderio.conduits.common.menu.ConduitMenu;
import com.enderio.conduits.common.menu.RedstoneCountFilterMenu;
import com.enderio.conduits.common.menu.RedstoneDoubleChannelFilterMenu;
import com.enderio.conduits.common.menu.RedstoneTimerFilterMenu;
import com.enderio.regilite.holder.RegiliteMenu;
import com.enderio.regilite.registry.MenuRegistry;
import net.neoforged.bus.api.IEventBus;

public class ConduitMenus {
    private static final MenuRegistry MENU_REGISTRY = EnderIOConduits.REGILITE.menuRegistry();

    public static final RegiliteMenu<NewConduitMenu> CONDUIT_MENU = MENU_REGISTRY
        .registerMenu("conduit", NewConduitMenu::new, () -> NewConduitScreen::new);

    public static final RegiliteMenu<RedstoneDoubleChannelFilterMenu> REDSTONE_DOUBLE_CHANNEL_FILTER = MENU_REGISTRY
        .registerMenu("redstone_and_filter", RedstoneDoubleChannelFilterMenu::factory, () -> RedstoneDoubleChannelFilterScreen::new);

    public static final RegiliteMenu<RedstoneTimerFilterMenu> REDSTONE_TIMER_FILTER = MENU_REGISTRY
        .registerMenu("redstone_timer_filter", RedstoneTimerFilterMenu::factory, () -> RedstoneTimerFilterScreen::new);

    public static final RegiliteMenu<RedstoneCountFilterMenu> REDSTONE_COUNT_FILTER = MENU_REGISTRY
        .registerMenu("redstone_count_filter", RedstoneCountFilterMenu::factory, () -> RedstoneCountFilterScreen::new);

    public static void register(IEventBus bus) {
        MENU_REGISTRY.register(bus);
    }
}
