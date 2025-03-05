package com.enderio.machines.client.gui.screen;

import com.enderio.base.api.EnderIO;
import com.enderio.machines.common.blocks.obelisks.inhibitor.InhibitorObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.inhibitor.InhibitorObeliskMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class InhibitorObeliskScreen extends ObeliskScreen<InhibitorObeliskBlockEntity, InhibitorObeliskMenu> {

    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/screen/inhibitor.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    public InhibitorObeliskScreen(InhibitorObeliskMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, BG_TEXTURE, WIDTH, HEIGHT);
    }

}
