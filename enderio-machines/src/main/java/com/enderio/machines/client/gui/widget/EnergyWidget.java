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

public class EnergyWidget extends EIOWidget {

    protected static final ResourceLocation WIDGETS = EnderIO.loc("textures/gui/widgets.png");

    private final Supplier<IMachineEnergyStorage> storageSupplier;

    public EnergyWidget(int x, int y, int width, int height, Supplier<IMachineEnergyStorage> storageSupplier) {
        super(x, y, width, height);
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

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, height-16, 0);
        for (int i = 0; i < Math.ceil(renderableHeight / 16f); i++) {
            int drawingHeight = Math.min(16, renderableHeight - 16*i);
            int notDrawingHeight = 16 - drawingHeight;
            guiGraphics.blit(WIDGETS, x, y + notDrawingHeight, 0, 0, 128 + notDrawingHeight, width, drawingHeight, 256, 256);
            guiGraphics.pose().translate(0,-16, 0);
        }

        RenderSystem.disableDepthTest();
        guiGraphics.pose().popPose();

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

