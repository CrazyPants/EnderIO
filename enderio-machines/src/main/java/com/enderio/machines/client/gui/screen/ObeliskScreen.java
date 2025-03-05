package com.enderio.machines.client.gui.screen;

import com.enderio.base.client.gui.widget.EIOCommonWidgets;
import com.enderio.base.client.gui.widget.RedstoneControlPickerWidget;
import com.enderio.base.common.init.EIOItems;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.machines.client.gui.screen.base.MachineScreen;
import com.enderio.machines.client.gui.widget.ActivityWidget;
import com.enderio.machines.client.gui.widget.CapacitorEnergyWidget;
import com.enderio.machines.common.blocks.obelisks.ObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.ObeliskMenu;
import com.enderio.machines.common.blocks.obelisks.aversion.AversionObeliskMenu;
import com.enderio.machines.common.lang.MachineLang;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class ObeliskScreen<J extends ObeliskBlockEntity<J>, T extends ObeliskMenu<J>>
        extends MachineScreen<T> {

    private final ResourceLocation background;

    public ObeliskScreen(T pMenu, Inventory pPlayerInventory, Component pTitle, ResourceLocation background,
            int imageWidth, int imageHeight) {
        super(pMenu, pPlayerInventory, pTitle);
        this.background = background;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
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
                (ignored) -> handleButtonPress(AversionObeliskMenu.VISIBILITY_BUTTON_ID)));

        addRenderableWidget(EIOCommonWidgets.createRangeIncrease(leftPos + imageWidth - 2 * 16, topPos + 2 + 16 * 2,
                (b) -> handleButtonPress(AversionObeliskMenu.INCREASE_BUTTON_ID)));
        addRenderableWidget(EIOCommonWidgets.createRangeDecrease(leftPos + imageWidth - 2 * 16, topPos + 2 + 16 * 2 + 8,
                (b) -> handleButtonPress(AversionObeliskMenu.DECREASE_BUTTON_ID)));

        addRenderableWidget(new ActivityWidget(leftPos + imageWidth - 6 - 16, topPos + 16 * 4, menu::getMachineStates));
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        pGuiGraphics.blit(background, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        guiGraphics.drawString(font, getMenu().getBlockEntity().getRange() + "",
                leftPos + imageWidth - 8 - 16 - font.width(getMenu().getBlockEntity().getRange() + "") - 10,
                topPos + 16 * 2 + 6, 0, false);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        guiGraphics.drawString(font, EIOLang.RANGE, imageWidth - 6 - font.width(EIOLang.RANGE), 16 + 8, 4210752, false);

//        guiGraphics.drawString(font, EIOLang.MAX_RANGE, imageWidth / 2 - font.width(EIOLang.MAX_RANGE) / 2, 5, 0,
//            false);
//        String maxRange = getMenu().getBlockEntity().getMaxRange() + "";
//        guiGraphics.drawString(font, maxRange, imageWidth / 2 - font.width(maxRange) / 2, 5 + font.lineHeight + 3, 0,
//            false);

        if (MachineLang.OBELISK_UPKEEP.getContents() instanceof TranslatableContents translatableContents) {
            MutableComponent comp = Component.translatable(translatableContents.getKey(),
                    getMenu().getBlockEntity().getPerTickEnergyCost());
            guiGraphics.drawString(font, comp, imageWidth / 2 - font.width(EIOLang.MAX_RANGE) / 2, 62, 0, false);
        }

        super.renderLabels(guiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderSlotContents(GuiGraphics guiGraphics, ItemStack itemstack, Slot slot,
            @Nullable String countString) {
        if (menu.getBlockEntity().requiresFilter() && slot.index == 1 && itemstack.isEmpty()) {
            ItemStack stack = new ItemStack(EIOItems.ENTITY_FILTER.asItem());
            RenderSystem.setShaderColor(1, 1, 1, 100 / 255f);
            RenderSystem.enableBlend();
            guiGraphics.renderFakeItem(stack, slot.x, slot.y);
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);

        } else {
            super.renderSlotContents(guiGraphics, itemstack, slot, countString);
        }
    }

}
