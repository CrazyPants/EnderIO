package com.enderio.machines.client.gui.screen;

import com.enderio.base.api.EnderIO;
import com.enderio.base.client.gui.widget.EIOCommonWidgets;
import com.enderio.base.client.gui.widget.RedstoneControlPickerWidget;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.machines.client.gui.screen.base.MachineScreen;
import com.enderio.machines.client.gui.widget.ActivityWidget;
import com.enderio.machines.client.gui.widget.CapacitorEnergyWidget;
import com.enderio.machines.common.blocks.obelisks.aversion.AversionObeliskMenu;
import com.enderio.machines.common.blocks.wireless_charger.WirelessChargerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WirelessChargerScreen extends MachineScreen<WirelessChargerMenu> {

    private static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/screen/wireless_charger.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    public WirelessChargerScreen(WirelessChargerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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

        var overlay = addIOConfigOverlay(1, leftPos + 7, topPos + 83, 162, 76);
        addIOConfigButton(leftPos + imageWidth - 6 - 16, topPos + 24, overlay);

        int rangeOffset = 50;
        addRenderableWidget(EIOCommonWidgets.createRange(leftPos + imageWidth - 6 - 16, topPos + rangeOffset,
                EIOLang.HIDE_RANGE, EIOLang.SHOW_RANGE, menu::isRangeVisible,
                (ignored) -> handleButtonPress(AversionObeliskMenu.VISIBILITY_BUTTON_ID)));

        addRenderableWidget(EIOCommonWidgets.createRangeIncrease(leftPos + imageWidth - 2 * 16,
                topPos + rangeOffset + 1, (b) -> handleButtonPress(AversionObeliskMenu.INCREASE_BUTTON_ID)));

        addRenderableWidget(EIOCommonWidgets.createRangeDecrease(leftPos + imageWidth - 2 * 16,
                topPos + rangeOffset + 9, (b) -> handleButtonPress(AversionObeliskMenu.DECREASE_BUTTON_ID)));

        addRenderableWidget(
                new ActivityWidget(leftPos + imageWidth - 6 - 16, topPos + rangeOffset + 16, menu::getMachineStates));
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.blit(BG_TEXTURE, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);

        int rangeOffset = 50;
        guiGraphics.drawString(font, getMenu().getBlockEntity().getRange() + "",
                leftPos + imageWidth - 8 - 16 - font.width(getMenu().getBlockEntity().getRange() + "") - 10,
                topPos + rangeOffset + 5, 0, false);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        int rangeOffset = 50;
        guiGraphics.drawString(font, EIOLang.RANGE, imageWidth - 9 - font.width(EIOLang.RANGE), rangeOffset - 10,
                4210752, false);
        guiGraphics.drawString(font, EIOLang.MAX_RANGE, imageWidth / 2 - font.width(EIOLang.MAX_RANGE) / 2, 20, 0,
                false);
        String maxRange = getMenu().getBlockEntity().getMaxRange() + "";
        guiGraphics.drawString(font, maxRange, imageWidth / 2 - font.width(maxRange) / 2, 20 + font.lineHeight + 3, 0,
                false);
        super.renderLabels(guiGraphics, pMouseX, pMouseY);
    }
}
