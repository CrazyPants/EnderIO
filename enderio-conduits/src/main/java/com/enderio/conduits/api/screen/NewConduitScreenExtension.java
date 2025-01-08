package com.enderio.conduits.api.screen;

import com.enderio.conduits.api.menu.ConduitMenuData;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import org.joml.Vector2i;

import java.util.List;

/**
 * Extend the conduit screen with additional widgets.
 */
public interface NewConduitScreenExtension<T extends ConduitMenuData> {

    List<AbstractWidget> createWidgets(Screen screen, T menuData, CompoundTag clientDataTag, Vector2i widgetsStart);
}
