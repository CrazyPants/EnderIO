package com.enderio.core.client.gui.screen;

import com.enderio.core.common.menu.LegacyBaseBlockEntityMenu;
import com.enderio.core.common.menu.SlotWithOverlay;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public abstract class EnderContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    private static final int ITEM_RENDER_Z = 400;

    private final Multimap<Integer, Renderable> overlayRenderables = HashMultimap.create();
    private final Multimap<Integer, GuiEventListener> overlayWidgets = HashMultimap.create();

    private final Map<String, StateRestoringWidget> stateRestoringWidgets = new HashMap<>();

    // TODO: 1.21: Intention is that all screens will have labels in future.
    protected boolean shouldRenderLabels = false;

    public EnderContainerScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    /**
     * Utility for server-side button presses
     * @param id The button ID that was pressed.
     */
    protected void handleButtonPress(int id) {
        Objects.requireNonNull(this.getMinecraft().gameMode).handleInventoryButtonClick(getMenu().containerId, id);
    }

    protected void centerAlignTitleLabelX() {
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (menu instanceof LegacyBaseBlockEntityMenu<?> baseBlockEntityMenu
                && baseBlockEntityMenu.getBlockEntity() == null) {
            return;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (shouldRenderLabels) {
            super.renderLabels(guiGraphics, mouseX, mouseY);
        }

        // Move back to screen space rather than aligned to the background coordinates
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(-leftPos, -topPos, 0.0D);

        int zOffset = 200;
        for (var layer : overlayRenderables.keySet()) {
            // Offset deeper for each layer.
            guiGraphics.pose().pushPose();
            zOffset += 150;
            guiGraphics.pose().translate(0.0D, 0.0D, zOffset);

            for (var overlay : overlayRenderables.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    overlay.render(guiGraphics, mouseX, mouseY,
                            Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));

                    if (overlay instanceof BaseOverlay baseOverlay) {
                        zOffset += baseOverlay.getAdditionalZOffset();
                    }
                }
            }

            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().popPose();

        guiGraphics.pose().translate(0, 0, zOffset);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(-leftPos, -topPos, 0.0D);

        renderTooltip(guiGraphics, mouseX, mouseY);

        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        // Do not render tooltips if the mouse is over an overlay.
        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    if (overlay.isMouseOver(pX, pY)) {
                        return;
                    }
                }
            }
        }

        super.renderTooltip(pGuiGraphics, pX, pY);
    }

    @Override
    protected void renderSlotContents(GuiGraphics guiGraphics, ItemStack itemstack, Slot slot,
            @Nullable String countString) {
        super.renderSlotContents(guiGraphics, itemstack, slot, countString);

        if (slot instanceof SlotWithOverlay slotWithOverlay) {
            if (slotWithOverlay.getForegroundSprite() != null) {
                RenderSystem.disableDepthTest();
                guiGraphics.blitSprite(slotWithOverlay.getForegroundSprite(), slot.x, slot.y, 16, 16);
                RenderSystem.enableDepthTest();
            }
        }
    }

    public <U extends StateRestoringWidget> U addRestorableState(String key, U widget) {
        stateRestoringWidgets.put(key, widget);
        return widget;
    }

    public EditBox addRestorableState(String key, EditBox widget) {
        stateRestoringWidgets.put(key, new RestorableEditBox(widget));
        return widget;
    }

    public <U extends Renderable> U addOverlayRenderableOnly(int layer, U renderable) {
        overlayRenderables.put(layer, renderable);
        return renderable;
    }

    public <U extends Renderable & GuiEventListener> U addOverlayRenderable(int layer, U widget) {
        overlayRenderables.put(layer, widget);
        overlayWidgets.put(layer, widget);
        return widget;
    }

    @Override
    protected void clearWidgets() {
        overlayRenderables.clear();
        overlayWidgets.clear();
        stateRestoringWidgets.clear();
        super.clearWidgets();
    }

    @Override
    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        // Gather state to persist
        Map<String, Object> valuesBeforeResize = stateRestoringWidgets.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValueForRestore()));

        super.resize(pMinecraft, pWidth, pHeight);

        // Restore state
        for (String key : valuesBeforeResize.keySet()) {
            stateRestoringWidgets.get(key).restoreValue(valuesBeforeResize.get(key));
        }
    }

    // region Gui event passthrough

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    overlay.mouseMoved(pMouseX, pMouseY);
                }
            }
        }

        super.mouseMoved(pMouseX, pMouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    if (overlay.isMouseOver(pMouseX, pMouseY)) {
                        setFocused(overlay);
                        return overlay.mouseClicked(pMouseX, pMouseY, pButton);
                    }
                }
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    if (overlay.isMouseOver(pMouseX, pMouseY)) {
                        return overlay.mouseReleased(pMouseX, pMouseY, pButton);
                    }
                }
            }
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    // Always pass mouse drag event through widgets first.
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    if (overlay.isMouseOver(pMouseX, pMouseY)) {
                        return overlay.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
                    }
                }
            }
        }

        if (getFocused() instanceof AbstractWidget abstractWidget && abstractWidget.isActive()) {
            return abstractWidget.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    if (overlay.isMouseOver(pMouseX, pMouseY)) {
                        return overlay.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
                    }
                }
            }
        }

        return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    /**
     * @deprecated Use {@link #onKeyPressed(int, int, int)} instead.
     */
    @Deprecated
    @Override
    public final boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.player.closeContainer();
            return true;
        }

        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    if (overlay.keyPressed(keyCode, scanCode, modifiers)) {
                        return true;
                    }
                }
            }
        }

        if (onKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    if (overlay.keyReleased(pKeyCode, pScanCode, pModifiers)) {
                        return true;
                    }
                }
            }
        }

        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    if (overlay.charTyped(pCodePoint, pModifiers)) {
                        return true;
                    }
                }
            }
        }

        return super.charTyped(pCodePoint, pModifiers);
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent pEvent) {
        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    var path = overlay.nextFocusPath(pEvent);
                    if (path != null) {
                        return path;
                    }
                }
            }
        }

        return super.nextFocusPath(pEvent);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        for (var layer : overlayWidgets.keySet()) {
            for (var overlay : overlayWidgets.get(layer)) {
                if (!(overlay instanceof AbstractWidget widget) || widget.isActive()) {
                    if (overlay.isMouseOver(pMouseX, pMouseY)) {
                        return true;
                    }
                }
            }
        }

        return super.isMouseOver(pMouseX, pMouseY);
    }

    // endregion
}
