package com.enderio.modconduits.mods.appeng;

import appeng.api.AECapabilities;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IManagedGridNode;
import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitMenuData;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.api.ticker.ConduitTicker;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public record MEConduit(
    ResourceLocation texture,
    Component description,
    boolean isDense
) implements Conduit<MEConduit> {

    public static MapCodec<MEConduit> CODEC = RecordCodecBuilder.mapCodec(
        builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("texture").forGetter(MEConduit::texture),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(MEConduit::description),
            Codec.BOOL.fieldOf("is_dense").forGetter(MEConduit::isDense)
        ).apply(builder, MEConduit::new)
    );

    @Override
    public ConduitType<MEConduit> type() {
        return AE2ConduitsModule.AE2_CONDUIT.get();
    }

    @Override
    public ConduitTicker<MEConduit> getTicker() {
        return Ticker.INSTANCE;
    }

    @Override
    public ConduitMenuData getMenuData() {
        return MenuData.INSTANCE;
    }

    @Override
    public boolean hasConnectionDelay() {
        return true;
    }

    @Override
    public boolean canConnectTo(Holder<Conduit<?, ?>> other) {
        return other.value().type() == type();
    }

    @Override
    public void onCreated(ConduitNode node, Level level, BlockPos pos, @Nullable Player player) {
        var data = node.getOrCreateData(AE2ConduitsModule.DATA.get());

        if (data.getMainNode() == null) {
            initMainNode(level, data);
        }

        IManagedGridNode mainNode = data.getMainNode();
        if (mainNode.isReady()) {
            return;
        }

        if (player != null) {
            mainNode.setOwningPlayer(player);
        }

        GridHelper.onFirstTick(level.getBlockEntity(pos), blockEntity -> {
            if (!mainNode.isReady()) {
                mainNode.create(level, pos);
            }
        });
    }

    @Override
    public void onRemoved(ConduitNode node, Level level, BlockPos pos) {
        var data = node.getOrCreateData(AE2ConduitsModule.DATA.get());
        IManagedGridNode mainNode = data.getMainNode();
        if (mainNode != null) {
            mainNode.destroy();
            data.clearMainNode();
        }
    }

    private void initMainNode(Level level, ConduitInWorldGridNodeHost nodeHost) {
        var mainNode = nodeHost.getMainNode();
        if (mainNode != null) {
            throw new UnsupportedOperationException("mainNode is already initialized");
        }

        Holder<Conduit<?, ?>> asHolder = level.registryAccess().registryOrThrow(EnderIOConduitsRegistries.Keys.CONDUIT).wrapAsHolder(this);

        mainNode = GridHelper.createManagedNode(nodeHost, GridNodeListener.INSTANCE)
            .setVisualRepresentation(ConduitBlockItem.getStackFor(asHolder, 1))
            .setInWorldNode(true)
            .setTagName("conduit");

        mainNode.setIdlePowerUsage(isDense() ? 0.4d : 0.1d);

        if (isDense()) {
            mainNode.setFlags(GridFlags.DENSE_CAPACITY);
        }

        nodeHost.setMainNode(mainNode, isDense());

        // Load any saved data
        nodeHost.loadMainNode();
    }

    @Override
    public void onConnectionsUpdated(ConduitNode node, Level level, BlockPos pos, Set<Direction> connectedSides) {
        var data = node.getOrCreateData(AE2ConduitsModule.DATA.get());
        IManagedGridNode mainNode = data.getMainNode();
        if (mainNode != null) {
            mainNode.setExposedOnSides(connectedSides);
        }
    }

    @Override
    public <TCap, TContext> @Nullable TCap proxyCapability(BlockCapability<TCap, TContext> capability, ConduitNode node,
        Level level, BlockPos pos, @Nullable TContext context) {

        if (capability == AECapabilities.IN_WORLD_GRID_NODE_HOST) {
            //noinspection unchecked
            return (TCap)node.getOrCreateData(AE2ConduitsModule.DATA.get());
        }

        return null;
    }

    @Override
    public int compareTo(@NotNull MEConduit o) {
        if (isDense() && !o.isDense()) {
            return 1;
        } else if (!isDense() && o.isDense()) {
            return -1;
        }

        return 0;
    }

    private static final class MenuData implements ConduitMenuData {

        private static final MenuData INSTANCE = new MenuData();

        @Override
        public boolean hasFilterInsert() {
            return false;
        }

        @Override
        public boolean hasFilterExtract() {
            return false;
        }

        @Override
        public boolean hasUpgrade() {
            return false;
        }

        @Override
        public boolean showBarSeparator() {
            return false;
        }

        @Override
        public boolean showBothEnable() {
            return false;
        }

        @Override
        public boolean showColorInsert() {
            return false;
        }

        @Override
        public boolean showColorExtract() {
            return false;
        }

        @Override
        public boolean showRedstoneExtract() {
            return false;
        }
    }

    private static final class Ticker implements ConduitTicker<MEConduit> {

        private static final Ticker INSTANCE = new Ticker();

        @Override
        public void tickGraph(ServerLevel level, MEConduit type, ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider) {
            //ae2 graphs don't actually do anything, that's all done by ae2
        }

        @Override
        public boolean canConnectTo(Level level, BlockPos conduitPos, Direction direction) {
            return GridHelper.getExposedNode(level, conduitPos.relative(direction), direction.getOpposite()) != null;
        }
    }
}
