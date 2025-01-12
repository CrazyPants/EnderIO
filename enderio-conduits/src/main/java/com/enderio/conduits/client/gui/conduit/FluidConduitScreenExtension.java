package com.enderio.conduits.client.gui.conduit;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.api.network.node.legacy.ConduitDataAccessor;
import com.enderio.conduits.api.screen.ConduitScreenExtension;
import com.enderio.conduits.common.conduit.legacy.LegacyFluidConduitData;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.core.common.util.TooltipUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Vector2i;

public final class FluidConduitScreenExtension implements ConduitScreenExtension {

    private static final ResourceLocation WIDGET_TEXTURE = EnderIO.loc("textures/gui/fluidbackground.png");

    @Override
    public List<AbstractWidget> createWidgets(Screen screen, ConduitDataAccessor conduitDataAccessor,
            UpdateDispatcher updateConduitData, Supplier<Direction> direction, Vector2i widgetsStart) {
        if (conduitDataAccessor.getOrCreateData(ConduitTypes.Data.FLUID.get()).lockedFluid().isSame(Fluids.EMPTY)) {
            return List.of();
        }
        return List.of(new FluidWidget(widgetsStart.add(0, 20),
                () -> conduitDataAccessor.getOrCreateData(ConduitTypes.Data.FLUID.get()).lockedFluid(), () -> {
                    LegacyFluidConduitData data = conduitDataAccessor.getOrCreateData(ConduitTypes.Data.FLUID.get());
                    data.setShouldReset(true);
                    updateConduitData.sendUpdate();
                }));
    }

    private static class FluidWidget extends AbstractWidget {
        private final Runnable onPress;
        private final Supplier<Fluid> currentFluid;

        FluidWidget(Vector2i pos, Supplier<Fluid> fluid, Runnable onPress) {
            super(pos.x(), pos.y(), 14, 14, Component.empty());
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
