package com.enderio.conduits.client.gui.screen.types;

import com.enderio.base.api.EnderIO;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.api.screen.ConduitMenuDataAccess;
import com.enderio.conduits.api.screen.ConduitScreenHelper;
import com.enderio.conduits.api.screen.ConduitScreenType;
import com.enderio.conduits.common.conduit.type.fluid.FluidConduit;
import com.enderio.conduits.common.conduit.type.fluid.FluidConduitConnectionConfig;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.network.C2SClearLockedFluidPacket;
import com.enderio.core.common.util.TooltipUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Supplier;

public class FluidConduitScreenType extends ConduitScreenType<FluidConduitConnectionConfig> {

    private static final ResourceLocation ICON_ROUND_ROBIN_ENABLED = EnderIO.loc("icon/round_robin_enabled");
    private static final ResourceLocation ICON_ROUND_ROBIN_DISABLED = EnderIO.loc("icon/round_robin_disabled");
    private static final ResourceLocation ICON_SELF_FEED_ENABLED = EnderIO.loc("icon/self_feed_enabled");
    private static final ResourceLocation ICON_SELF_FEED_DISABLED = EnderIO.loc("icon/self_feed_disabled");

    @Override
    public void createWidgets(ConduitScreenHelper screen,
            ConduitMenuDataAccess<FluidConduitConnectionConfig> dataAccess) {

        int currentY = 0;

        // Add insert/extract checkboxes.
        screen.addCheckbox(0, 0, () -> dataAccess.getConnectionConfig().isSend(),
                value -> dataAccess.updateConnectionConfig(config -> config.withIsSend(value)));

        screen.addCheckbox(90, 0, () -> dataAccess.getConnectionConfig().isReceive(),
                value -> dataAccess.updateConnectionConfig(config -> config.withIsReceive(value)));

        currentY += 20;

        // Locked fluid widget
        if (dataAccess.conduit() instanceof FluidConduit fluidConduit && !fluidConduit.isMultiFluid()) {
            screen.addRenderableWidget(new FluidWidget(screen.getAreaLeft(), screen.getAreaTop() + currentY,
                () -> getLockedFluid(dataAccess),
                () -> PacketDistributor.sendToServer(new C2SClearLockedFluidPacket(dataAccess.getBlockPos()))));
        } else {
            // Channel colors
            screen.addColorPicker(0, currentY, ConduitLang.CONDUIT_CHANNEL, () -> dataAccess.getConnectionConfig().sendColor(),
                value -> dataAccess.updateConnectionConfig(config -> config.withSendColor(value)));

            screen.addColorPicker(90, currentY, ConduitLang.CONDUIT_CHANNEL,
                () -> dataAccess.getConnectionConfig().receiveColor(),
                value -> dataAccess.updateConnectionConfig(config -> config.withReceiveColor(value)));

            currentY += 20;
        }

        // TODO: Could be good fluid conduit features?
        /*// Round robin
        screen.addToggleButton(90 + 16 + 4, 20, 16, 16, ConduitLang.ROUND_ROBIN_ENABLED,
                ConduitLang.ROUND_ROBIN_DISABLED, ICON_ROUND_ROBIN_ENABLED, ICON_ROUND_ROBIN_DISABLED,
                () -> dataAccess.getConnectionConfig().isRoundRobin(),
                value -> dataAccess.updateConnectionConfig(config -> config.withIsRoundRobin(value)));

        // Self feed
        screen.addToggleButton(90 + (16 + 4) * 2, 20, 16, 16, ConduitLang.SELF_FEED_ENABLED,
                ConduitLang.SELF_FEED_DISABLED, ICON_SELF_FEED_ENABLED, ICON_SELF_FEED_DISABLED,
                () -> dataAccess.getConnectionConfig().isSelfFeed(),
                value -> dataAccess.updateConnectionConfig(config -> config.withIsSelfFeed(value)));*/

        // Redstone control
        var redstoneChannelWidget = screen.addColorPicker(90 + 16 + 4, currentY, ConduitLang.REDSTONE_CHANNEL,
                () -> dataAccess.getConnectionConfig().receiveRedstoneChannel(),
                value -> dataAccess.updateConnectionConfig(config -> config.withReceiveRedstoneChannel(value)));

        // Only show the redstone widget when redstone control is sensitive to signals.
        screen.addPreRenderAction(() -> redstoneChannelWidget.visible = dataAccess.getConnectionConfig()
                .receiveRedstoneControl()
                .isRedstoneSensitive());

        screen.addRedstoneControlPicker(90, currentY, EIOLang.REDSTONE_MODE,
                () -> dataAccess.getConnectionConfig().receiveRedstoneControl(),
                value -> dataAccess.updateConnectionConfig(config -> config.withReceiveRedstoneControl(value)));

        // TODO: Show redstone signal indicators using the extra NBT payload.
    }

    private Fluid getLockedFluid(ConduitMenuDataAccess<FluidConduitConnectionConfig> dataAccess) {
        var tag = dataAccess.getExtraGuiData();
        if (tag == null) {
            return Fluids.EMPTY;
        }

        if (!tag.contains("LockedFluid")) {
            return Fluids.EMPTY;
        }

        return BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString("LockedFluid")));
    }

    @Override
    public void renderLabels(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, font, mouseX, mouseY);

        guiGraphics.drawString(font, ConduitLang.CONDUIT_INSERT, 16 + 2, 4, 4210752, false);
        guiGraphics.drawString(font, ConduitLang.CONDUIT_EXTRACT, 90 + 16 + 2, 4, 4210752, false);
    }

    private static class FluidWidget extends AbstractWidget {
        private static final ResourceLocation WIDGET_TEXTURE = EnderIO.loc("textures/gui/fluidbackground.png");

        private final Runnable onPress;
        private final Supplier<Fluid> currentFluid;

        FluidWidget(int x, int y, Supplier<Fluid> fluid, Runnable onPress) {
            super(x, y, 14, 14, Component.empty());
            this.onPress = onPress;
            this.currentFluid = fluid;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            if (isHoveredOrFocused()) {
                MutableComponent tooltip = ConduitLang.FLUID_CONDUIT_CHANGE_FLUID1.copy();
                tooltip.append("\n").append(ConduitLang.FLUID_CONDUIT_CHANGE_FLUID2);
                if (!currentFluid.get().isSame(Fluids.EMPTY)) {
                    tooltip.append("\n")
                        .append(TooltipUtil.withArgs(ConduitLang.FLUID_CONDUIT_CHANGE_FLUID3,
                            currentFluid.get().getFluidType().getDescription()));
                }
                setTooltip(Tooltip.create(TooltipUtil.style(tooltip)));
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            guiGraphics.blit(WIDGET_TEXTURE, getX(), getY(), 0, 0, this.width, this.height);
            if (currentFluid.get().isSame(Fluids.EMPTY)) {
                return;
            }

            IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(currentFluid.get());
            ResourceLocation still = props.getStillTexture();
            AbstractTexture texture = Minecraft.getInstance()
                .getTextureManager()
                .getTexture(TextureAtlas.LOCATION_BLOCKS);
            if (texture instanceof TextureAtlas atlas) {
                TextureAtlasSprite sprite = atlas.getSprite(still);

                int color = props.getTintColor();
                RenderSystem.setShaderColor(FastColor.ARGB32.red(color) / 255.0F,
                    FastColor.ARGB32.green(color) / 255.0F, FastColor.ARGB32.blue(color) / 255.0F,
                    FastColor.ARGB32.alpha(color) / 255.0F);
                RenderSystem.enableBlend();

                int atlasWidth = (int) (sprite.contents().width() / (sprite.getU1() - sprite.getU0()));
                int atlasHeight = (int) (sprite.contents().height() / (sprite.getV1() - sprite.getV0()));

                guiGraphics.blit(TextureAtlas.LOCATION_BLOCKS, getX() + 1, getY() + 1, 0, sprite.getU0() * atlasWidth,
                    sprite.getV0() * atlasHeight, 12, 12, atlasWidth, atlasHeight);

                RenderSystem.setShaderColor(1, 1, 1, 1);
            }

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
        }

        @Override
        public void onClick(double pMouseX, double pMouseY) {
            onPress.run();
        }
    }
}
