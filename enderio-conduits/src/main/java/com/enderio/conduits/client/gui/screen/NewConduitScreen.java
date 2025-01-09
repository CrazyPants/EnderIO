package com.enderio.conduits.client.gui.screen;

import com.enderio.base.api.EnderIO;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.base.client.gui.widget.DyeColorPickerWidget;
import com.enderio.base.client.gui.widget.RedstoneControlPickerWidget;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.connection.config.io.ChanneledIOConnectionConfig;
import com.enderio.conduits.api.connection.config.io.IOConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.enderio.conduits.api.menu.ConduitMenuComponent;
import com.enderio.conduits.api.menu.ConduitMenuDataAccess;
import com.enderio.conduits.api.menu.ConduitMenuType;
import com.enderio.conduits.api.screen.ConduitScreenHelper;
import com.enderio.conduits.api.screen.ConduitScreenType;
import com.enderio.conduits.client.gui.screen.types.ConduitScreenTypes;
import com.enderio.conduits.common.conduit.menu.NewConduitMenu;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.network.connections.C2SSetConduitChannelPacket;
import com.enderio.conduits.common.network.connections.C2SSetConduitRedstoneChannelPacket;
import com.enderio.conduits.common.network.connections.C2SSetConduitRedstoneControlPacket;
import com.enderio.conduits.common.network.connections.SetConduitConnectionConfigPacket;
import com.enderio.core.client.gui.screen.EnderContainerScreen;
import com.enderio.core.client.gui.widgets.ToggleIconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NewConduitScreen extends EnderContainerScreen<NewConduitMenu> {
    public static final ResourceLocation TEXTURE = EnderIO.loc("textures/gui/conduit.png");
    private static final int WIDTH = 206;
    private static final int HEIGHT = 195;

    private final ScreenHelper screenHelper = new ScreenHelper();

    private final ConduitScreenTypeContainer<?> screenTypeContainer;

    private final List<Runnable> preRenderActions = new ArrayList<>();

    public NewConduitScreen(NewConduitMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

//        this.shouldRenderLabels = true;
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;

        // Get the screen type for this conduit, if available.
        this.screenTypeContainer = new ConduitScreenTypeContainer<>(menu.getSelectedConduit().value());
    }

    @Override
    protected void init() {
        super.init();
        preRenderActions.clear();

        if (screenTypeContainer.hasScreenType()) {
            screenTypeContainer.addWidgets(screenHelper);
            return;
        }

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

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        preRenderActions.forEach(Runnable::run);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // Example of conduit menu type thing.
//    private ConduitMenuType<ItemConduitConnectionConfig> EXAMPLE = ConduitMenuType.builder(ItemConduitConnectionConfig.TYPE)
//        .layout(/* TODO */) // << Ignore, i was going to do the enable buttons here, but I think I'll just make those components too.
//        .addComponent(new ConduitMenuComponent.ColorPicker<>(22, 7, ConduitLang.CONDUIT_CHANNEL,
//                ItemConduitConnectionConfig::insertChannel, ItemConduitConnectionConfig::withInputChannel))
//        .addComponent(new ConduitMenuComponent.ColorPicker<>(22, 112, ConduitLang.CONDUIT_CHANNEL,
//                ItemConduitConnectionConfig::extractChannel, ItemConduitConnectionConfig::withOutputChannel))
//        .addComponent(new ConduitMenuComponent.RedstoneControlPicker<>(22, 112, EIOLang.REDSTONE_MODE,
//                ItemConduitConnectionConfig::receiveRedstoneControl, ItemConduitConnectionConfig::withRedstoneControl))
//        .build();

    private <T extends ConnectionConfig> void addComponents(ConduitMenuType<T> menuType) {
        Supplier<T> config = () -> getConnectionConfig(menuType.connectionType());

        for (var component : menuType.components()) {
            int x = getGuiLeft() + component.x();
            int y = getGuiTop() + component.y();

            if (component instanceof ConduitMenuComponent.Text<T> text) {
                // TODO
            } else if (component instanceof ConduitMenuComponent.ToggleButton<T> toggleButton) {
                // TODO
            } else if (component instanceof ConduitMenuComponent.ColorPicker<T> colorPicker) {
                addRenderableWidget(new DyeColorPickerWidget(x, y,
                        () -> colorPicker.getter().apply(config.get()),
                        color -> {}, // TODO: Send packet with component ID and value
                        colorPicker.title()));
            } else if (component instanceof ConduitMenuComponent.RedstoneControlPicker<T> redstoneControlPicker) {
                // TODO
            }
        }
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

    private <T extends ConnectionConfig> T getConnectionConfig(ConnectionConfigType<T> configType) {
        return menu.connectionConfig(configType);
    }

    private IOConnectionConfig getIOConnectionConfig() {
        if (!menu.connectionConfigType().supportsIO()) {
            throw new IllegalStateException("Connection config type does not support IO");
        }

        if (!(getConnectionConfig() instanceof IOConnectionConfig ioConnectionConfig)) {
            throw new IllegalStateException("Connection config is not an IO connection config. Mismatch between connection type class and instance.");
        }

        return ioConnectionConfig;
    }

    private ChanneledIOConnectionConfig getChannelledIOConnectionConfig() {
        if (!menu.connectionConfigType().supportsIOChannels()) {
            throw new IllegalStateException("Connection config type does not support IO");
        }

        if (!(getConnectionConfig() instanceof ChanneledIOConnectionConfig chanelledIOConnectionConfig)) {
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

        if (screenTypeContainer.hasScreenType()) {
            screenTypeContainer.renderLabels(guiGraphics, mouseX, mouseY);
        } else {
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

//    private <T extends Conduit<T, U>, U extends ConnectionConfig> ConduitScreenHelper<U> createHelper() {
//
//    }

    // Due to the generics, the menu data access and screen type need to be contained here.
    private class ConduitScreenTypeContainer<U extends ConnectionConfig> {
        private final ConduitMenuDataAccess<U> dataAccess;

        @Nullable
        private final ConduitScreenType<U> screenType;

        public ConduitScreenTypeContainer(Conduit<?, U> conduit) {
            this.dataAccess = createDataAccess(conduit);
            this.screenType = ConduitScreenTypes.get(conduit.type());
        }

        public boolean hasScreenType() {
            return screenType != null;
        }

        public void addWidgets(ScreenHelper screenHelper) {
            if (screenType != null) {
                screenType.createWidgets(screenHelper, dataAccess);
            }
        }

        public void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (screenType != null) {
                screenType.renderLabels(guiGraphics, font, mouseX, mouseY);
            }
        }

        private <T extends Conduit<T, U>, U extends ConnectionConfig> ConduitMenuDataAccess<U> createDataAccess(Conduit<T, U> conduit) {
            return new ConduitMenuDataAccess<>() {
                @Override
                public U getConnectionConfig() {
                    return menu.connectionConfig(conduit.connectionConfigType());
                }

                @Override
                public void updateConnectionConfig(java.util.function.Function<U, U> configModifier) {
                    var newConfig = configModifier.apply(menu.connectionConfig(conduit.connectionConfigType()));

                    PacketDistributor.sendToServer(new SetConduitConnectionConfigPacket(menu.containerId, newConfig));

                    // Update on the client so UI is immediately in sync
                    menu.setConnectionConfig(newConfig);
                }

                @Override
                public CompoundTag getClientDataTag() {
                    return menu.getClientDataTag();
                }
            };
        }
    }

    private class ScreenHelper implements ConduitScreenHelper {

        @Override
        public int getAreaLeft() {
            return getGuiLeft() + 22;
        }

        @Override
        public int getAreaTop() {
            return getGuiTop() + 7;
        }

        @Override
        public int getUsableWidth() {
            // TODO
            return 0;
        }

        @Override
        public int getUsableHeight() {
            // TODO
            return 0;
        }

        // TODO: would be cool to make these relative?

        @Override
        public AbstractWidget addCheckbox(int x, int y, Supplier<Boolean> getter, Consumer<Boolean> setter) {
            var widget = ToggleIconButton.createCheckbox(getAreaLeft() + x, getAreaTop() + y, getter, setter);
            addRenderableWidget(widget);
            return widget;
        }

        @Override
        public AbstractWidget addColorPicker(int x, int y, Component title, Supplier<DyeColor> getter, Consumer<DyeColor> setter) {
            var widget = new DyeColorPickerWidget(getAreaLeft() + x, getAreaTop() + y, getter, setter, title);
            addRenderableWidget(widget);
            return widget;
        }

        @Override
        public AbstractWidget addRedstoneControlPicker(int x, int y, Component title, Supplier<RedstoneControl> getter, Consumer<RedstoneControl> setter) {
            var widget = new RedstoneControlPickerWidget(getAreaLeft() + x, getAreaTop() + y, getter, setter, title);
            addRenderableWidget(widget);
            return widget;
        }

        // Dynamic UI utilities

        @Override
        public void addPreRenderAction(Runnable runnable) {
            preRenderActions.add(runnable);
        }

        // Custom widgets

        @Override
        public <W extends GuiEventListener & NarratableEntry> W addWidget(W listener) {
            return NewConduitScreen.this.addWidget(listener);
        }

        @Override
        public <W extends Renderable> W addRenderableOnly(W renderable) {
            return NewConduitScreen.this.addRenderableOnly(renderable);
        }

        @Override
        public <W extends GuiEventListener & Renderable & NarratableEntry> W addRenderableWidget(W widget) {
            return NewConduitScreen.this.addRenderableWidget(widget);
        }

        @Override
        public void removeWidget(GuiEventListener listener) {
            NewConduitScreen.this.removeWidget(listener);
        }
    }
}
