package com.enderio.machines.client.gui.screen;

import com.enderio.base.api.EnderIO;
import com.enderio.machines.common.blocks.obelisks.relocator.RelocatorObeliskBlockEntity;
import com.enderio.machines.common.blocks.obelisks.relocator.RelocatorObeliskMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RelocatorObeliskScreen extends ObeliskScreen<RelocatorObeliskBlockEntity, RelocatorObeliskMenu> {

    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/screen/relocator.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    public RelocatorObeliskScreen(RelocatorObeliskMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, BG_TEXTURE, WIDTH, HEIGHT);
    }

}
