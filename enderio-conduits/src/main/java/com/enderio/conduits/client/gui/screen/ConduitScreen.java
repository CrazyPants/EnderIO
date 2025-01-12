package com.enderio.conduits.client.gui.screen;

import com.enderio.base.api.EnderIO;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.base.client.gui.widget.DyeColorPickerWidget;
import com.enderio.base.client.gui.widget.RedstoneControlPickerWidget;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.screen.ConduitMenuDataAccess;
import com.enderio.conduits.api.screen.ConduitScreenHelper;
import com.enderio.conduits.api.screen.ConduitScreenType;
import com.enderio.conduits.client.gui.NewConduitSelectionButton;
import com.enderio.conduits.client.gui.screen.types.ConduitScreenTypes;
import com.enderio.conduits.common.conduit.menu.ConduitMenu;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.network.SetConduitConnectionConfigPacket;
import com.enderio.core.client.gui.screen.EnderContainerScreen;
import com.enderio.core.client.gui.widgets.ToggleIconButton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
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

public class ConduitScreen extends EnderContainerScreen<ConduitMenu> {
    public static final ResourceLocation TEXTURE = EnderIO.loc("textures/gui/conduit.png");
    private static final int WIDTH = 206;
    private static final int HEIGHT = 195;

    private final ScreenHelper screenHelper = new ScreenHelper();

    private final ConduitScreenTypeContainer<?> screenTypeContainer;

    private final List<Runnable> preRenderActions = new ArrayList<>();

    public ConduitScreen(ConduitMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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
        }

        for (int i = 0; i < 9; i++) {
            addRenderableWidget(new NewConduitSelectionButton(getGuiLeft() + 206, getGuiTop() + 4 + 24 * i, i,
                    menu::getSelectedConduit, menu::getConnectedConduits,
                    idx -> handleButtonPress(ConduitMenu.BUTTON_CHANGE_CONDUIT_START_ID + idx)));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        preRenderActions.forEach(Runnable::run);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        guiGraphics.blit(TEXTURE, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);

//        if (menu.connectionConfigType().supportsIO()) {
//            guiGraphics.blit(TEXTURE, getGuiLeft() + 102, getGuiTop() + 7, 255, 0, 1, 97);
//        }

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
            guiGraphics.drawString(this.font, ConduitLang.CONDUIT_ERROR_NO_SCREEN_TYPE, 22, 7 + 4, 0xffff5733, false);
        }
    }

    // Due to the generics, the menu data access and screen type need to be
    // contained here.
    private class ConduitScreenTypeContainer<U extends ConnectionConfig> {
        private final ConduitMenuDataAccess<U> dataAccess;

        @Nullable
        private final ConduitScreenType<U> screenType;

        public ConduitScreenTypeContainer(Conduit<?, U> conduit) {
            this.dataAccess = createDataAccess(menu.getBlockPos(), conduit);
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
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(22, 7, 0);
                screenType.renderLabels(guiGraphics, font, mouseX, mouseY);
                guiGraphics.pose().popPose();
            }
        }

        private <T extends Conduit<T, U>, U extends ConnectionConfig> ConduitMenuDataAccess<U> createDataAccess(
                BlockPos pos, Conduit<T, U> conduit) {
            return new ConduitMenuDataAccess<>() {
                @Override
                public Conduit<?, U> conduit() {
                    return conduit;
                }

                @Override
                public BlockPos getBlockPos() {
                    return pos;
                }

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
                public CompoundTag getExtraGuiData() {
                    return menu.extraGuiData();
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

        @Override
        public AbstractWidget addCheckbox(int x, int y, Supplier<Boolean> getter, Consumer<Boolean> setter) {
            var widget = ToggleIconButton.createCheckbox(getAreaLeft() + x, getAreaTop() + y, getter, setter);
            addRenderableWidget(widget);
            return widget;
        }

        @Override
        public AbstractWidget addColorPicker(int x, int y, Component title, Supplier<DyeColor> getter,
                Consumer<DyeColor> setter) {
            var widget = new DyeColorPickerWidget(getAreaLeft() + x, getAreaTop() + y, getter, setter, title);
            addRenderableWidget(widget);
            return widget;
        }

        @Override
        public AbstractWidget addRedstoneControlPicker(int x, int y, Component title, Supplier<RedstoneControl> getter,
                Consumer<RedstoneControl> setter) {
            var widget = new RedstoneControlPickerWidget(getAreaLeft() + x, getAreaTop() + y, getter, setter, title);
            addRenderableWidget(widget);
            return widget;
        }

        @Override
        public AbstractWidget addToggleButton(int x, int y, int width, int height, Component enabledTitle,
                Component disabledTitle, ResourceLocation enabledSprite, ResourceLocation disabledSprite,
                Supplier<Boolean> getter, Consumer<Boolean> setter) {

            var widget = ToggleIconButton.of(getAreaLeft() + x, getAreaTop() + y, width, height, enabledSprite,
                    disabledSprite, enabledTitle, disabledTitle, getter, setter);
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
            return ConduitScreen.this.addWidget(listener);
        }

        @Override
        public <W extends Renderable> W addRenderableOnly(W renderable) {
            return ConduitScreen.this.addRenderableOnly(renderable);
        }

        @Override
        public <W extends GuiEventListener & Renderable & NarratableEntry> W addRenderableWidget(W widget) {
            return ConduitScreen.this.addRenderableWidget(widget);
        }

        @Override
        public void removeWidget(GuiEventListener listener) {
            ConduitScreen.this.removeWidget(listener);
        }
    }
}
