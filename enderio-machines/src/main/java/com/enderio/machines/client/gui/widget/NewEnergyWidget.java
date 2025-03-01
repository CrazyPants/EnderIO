package com.enderio.machines.client.gui.widget;

import com.enderio.base.api.EnderIO;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.client.gui.widgets.EIOWidget;
import com.enderio.core.common.util.TooltipUtil;
import com.enderio.machines.common.io.energy.ILargeMachineEnergyStorage;
import com.enderio.machines.common.io.energy.IMachineEnergyStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Supplier;

public class NewEnergyWidget extends EIOWidget {

    protected static final ResourceLocation ENERGY_BAR_FILL_SPRITE = EnderIO.loc("widget/energy_bar_fill");

    private final Supplier<IMachineEnergyStorage> storageSupplier;

    public NewEnergyWidget(int x, int y, Supplier<IMachineEnergyStorage> storageSupplier) {
        super(x, y, 18, 52);
        this.storageSupplier = storageSupplier;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Don't bother if we have no energy capacity, protects from divide by zero's when there's no capacitor.
        IMachineEnergyStorage storage = storageSupplier.get();
        if (storage.getMaxEnergyStored() <= 0) {
            return;
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        float filledVolume = (float)(getEnergyStored(storage) / (double) getMaxEnergyStored(storage));
        int renderableHeight = (int)(filledVolume * height);
        int hiddenHeight = height - renderableHeight;

        guiGraphics.blitSprite(ENERGY_BAR_FILL_SPRITE, width, height, 0, hiddenHeight, x, y + hiddenHeight, width, renderableHeight);

        RenderSystem.disableDepthTest();

        renderToolTip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }

    public void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) {
            Minecraft minecraft = Minecraft.getInstance();

            IMachineEnergyStorage storage = storageSupplier.get();

            NumberFormat fmt = NumberFormat.getInstance(Locale.ENGLISH);
            guiGraphics.renderTooltip(minecraft.font,
                TooltipUtil.withArgs(EIOLang.ENERGY_AMOUNT, fmt.format(getEnergyStored(storage)) + "/" + fmt.format(
               getMaxEnergyStored(storage))), mouseX, mouseY);

            guiGraphics.renderTooltip(minecraft.font, TooltipUtil.withArgs(EIOLang.ENERGY_IN, fmt.format(storage.getEnergyReceived())), mouseX,
                mouseY + minecraft.font.lineHeight + 1);

            guiGraphics.renderTooltip(minecraft.font, TooltipUtil.withArgs(EIOLang.ENERGY_OUT, fmt.format(storage.getEnergyUsed())), mouseX,
                mouseY + minecraft.font.lineHeight * 2 + 2);
        }
    }

    private static long getEnergyStored(IMachineEnergyStorage storage) {
        if (storage instanceof ILargeMachineEnergyStorage largeStorage) {
            return largeStorage.getLargeEnergyStored();
        }

        return storage.getEnergyStored();
    }

    private static long getMaxEnergyStored(IMachineEnergyStorage storage) {
        if (storage instanceof ILargeMachineEnergyStorage largeStorage) {
            return largeStorage.getLargeMaxEnergyStored();
        }

        return storage.getMaxEnergyStored();
    }
}

