package com.enderio.conduits.api.screen;

import com.enderio.base.api.misc.RedstoneControl;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Experimental
public interface ConduitScreenHelper {

    // Positions
    int getAreaLeft();
    int getAreaTop();
    int getUsableWidth();
    int getUsableHeight();

    // Built-in widget support
    AbstractWidget addCheckbox(int x, int y, Supplier<Boolean> getter, Consumer<Boolean> setter);

    AbstractWidget addColorPicker(int x, int y, Component title, Supplier<DyeColor> getter, Consumer<DyeColor> setter);

    AbstractWidget addRedstoneControlPicker(int x, int y, Component title, Supplier<RedstoneControl> getter, Consumer<RedstoneControl> setter);

    AbstractWidget addToggleButton(int x, int y, int width, int height, Component enabledTitle, Component disabledTitle, ResourceLocation enabledSprite,
        ResourceLocation disabledSprite, Supplier<Boolean> getter, Consumer<Boolean> setter);

    // TODO: Create icon button

    // TODO: Create custom picker?

    // Dynamic UI utilities
    void addPreRenderAction(Runnable runnable);

    // Custom widget support

    <W extends GuiEventListener & Renderable & NarratableEntry> W addRenderableWidget(W widget);
    <W extends Renderable> W addRenderableOnly(W renderable);
    <W extends GuiEventListener & NarratableEntry> W addWidget(W listener);
    void removeWidget(GuiEventListener listener);
}
