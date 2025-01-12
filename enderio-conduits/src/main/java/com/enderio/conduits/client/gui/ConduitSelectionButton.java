package com.enderio.conduits.client.gui;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.client.gui.screen.NewConduitScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ConduitSelectionButton extends AbstractButton {
    private final Holder<Conduit<?, ?>> conduit;
    private final Supplier<Holder<Conduit<?, ?>>> getter;
    private final Consumer<Holder<Conduit<?, ?>>> setter;

    public ConduitSelectionButton(int pX, int pY, Holder<Conduit<?, ?>> conduit, Supplier<Holder<Conduit<?, ?>>> getter,
            Consumer<Holder<Conduit<?, ?>>> setter) {
        super(pX, pY, 21, 24, Component.empty());
        this.conduit = conduit;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    protected boolean isValidClickButton(int pButton) {
        return super.isValidClickButton(pButton) && getter.get() != conduit;
    }

    @Override
    public void onPress() {
        setter.accept(conduit);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        guiGraphics.blit(NewConduitScreen.TEXTURE, getX(), getY(), 227, 0, this.width, this.height);
        if (getter.get() == conduit) {
            guiGraphics.blit(NewConduitScreen.TEXTURE, getX() - 3, getY(), 224, 0, 3, this.height);
        }

        ResourceLocation iconLocation = MissingTextureAtlasSprite.getLocation();
        ResourceLocation conduitKey = conduit.unwrapKey().map(ResourceKey::location).orElse(null);
        if (conduitKey != null) {
            iconLocation = ResourceLocation.fromNamespaceAndPath(conduitKey.getNamespace(),
                    "conduit_icon/" + conduitKey.getPath());
        }

        guiGraphics.blitSprite(iconLocation, getX() + 3, getY() + 6, 12, 12);

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }

    public Holder<Conduit<?, ?>> getConduit() {
        return conduit;
    }
}
