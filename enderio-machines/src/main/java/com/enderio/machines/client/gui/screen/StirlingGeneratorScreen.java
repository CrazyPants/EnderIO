package com.enderio.machines.client.gui.screen;

import com.enderio.base.api.EnderIO;
import com.enderio.base.client.gui.widget.RedstoneControlPickerWidget;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.machines.client.gui.screen.base.MachineScreen;
import com.enderio.machines.client.gui.widget.ActivityWidget;
import com.enderio.machines.client.gui.widget.CapacitorEnergyWidget;
import com.enderio.machines.client.gui.widget.ProgressWidget;
import com.enderio.machines.common.blocks.stirling_generator.StirlingGeneratorMenu;
import com.enderio.machines.common.lang.MachineLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class StirlingGeneratorScreen extends MachineScreen<StirlingGeneratorMenu> {
    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/screen/stirling_generator.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    public StirlingGeneratorScreen(StirlingGeneratorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableOnly(new ProgressWidget.BottomUp(BG_TEXTURE, menu::getBurnProgress, getGuiLeft() + 81,
                getGuiTop() + 53, 14, 14, 176, 0));

        addRenderableOnly(new CapacitorEnergyWidget(16 + leftPos, 14 + topPos, 9, 42, menu::getEnergyStorage,
                menu::isCapacitorInstalled));

        addRenderableWidget(new RedstoneControlPickerWidget(leftPos + imageWidth - 6 - 16, topPos + 6,
                menu::getRedstoneControl, menu::setRedstoneControl, EIOLang.REDSTONE_MODE));

        addRenderableWidget(new ActivityWidget(leftPos + imageWidth - 6 - 16, topPos + 16 * 4, menu::getMachineStates));

        var overlay = addIOConfigOverlay(1, leftPos + 7, topPos + 83, 162, 76);
        addIOConfigButton(leftPos + imageWidth - 6 - 16, topPos + 24, overlay);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.blit(BG_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        if (MachineLang.GENERATING.getContents() instanceof TranslatableContents translatableContents) {
            int generating = menu.getBurnProgress() > 0 ? getMenu().getBlockEntity().getGenerationRate() : 0;
            MutableComponent comp = Component.translatable(translatableContents.getKey(), generating);
            guiGraphics.drawString(font, comp, imageWidth / 2 - font.width(comp.getString()) / 2, 9, 0, false);
            if (MachineLang.FUEL_EFFICIENCY.getContents() instanceof TranslatableContents conts) {
                comp = Component.translatable(conts.getKey(), getMenu().getBlockEntity().getFuelEfficiency());
                guiGraphics.drawString(font, comp, imageWidth / 2 - font.width(comp.getString()) / 2,
                        9 + font.lineHeight + 2, 0, false);
            }
        }
        super.renderLabels(guiGraphics, pMouseX, pMouseY);
    }
}
