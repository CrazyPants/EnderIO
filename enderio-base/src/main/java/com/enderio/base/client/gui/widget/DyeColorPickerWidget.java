package com.enderio.base.client.gui.widget;

import com.enderio.base.client.gui.icon.EIOEnumIcons;
import com.enderio.base.common.lang.EIOEnumLang;
import com.enderio.core.client.gui.widgets.BaseEnumPickerWidget;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

public class DyeColorPickerWidget extends BaseEnumPickerWidget<DyeColor> {

    // Reproduces the old ColorControl order for player familiarity.
    private static final DyeColor[] ORDERED_VALUES = new DyeColor[] { DyeColor.GREEN, DyeColor.BROWN, DyeColor.BLUE,
            DyeColor.PURPLE, DyeColor.CYAN, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.PINK, DyeColor.LIME,
            DyeColor.YELLOW, DyeColor.LIGHT_BLUE, DyeColor.MAGENTA, DyeColor.ORANGE, DyeColor.WHITE, DyeColor.BLACK,
            DyeColor.RED, };

    public DyeColorPickerWidget(int pX, int pY, Supplier<DyeColor> getter, Consumer<DyeColor> setter,
            Component optionName) {
        super(pX, pY, 16, 16, DyeColor.class, getter, setter, optionName);
    }

    @Override
    @Nullable
    public Component getValueTooltip(DyeColor value) {
        return EIOEnumLang.DYE_COLOR.get(value);
    }

    @Override
    public ResourceLocation getValueIcon(DyeColor value) {
        return EIOEnumIcons.DYE_COLOR.get(value);
    }

    @Override
    public DyeColor[] getValues() {
        return ORDERED_VALUES;
    }
}
