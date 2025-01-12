package com.enderio.conduits.client.gui.screen.filter;

import com.enderio.base.api.EnderIO;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.common.menu.RedstoneTimerFilterMenu;
import com.enderio.core.client.gui.screen.EIOScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector2i;

public class RedstoneTimerFilterScreen extends EIOScreen<RedstoneTimerFilterMenu> {

    private static final Vector2i BG_SIZE = new Vector2i(183, 201);
    private static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/40/item_filter.png");

    public RedstoneTimerFilterScreen(RedstoneTimerFilterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        EditBox pWidget = new EditBox(this.font, this.leftPos + 60, this.topPos + 20, 60, 20,
                Component.literal("" + getMenu().getFilter().getMaxTicks())) {
            @Override
            public boolean charTyped(char pCodePoint, int pModifiers) {
                return Character.isDigit(pCodePoint) && super.charTyped(pCodePoint, pModifiers);
            }
        };
        pWidget.setValue("" + getMenu().getFilter().getMaxTicks());
        addRenderableWidget(pWidget);
        addRenderableWidget(Button.builder(EIOLang.CONFIRM, pButton -> getMenu().setTimer(pWidget.getValue()))
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
