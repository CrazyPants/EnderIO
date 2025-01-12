package com.enderio.conduits.api.screen;

import com.enderio.conduits.api.connection.config.ConnectionConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public abstract class ConduitScreenType<U extends ConnectionConfig> {

    // TODO: Other built-in behaviours like this.
    // Could also create IOConduitScreenType with automated helpers for adding the
    // toggles.
    private boolean renderSideSeparator = false;

    public abstract void createWidgets(ConduitScreenHelper screen, ConduitMenuDataAccess<U> dataAccess);

    // TODO: Wrapper over guiGraphics? Means less breaks in the API?
    public void renderLabels(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY) {
    }
}
