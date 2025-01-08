package com.enderio.modconduits.mods.mekanism;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.api.network.node.legacy.ConduitDataAccessor;
import com.enderio.conduits.api.screen.ConduitScreenExtension;
import com.enderio.core.common.util.TooltipUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.api.chemical.ChemicalStack;
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
import org.joml.Vector2i;

import java.util.List;
import java.util.function.Supplier;

public final class ChemicalConduitScreenExtension implements ConduitScreenExtension {

    private static final ResourceLocation WIDGET_TEXTURE = EnderIO.loc("textures/gui/chemicalbackground.png");

    @Override
    public List<AbstractWidget> createWidgets(Screen screen, ConduitDataAccessor conduitDataAccessor, UpdateDispatcher updateConduitData,
        Supplier<Direction> direction, Vector2i widgetsStart) {
        if (conduitDataAccessor.getOrCreateData(MekanismModule.CHEMICAL_DATA_TYPE.get()).lockedChemical().isEmpty()) {
            return List.of();
        }
        return List.of(
            new ChemicalWidget(widgetsStart.add(0, 20),
                () -> conduitDataAccessor.getOrCreateData(MekanismModule.CHEMICAL_DATA_TYPE.get()).lockedChemical(),
                () -> {
                    ChemicalConduitData data = conduitDataAccessor.getOrCreateData(MekanismModule.CHEMICAL_DATA_TYPE.get());
                    data.setShouldReset(true);
                    updateConduitData.sendUpdate();
                })
            );
    }

    private static class ChemicalWidget extends AbstractWidget {
        private final Runnable onPress;
        private final Supplier<ChemicalStack> currentChemical;

        ChemicalWidget(Vector2i pos, Supplier<ChemicalStack> chemical, Runnable onPress) {
            super(pos.x(), pos.y(), 14, 14, Component.empty());
            this.onPress = onPress;
            this.currentChemical = chemical;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            if (isHoveredOrFocused()) {
                MutableComponent tooltip = MekanismModule.CHEMICAL_CONDUIT_CHANGE_FLUID1.copy();
                tooltip.append("\n").append(MekanismModule.CHEMICAL_CONDUIT_CHANGE_FLUID2);
                if (!currentChemical.get().isEmpty()) {
                    tooltip.append("\n").append(TooltipUtil.withArgs(MekanismModule.CHEMICAL_CONDUIT_CHANGE_FLUID3, currentChemical.get().getChemical().getTextComponent()));
                }
                setTooltip(Tooltip.create(TooltipUtil.style(tooltip)));
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            guiGraphics.blit(WIDGET_TEXTURE, getX(), getY(), 0, 0, this.width, this.height);
            if (currentChemical.get().isEmpty()) {
                return;
            }

            ResourceLocation still = currentChemical.get().getChemical().getIcon();
            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
            if (texture instanceof TextureAtlas atlas) {
                TextureAtlasSprite sprite = atlas.getSprite(still);

                int color = currentChemical.get().getChemicalTint();
                RenderSystem.setShaderColor( ((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, 1);
                RenderSystem.enableBlend();


                int atlasWidth = (int)(sprite.contents().width() / (sprite.getU1() - sprite.getU0()));
                int atlasHeight = (int)(sprite.contents().height() / (sprite.getV1() - sprite.getV0()));

                guiGraphics.blit(TextureAtlas.LOCATION_BLOCKS, getX() + 1, getY() + 1, 0, sprite.getU0()*atlasWidth, sprite.getV0()*atlasHeight, 12, 12, atlasWidth, atlasHeight);

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
