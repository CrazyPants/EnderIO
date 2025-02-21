package com.enderio.machines.client.gui.screen;

import com.enderio.base.api.EnderIO;
import com.enderio.base.client.gui.widget.EIOCommonWidgets;
import com.enderio.base.client.gui.widget.RedstoneControlPickerWidget;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.machines.client.gui.screen.base.MachineScreen;
import com.enderio.machines.client.gui.widget.ActivityWidget;
import com.enderio.machines.client.gui.widget.CapacitorEnergyWidget;
import com.enderio.machines.common.blocks.obelisks.relocator.RelocatorObeliskMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RelocatorObeliskScreen extends MachineScreen<RelocatorObeliskMenu> {

    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/screen/inhibitor.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    public RelocatorObeliskScreen(RelocatorObeliskMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        addRenderableOnly(new CapacitorEnergyWidget(16 + leftPos, 14 + topPos, 9, 42, menu::getEnergyStorage,
                menu::isCapacitorInstalled));

        addRenderableWidget(new RedstoneControlPickerWidget(leftPos + imageWidth - 6 - 16, topPos + 6,
                menu::getRedstoneControl, menu::setRedstoneControl, EIOLang.REDSTONE_MODE));

        addRenderableWidget(EIOCommonWidgets.createRange(leftPos + imageWidth - 6 - 16, topPos + 34, EIOLang.HIDE_RANGE,
                EIOLang.SHOW_RANGE, menu::isRangeVisible,
                (ignored) -> handleButtonPress(RelocatorObeliskMenu.VISIBILITY_BUTTON_ID)));

        addRenderableWidget(EIOCommonWidgets.createRangeIncrease(leftPos + imageWidth - 2 * 16, topPos + 2 + 16 * 2,
                (b) -> handleButtonPress(RelocatorObeliskMenu.INCREASE_BUTTON_ID)));
        addRenderableWidget(EIOCommonWidgets.createRangeDecrease(leftPos + imageWidth - 2 * 16, topPos + 2 + 16 * 2 + 8,
                (b) -> handleButtonPress(RelocatorObeliskMenu.DECREASE_BUTTON_ID)));

        addRenderableWidget(new ActivityWidget(leftPos + imageWidth - 6 - 16, topPos + 16 * 4, menu::getMachineStates));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        guiGraphics.drawString(font, getMenu().getBlockEntity().getRange() + "",
                leftPos + imageWidth - 8 - 16 - font.width(getMenu().getBlockEntity().getRange() + "") - 10,
                topPos + 16 * 2 + 6, 0, false);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.blit(BG_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        guiGraphics.drawString(font, EIOLang.RANGE, imageWidth - 6 - font.width(EIOLang.RANGE), 16 + 8, 4210752, false);
        super.renderLabels(guiGraphics, pMouseX, pMouseY);
    }
}
