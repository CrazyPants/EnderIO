package com.enderio.machines.client.gui.screen;

import com.enderio.base.api.EnderIO;
import com.enderio.machines.common.blocks.obelisks.aversion.AversionObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.aversion.AversionObeliskMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AversionObeliskScreen extends ObeliskScreen<AversionObeliskBlockEntity, AversionObeliskMenu> {

    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/screen/aversion.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    public AversionObeliskScreen(AversionObeliskMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, BG_TEXTURE, WIDTH, HEIGHT);
    }

}
