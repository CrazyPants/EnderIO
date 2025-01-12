package com.enderio.conduits.client.gui.screen.types;

import com.enderio.conduits.api.screen.ConduitMenuDataAccess;
import com.enderio.conduits.api.screen.ConduitScreenHelper;
import com.enderio.conduits.api.screen.ConduitScreenType;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitConnectionConfig;
import com.enderio.conduits.common.init.ConduitLang;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class RedstoneConduitScreenType extends ConduitScreenType<RedstoneConduitConnectionConfig> {
    @Override
    public void createWidgets(ConduitScreenHelper screen,
            ConduitMenuDataAccess<RedstoneConduitConnectionConfig> dataAccess) {
        // Add insert/extract checkboxes.
        screen.addCheckbox(0, 0, () -> dataAccess.getConnectionConfig().isReceive(),
                value -> dataAccess.updateConnectionConfig(config -> config.withIsReceive(value)));

        screen.addCheckbox(90, 0, () -> dataAccess.getConnectionConfig().isSend(),
                value -> dataAccess.updateConnectionConfig(config -> config.withIsSend(value)));

        // Channel colors
        screen.addColorPicker(0, 20, ConduitLang.CONDUIT_CHANNEL, () -> dataAccess.getConnectionConfig().receiveColor(),
                value -> dataAccess.updateConnectionConfig(config -> config.withReceiveColor(value)));

        screen.addColorPicker(90, 20, ConduitLang.CONDUIT_CHANNEL, () -> dataAccess.getConnectionConfig().sendColor(),
                value -> dataAccess.updateConnectionConfig(config -> config.withSendColor(value)));

        // Strong signal
        screen.addCheckbox(90, 40, () -> dataAccess.getConnectionConfig().isStrongOutputSignal(),
                value -> dataAccess.updateConnectionConfig(config -> config.withIsStrongOutputSignal(value)));
    }

    @Override
    public void renderLabels(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, font, mouseX, mouseY);

        guiGraphics.drawString(font, ConduitLang.CONDUIT_INPUT, 16 + 2, 4, 4210752, false);
        guiGraphics.drawString(font, ConduitLang.CONDUIT_OUTPUT, 90 + 16 + 2, 4, 4210752, false);

        guiGraphics.drawString(font, ConduitLang.CONDUIT_REDSTONE_SIGNAL_COLOR, 16 + 2, 20 + 4, 4210752, false);
        guiGraphics.drawString(font, ConduitLang.CONDUIT_REDSTONE_SIGNAL_COLOR, 90 + 16 + 2, 20 + 4, 4210752, false);

        guiGraphics.drawString(font, ConduitLang.CONDUIT_REDSTONE_STRONG_SIGNAL, 90 + 16 + 2, 40 + 4, 4210752, false);
    }
}
