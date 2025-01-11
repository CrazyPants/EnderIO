package com.enderio.conduits.client.gui.screen.types;

import com.enderio.base.api.EnderIO;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.api.menu.ConduitMenuDataAccess;
import com.enderio.conduits.api.screen.ConduitScreenHelper;
import com.enderio.conduits.api.screen.ConduitScreenType;
import com.enderio.conduits.common.conduit.type.item.ItemConduitConnectionConfig;
import com.enderio.conduits.common.init.ConduitLang;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class ItemConduitScreenType extends ConduitScreenType<ItemConduitConnectionConfig> {

    private static final ResourceLocation ICON_ROUND_ROBIN_ENABLED = EnderIO.loc("icon/round_robin_enabled");
    private static final ResourceLocation ICON_ROUND_ROBIN_DISABLED = EnderIO.loc("icon/round_robin_disabled");
    private static final ResourceLocation ICON_SELF_FEED_ENABLED = EnderIO.loc("icon/self_feed_enabled");
    private static final ResourceLocation ICON_SELF_FEED_DISABLED = EnderIO.loc("icon/self_feed_disabled");

    @Override
    public void createWidgets(ConduitScreenHelper screen, ConduitMenuDataAccess<ItemConduitConnectionConfig> dataAccess) {
        // Add insert/extract checkboxes.
        screen.addCheckbox(0, 0, () -> dataAccess.getConnectionConfig().isSend(),
            value -> dataAccess.updateConnectionConfig(config -> config.withIsSend(value)));

        screen.addCheckbox(90, 0, () -> dataAccess.getConnectionConfig().isReceive(),
            value -> dataAccess.updateConnectionConfig(config -> config.withIsReceive(value)));

        // Channel colors
        screen.addColorPicker(0, 20, ConduitLang.CONDUIT_CHANNEL, () -> dataAccess.getConnectionConfig().sendColor(),
            value -> dataAccess.updateConnectionConfig(config -> config.withSendColor(value)));

        screen.addColorPicker(90, 20, ConduitLang.CONDUIT_CHANNEL, () -> dataAccess.getConnectionConfig().receiveColor(),
            value -> dataAccess.updateConnectionConfig(config -> config.withReceiveColor(value)));

        // Round robin
        screen.addToggleButton(90 + 16 + 4, 20, 16, 16, ConduitLang.ROUND_ROBIN_ENABLED, ConduitLang.ROUND_ROBIN_DISABLED,
            ICON_ROUND_ROBIN_ENABLED, ICON_ROUND_ROBIN_DISABLED, () -> dataAccess.getConnectionConfig().isRoundRobin(),
            value -> dataAccess.updateConnectionConfig(config -> config.withIsRoundRobin(value)));

        // Self feed
        screen.addToggleButton(90 + (16 + 4) * 2, 20, 16, 16, ConduitLang.SELF_FEED_ENABLED, ConduitLang.SELF_FEED_DISABLED,
            ICON_SELF_FEED_ENABLED, ICON_SELF_FEED_DISABLED, () -> dataAccess.getConnectionConfig().isSelfFeed(),
            value -> dataAccess.updateConnectionConfig(config -> config.withIsSelfFeed(value)));

        // Redstone control
        var redstoneChannelWidget = screen.addColorPicker(90 + 16 + 4, 40, ConduitLang.REDSTONE_CHANNEL, () -> dataAccess.getConnectionConfig().receiveRedstoneChannel(),
            value -> dataAccess.updateConnectionConfig(config -> config.withReceiveRedstoneChannel(value)));

        // Only show the redstone widget when redstone control is sensitive to signals.
        screen.addPreRenderAction(
            () -> redstoneChannelWidget.visible = dataAccess.getConnectionConfig().receiveRedstoneControl().isRedstoneSensitive());

        screen.addRedstoneControlPicker(90, 40, EIOLang.REDSTONE_MODE, () -> dataAccess.getConnectionConfig().receiveRedstoneControl(),
            value -> dataAccess.updateConnectionConfig(config -> config.withReceiveRedstoneControl(value)));

        // TODO: Show redstone signal indicators (somehow)
    }

    @Override
    public void renderLabels(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, font, mouseX, mouseY);

        guiGraphics.drawString(font, ConduitLang.CONDUIT_INSERT, 16 + 2, 4, 4210752, false);
        guiGraphics.drawString(font, ConduitLang.CONDUIT_EXTRACT, 90 + 16 + 2,  4, 4210752, false);
    }
}
