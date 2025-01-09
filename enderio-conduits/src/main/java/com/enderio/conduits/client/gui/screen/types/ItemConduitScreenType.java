package com.enderio.conduits.client.gui.screen.types;

import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.api.menu.ConduitMenuDataAccess;
import com.enderio.conduits.api.screen.ConduitScreenHelper;
import com.enderio.conduits.api.screen.ConduitScreenType;
import com.enderio.conduits.common.conduit.type.item.ItemConduitConnectionConfig;
import com.enderio.conduits.common.init.ConduitLang;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class ItemConduitScreenType extends ConduitScreenType<ItemConduitConnectionConfig> {

    @Override
    public void createWidgets(ConduitScreenHelper screen, ConduitMenuDataAccess<ItemConduitConnectionConfig> dataAccess) {
        // Add insert/extract checkboxes.
        screen.addCheckbox(0, 0, () -> dataAccess.getConnectionConfig().isSend(),
            value -> dataAccess.updateConnectionConfig(config -> config.withIsSend(value)));

        screen.addCheckbox(90, 0, () -> dataAccess.getConnectionConfig().isReceive(),
            value -> dataAccess.updateConnectionConfig(config -> config.withIsReceive(value)));

        screen.addColorPicker(0, 20, ConduitLang.CONDUIT_CHANNEL, () -> dataAccess.getConnectionConfig().sendColor(),
            value -> dataAccess.updateConnectionConfig(config -> config.withSendColor(value)));

        screen.addColorPicker(90, 20, ConduitLang.CONDUIT_CHANNEL, () -> dataAccess.getConnectionConfig().receiveColor(),
            value -> dataAccess.updateConnectionConfig(config -> config.withReceiveColor(value)));

        var redstoneChannelWidget = screen.addColorPicker(90 + 20, 40, ConduitLang.REDSTONE_CHANNEL, () -> dataAccess.getConnectionConfig().receiveRedstoneChannel(),
            value -> dataAccess.updateConnectionConfig(config -> config.withReceiveRedstoneChannel(value)));

        // Only show the redstone widget when redstone control is sensitive to signals.
        screen.addPreRenderAction(
            () -> redstoneChannelWidget.visible = dataAccess.getConnectionConfig().receiveRedstoneControl().isRedstoneSensitive());

        screen.addRedstoneControlPicker(90, 40, EIOLang.REDSTONE_MODE, () -> dataAccess.getConnectionConfig().receiveRedstoneControl(),
            value -> dataAccess.updateConnectionConfig(config -> config.withReceiveRedstoneControl(value)));

        // TODO: Show redstone signal indicators (somehow)

        // TODO: Item-specific buttons
    }

    @Override
    public void renderLabels(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, font, mouseX, mouseY);

        guiGraphics.drawString(font, ConduitLang.CONDUIT_INSERT, 22 + 16 + 2, 7 + 4, 4210752, false);
        guiGraphics.drawString(font, ConduitLang.CONDUIT_EXTRACT, 22 + 90 + 16 + 2, 7 + 4, 4210752, false);
    }
}
