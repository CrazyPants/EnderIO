package com.enderio.conduits.client.gui;

import com.enderio.base.api.EnderIO;
import com.enderio.base.client.gui.widget.DyeColorPickerWidget;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.menu.RedstoneCountFilterMenu;
import com.enderio.core.client.gui.screen.EIOScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector2i;

public class RedstoneCountFilterScreen extends EIOScreen<RedstoneCountFilterMenu> {
    private static final Vector2i BG_SIZE = new Vector2i(183,201);
    private static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/40/item_filter.png");

    public RedstoneCountFilterScreen(RedstoneCountFilterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new DyeColorPickerWidget(this.leftPos + 15, this.topPos + 30,
            getMenu().getFilter()::getChannel,
            getMenu()::setChannel,
            ConduitLang.REDSTONE_CHANNEL));

        EditBox pWidget = new EditBox(this.font, this.leftPos + 60, this.topPos + 20, 60, 20, Component.literal("" + getMenu().getFilter().getMaxCount())) {
            @Override
            public boolean charTyped(char pCodePoint, int pModifiers) {
                return Character.isDigit(pCodePoint) && super.charTyped(pCodePoint, pModifiers);
            }
        };
        pWidget.setValue("" + getMenu().getFilter().getMaxCount());
        addRenderableWidget(pWidget);
        addRenderableWidget(Button.builder(EIOLang.CONFIRM, pButton -> getMenu().setCount(pWidget.getValue()))
            .pos(this.leftPos + 60, this.topPos + 41)
            .size(60, 20)
            .build());

    }

    @Override
    public ResourceLocation getBackgroundImage() {
        return BG_TEXTURE;
    }

    @Override
    protected Vector2i getBackgroundImageSize() {
        return BG_SIZE;
    }
}
