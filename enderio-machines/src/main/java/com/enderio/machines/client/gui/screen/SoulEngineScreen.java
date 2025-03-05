package com.enderio.machines.client.gui.screen;

import com.enderio.base.api.EnderIO;
import com.enderio.base.client.gui.widget.RedstoneControlPickerWidget;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.machines.client.gui.screen.base.MachineScreen;
import com.enderio.machines.client.gui.widget.ActivityWidget;
import com.enderio.machines.client.gui.widget.CapacitorEnergyWidget;
import com.enderio.machines.client.gui.widget.FluidStackWidget;
import com.enderio.machines.common.blocks.soul_engine.SoulEngineMenu;
import com.enderio.machines.common.souldata.EngineSoul;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;

public class SoulEngineScreen extends MachineScreen<SoulEngineMenu> {

    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/screen/soul_engine.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    public SoulEngineScreen(SoulEngineMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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

        addRenderableOnly(new FluidStackWidget(80 + leftPos, 21 + topPos, 16, 47, menu::getFluidTank));

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
        Optional<ResourceLocation> rl = getMenu().getBlockEntity().getEntityType();
        if (rl.isPresent()) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(rl.get());
            if (BuiltInRegistries.ENTITY_TYPE.getKey(type).equals(rl.get())) { // check we don't get the default pig
                String name = type.getDescription().getString();
                guiGraphics.drawString(font, name, imageWidth / 2f - font.width(name) / 2f, 10, 4210752, false);
            } else {
                guiGraphics.drawString(font, rl.get().toString(),
                        imageWidth / 2f - font.width(rl.get().toString()) / 2f, 10, 4210752, false);
            }
            EngineSoul.SoulData data = EngineSoul.ENGINE.map.get(rl.get());
            if (data != null) {
                double burnRate = menu.getBlockEntity().getBurnRate();
                float genRate = menu.getBlockEntity().getGenerationRate();
                double tickPerMb = data.tickpermb() / burnRate;
                float energyPerMb = data.powerpermb() * genRate;
                int generating = (int) Math.ceil(energyPerMb / tickPerMb);

                guiGraphics.drawString(font, data.tickpermb() / burnRate + " t/mb", imageWidth / 2f + 12, 40, 4210752,
                        false);
                guiGraphics.drawString(font, (int) (energyPerMb) + " µI/mb", imageWidth / 2f + 12, 50, 4210752, false);
                guiGraphics.drawString(font, generating + " µI/t", imageWidth / 2f + 12, 60, 4210752, false);

            }
        }

        super.renderLabels(guiGraphics, pMouseX, pMouseY);
    }
}
