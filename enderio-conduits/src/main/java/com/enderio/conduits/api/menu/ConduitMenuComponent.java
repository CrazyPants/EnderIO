package com.enderio.conduits.api.menu;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.mojang.datafixers.types.Func;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

// TODO: is this stupid?
// It allows most menus to be defined without the need for custom widget logic.
@ApiStatus.Experimental
public sealed interface ConduitMenuComponent<T extends ConnectionConfig> {
    int x();
    int y();

    record Text<T extends ConnectionConfig>(int x, int y, Component text) implements ConduitMenuComponent<T> {
    }

    record ToggleButton<T extends ConnectionConfig>(int x, int y, Component title, ResourceLocation enabledSprite, ResourceLocation disabledSprite, Function<T, Boolean> getter, Function<T, T> onToggle) implements ConduitMenuComponent<T> {
    }

    record ColorPicker<T extends ConnectionConfig>(int x, int y, Component title, Function<T, DyeColor> getter, BiFunction<T, DyeColor, T> setter) implements ConduitMenuComponent<T> {
    }

    record RedstoneControlPicker<T extends ConnectionConfig>(int x, int y, Component title, Function<T, RedstoneControl> getter, BiFunction<T, RedstoneControl, T> setter) implements ConduitMenuComponent<T> {
    }
}
