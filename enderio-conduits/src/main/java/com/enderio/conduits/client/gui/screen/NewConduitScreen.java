package com.enderio.conduits.client.gui.screen;

import com.enderio.base.api.EnderIO;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.base.client.gui.widget.DyeColorPickerWidget;
import com.enderio.base.client.gui.widget.RedstoneControlPickerWidget;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.io.ChannelResourceConnectionConfig;
import com.enderio.conduits.api.connection.config.io.ResourceConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.enderio.conduits.common.conduit.menu.NewConduitMenu;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.network.connections.C2SSetConduitChannelPacket;
import com.enderio.conduits.common.network.connections.C2SSetConduitRedstoneChannelPacket;
import com.enderio.conduits.common.network.connections.C2SSetConduitRedstoneControlPacket;
import com.enderio.core.client.gui.screen.EnderContainerScreen;
import com.enderio.core.client.gui.widgets.ToggleIconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class NewConduitScreen extends EnderContainerScreen<NewConduitMenu> {
    public static final ResourceLocation TEXTURE = EnderIO.loc("textures/gui/conduit.png");
    private static final int WIDTH = 206;
    private static final int HEIGHT = 195;

    public NewConduitScreen(NewConduitMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

//        this.shouldRenderLabels = true;
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        // Left column
        int leftX = getGuiLeft() + 22;
        int leftY = getGuiTop() + 7;

        // Right column
        int rightX = getGuiLeft() + 22 + 90;
        int rightY = getGuiTop() + 7;

        // Add enable checkboxes
        if (menu.connectionConfigType().supportsIO()) {
            addRenderableWidget(ToggleIconButton.createCheckbox(leftX, leftY, () -> getIOConnectionConfig().canInsert(),
                newVal -> handleButtonPress(NewConduitMenu.BUTTON_TOGGLE_0_ID)));
            addRenderableWidget(ToggleIconButton.createCheckbox(rightX, rightY, () -> getIOConnectionConfig().canExtract(),
                newVal -> handleButtonPress(NewConduitMenu.BUTTON_TOGGLE_1_ID)));

            leftY += 20;
            rightY += 20;
        } else {
            addRenderableWidget(ToggleIconButton.createCheckbox(leftX, leftY, this::isConnected,
                newVal -> handleButtonPress(NewConduitMenu.BUTTON_TOGGLE_0_ID)));

            leftY += 20;
        }

        // Channels
        if (menu.connectionConfigType().supportsIOChannels()) {
            addRenderableWidget(new DyeColorPickerWidget(leftX, leftY,
                () -> getChannelledIOConnectionConfig().insertChannel(),
                color -> PacketDistributor.sendToServer(new C2SSetConduitChannelPacket(pos(), menu.getSide(), menu.getSelectedConduit(),
                    C2SSetConduitChannelPacket.Side.INPUT, color)),
                ConduitLang.CONDUIT_CHANNEL));

            addRenderableWidget(new DyeColorPickerWidget(rightX, rightY,
                () -> getChannelledIOConnectionConfig().extractChannel(),
                color -> PacketDistributor.sendToServer(new C2SSetConduitChannelPacket(pos(), menu.getSide(), menu.getSelectedConduit(),
                    C2SSetConduitChannelPacket.Side.OUTPUT, color)),
                ConduitLang.CONDUIT_CHANNEL));

            leftY += 20;
            rightY += 20;
        }

        // Redstone Control
        if (menu.connectionConfigType().supportsRedstoneControl()) {
            final AbstractWidget redstoneControlButton = addRenderableWidget(new DyeColorPickerWidget(rightX + 20, rightY,
                () -> getRedstoneControlledConnectionConfig().redstoneChannel(),
                color -> PacketDistributor.sendToServer(new C2SSetConduitRedstoneChannelPacket(pos(), menu.getSide(), menu.getSelectedConduit(), color)),
                ConduitLang.REDSTONE_CHANNEL));

            RedstoneControl redstoneControl = getRedstoneControlledConnectionConfig().redstoneControl();
            redstoneControlButton.visible = redstoneControl != RedstoneControl.NEVER_ACTIVE &&
                redstoneControl != RedstoneControl.ALWAYS_ACTIVE;

            addRenderableWidget(new RedstoneControlPickerWidget(rightX, rightY,
                () -> getRedstoneControlledConnectionConfig().redstoneControl(),
                mode -> {
                    PacketDistributor.sendToServer(new C2SSetConduitRedstoneControlPacket(pos(), menu.getSide(), menu.getSelectedConduit(), mode));
                    redstoneControlButton.visible = mode != RedstoneControl.NEVER_ACTIVE && mode != RedstoneControl.ALWAYS_ACTIVE;
                },
                EIOLang.REDSTONE_MODE));

            rightY += 20;
        }

        // TODO: Screen extensions

        // TODO: Conduit selection buttons
    }

    private BlockPos pos() {
        return menu.getBlockEntity().getBlockPos();
    }

    private boolean isConnected() {
        return menu.isConnected();
    }

    private ConnectionConfig getConnectionConfig() {
        return menu.connectionConfig();
    }

    private ResourceConnectionConfig getIOConnectionConfig() {
        if (!menu.connectionConfigType().supportsIO()) {
            throw new IllegalStateException("Connection config type does not support IO");
        }

        if (!(getConnectionConfig() instanceof ResourceConnectionConfig resourceConnectionConfig)) {
            throw new IllegalStateException("Connection config is not an IO connection config. Mismatch between connection type class and instance.");
        }

        return resourceConnectionConfig;
    }

    private ChannelResourceConnectionConfig getChannelledIOConnectionConfig() {
        if (!menu.connectionConfigType().supportsIOChannels()) {
            throw new IllegalStateException("Connection config type does not support IO");
        }

        if (!(getConnectionConfig() instanceof ChannelResourceConnectionConfig chanelledIOConnectionConfig)) {
            throw new IllegalStateException("Connection config is not an IO connection config. Mismatch between connection type class and instance.");
        }

        return chanelledIOConnectionConfig;
    }

    private RedstoneControlledConnection getRedstoneControlledConnectionConfig() {
        if (!menu.connectionConfigType().supportsRedstoneControl()) {
            throw new IllegalStateException("Connection config type does not support redstone control");
        }

        if (!(getConnectionConfig() instanceof RedstoneControlledConnection redstoneControlledConnection)) {
            throw new IllegalStateException("Connection config is not a redstone controlled connection config. Mismatch between connection type class and instance.");
        }

        return redstoneControlledConnection;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.blit(TEXTURE, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);

        if (menu.connectionConfigType().supportsIO()) {
            guiGraphics.blit(TEXTURE, getGuiLeft() + 102, getGuiTop() + 7, 255, 0, 1, 97);
        }

        // TODO
        for (SlotType type : SlotType.values()) {
//            if (type.isAvailableFor(data)) {
//                guiGraphics.blit(TEXTURE, type.getX() - 1, type.getY() - 1, 206, 0, 18, 18);
//            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        if (menu.connectionConfigType().supportsIO()) {
            // TODO: Test for a signal type -or- adopt input/output as the standard.
            guiGraphics.drawString(this.font, ConduitLang.CONDUIT_INSERT, 22 + 16 + 2, 7 + 4, 4210752, false);
            guiGraphics.drawString(this.font, ConduitLang.CONDUIT_EXTRACT, 112 + 16 + 2, 7 + 4, 4210752, false);

//            guiGraphics.drawString(this.font, ConduitLang.CONDUIT_INPUT, 22 + 16 + 2, 7 + 4, 4210752, false);
//            guiGraphics.drawString(this.font, ConduitLang.CONDUIT_OUTPUT, 112 + 16 + 2, 7 + 4, 4210752, false);
        } else {
            guiGraphics.drawString(this.font, ConduitLang.CONDUIT_ENABLED, 22 + 16 + 2, 7 + 4, 4210752, false);
        }
    }
}
