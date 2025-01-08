package com.enderio.conduits.client.gui;

import com.enderio.base.api.EnderIO;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.base.client.gui.widget.DyeColorPickerWidget;
import com.enderio.base.client.gui.widget.RedstoneControlPickerWidget;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.network.node.legacy.ConduitData;
import com.enderio.conduits.api.network.node.legacy.ConduitDataAccessor;
import com.enderio.conduits.api.network.node.legacy.ConduitDataType;
import com.enderio.conduits.api.ConduitMenuData;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.screen.ConduitScreenExtension;
import com.enderio.conduits.client.gui.conduit.ConduitScreenExtensions;
import com.enderio.conduits.common.conduit.ConduitBundle;
import com.enderio.conduits.common.conduit.connection.ConnectionState;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.menu.ConduitMenu;
import com.enderio.conduits.common.network.C2SSetConduitConnectionState;
import com.enderio.conduits.common.network.C2SSetConduitExtendedData;
import com.enderio.core.client.gui.screen.EnderContainerScreen;
import com.enderio.core.client.gui.widgets.ToggleIconButton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class ConduitScreen extends EnderContainerScreen<ConduitMenu> {

    public static final ResourceLocation TEXTURE = EnderIO.loc("textures/gui/conduit.png");
    private static final int WIDTH = 206;
    private static final int HEIGHT = 195;

    private final ClientConduitDataAccessor conduitDataAccessor = new ClientConduitDataAccessor();

    public ConduitScreen(ConduitMenu pMenu, Inventory pPlayerInventory, Component title) {
        super(pMenu, pPlayerInventory, title);

        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // close and don't render if someone removed the conduit we are looking at or
        // similar
        if (!menu.stillValid(minecraft.player)) {
            minecraft.player.closeContainer();
        } else {
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);

        ConduitMenuData data = getMenuData();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getGuiLeft(), getGuiTop(), 0);

        if (data.showBarSeparator()) {
            guiGraphics.blit(TEXTURE, 102, 7, 255, 0, 1, 97);
        }

        for (SlotType type : SlotType.values()) {
            if (type.isAvailableFor(data)) {
                guiGraphics.blit(TEXTURE, type.getX() - 1, type.getY() - 1, 206, 0, 18, 18);
            }
        }

        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        ConduitMenuData data = getMenuData();

        guiGraphics.drawString(this.font, ConduitLang.CONDUIT_INSERT, 22 + 16 + 2, 7 + 4, 4210752, false);

        if (data.showBothEnable()) {
            guiGraphics.drawString(this.font, ConduitLang.CONDUIT_EXTRACT, 112 + 16 + 2, 7 + 4, 4210752, false);
        }

        super.renderLabels(guiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void init() {
        super.init();
        ConduitMenuData data = getMenuData();
        Vector2i pos = new Vector2i(22, 7).add(getGuiLeft(), getGuiTop());

        addRenderableWidget(
                ToggleIconButton.createCheckbox(pos.x(), pos.y(), () -> getOnDynamic(dyn -> dyn.isInsert(), false),
                        bool -> actOnDynamic(dyn -> dyn.withEnabled(false, bool))));

        if (data.showBothEnable()) {
            addRenderableWidget(ToggleIconButton.createCheckbox(pos.x() + 90, pos.y(),
                    () -> getOnDynamic(dyn -> dyn.isExtract(), false),
                    bool -> actOnDynamic(dyn -> dyn.withEnabled(true, bool))));
        }

        if (data.showColorInsert()) {
            addRenderableWidget(new DyeColorPickerWidget(pos.x(), pos.y() + 20,
                    () -> getOnDynamic(dyn -> dyn.insertChannel(), DyeColor.GREEN),
                    color -> actOnDynamic(dyn -> dyn.withColor(false, color)), ConduitLang.CONDUIT_CHANNEL));
        }

        if (data.showColorExtract()) {
            addRenderableWidget(new DyeColorPickerWidget(pos.x() + 90, pos.y() + 20,
                    () -> getOnDynamic(dyn -> dyn.extractChannel(), DyeColor.GREEN),
                    color -> actOnDynamic(dyn -> dyn.withColor(true, color)), ConduitLang.CONDUIT_CHANNEL));
        }

        if (data.showRedstoneExtract()) {
            addRenderableWidget(new RedstoneControlPickerWidget(pos.x() + 90, pos.y() + 40,
                    () -> getOnDynamic(dyn -> dyn.control(), RedstoneControl.ACTIVE_WITH_SIGNAL),
                    mode -> actOnDynamic(dyn -> dyn.withRedstoneMode(mode)), EIOLang.REDSTONE_MODE));

            addRenderableWidget(new DyeColorPickerWidget(pos.x() + 90 + 20, pos.y() + 40,
                    () -> getOnDynamic(dyn -> dyn.redstoneChannel(), DyeColor.GREEN),
                    color -> actOnDynamic(dyn -> dyn.withRedstoneChannel(color)), ConduitLang.REDSTONE_CHANNEL));
        }

        addConduitScreenExtensionWidgets();
        addConduitSelectionButtons();
    }

    private void addConduitScreenExtensionWidgets() {
        ConduitScreenExtension conduitScreenExtension = ConduitScreenExtensions.get(menu.getConduit().value().type());

        if (conduitScreenExtension != null) {
            conduitScreenExtension
                    .createWidgets(this, conduitDataAccessor, this::sendExtendedConduitUpdate, menu::getDirection,
                            new Vector2i(22, 7).add(getGuiLeft(), getGuiTop()))
                    .forEach(this::addRenderableWidget);
        }
    }

    private void addConduitSelectionButtons() {
        List<Holder<Conduit<?>>> validConnections = new ArrayList<>();
        for (Holder<Conduit<?>> type : getBundle().getConduits()) {
            if (getConnectionState(type) instanceof DynamicConnectionState) {
                validConnections.add(type);
            }
        }

        for (int i = 0; i < validConnections.size(); i++) {
            Holder<Conduit<?>> connection = validConnections.get(i);
            addRenderableWidget(new ConduitSelectionButton(getGuiLeft() + 206, getGuiTop() + 4 + 24 * i, connection,
                    this::getConduit, this::setConduitType));
        }
    }

    private void sendExtendedConduitUpdate() {
        Holder<Conduit<?>> conduit = menu.getConduit();
        ConduitGraphObject node = getBundle().getNodeFor(conduit);

//        PacketDistributor.sendToServer(new C2SSetConduitExtendedData(menu.getBlockEntity().getBlockPos(),
//                menu.getConduit(), node.conduitDataContainer()));
    }

    private void actOnDynamic(Function<DynamicConnectionState, DynamicConnectionState> map) {
        if (getConnectionState() instanceof DynamicConnectionState dyn) {
            PacketDistributor.sendToServer(new C2SSetConduitConnectionState(getMenu().getBlockEntity().getBlockPos(),
                    getMenu().getDirection(), getMenu().getConduit(), map.apply(dyn)));
        }
    }

    private <T> T getOnDynamic(Function<DynamicConnectionState, T> map, T defaultValue) {
        return getConnectionState() instanceof DynamicConnectionState dyn ? map.apply(dyn) : defaultValue;
    }

    public Holder<Conduit<?>> getConduit() {
        return menu.getConduit();
    }

    public ConduitMenuData getMenuData() {
        return getConduit().value().getMenuData();
    }

    private void setConduitType(Holder<Conduit<?>> conduit) {
        menu.setConduit(conduit);
        rebuildWidgets();
    }

    private ConnectionState getConnectionState() {
        return getConnectionState(menu.getConduit());
    }

    private ConnectionState getConnectionState(Holder<Conduit<?>> type) {
        return getBundle().getConnectionState(menu.getDirection(), type);
    }

    private ConduitBundle getBundle() {
        return menu.getBlockEntity().getBundle();
    }

    /**
     * This is a simple passthrough to the current conduit node's data.
     * This results in a much nicer API than that of the alternative approach of a conduit data accessor supplier.
     */
    private class ClientConduitDataAccessor implements ConduitDataAccessor {

        private ConduitDataAccessor getCurrentDataAccessor() {
//            return getBundle().getNodeFor(menu.getConduit());
            return null;
        }

        @Override
        public boolean hasData(ConduitDataType<?> type) {
            return getCurrentDataAccessor().hasData(type);
        }

        @Override
        public <T extends ConduitData<T>> @Nullable T getData(ConduitDataType<T> type) {
            return getCurrentDataAccessor().getData(type);
        }

        @Override
        public <T extends ConduitData<T>> T getOrCreateData(ConduitDataType<T> type) {
            return getCurrentDataAccessor().getOrCreateData(type);
        }
    }
}
