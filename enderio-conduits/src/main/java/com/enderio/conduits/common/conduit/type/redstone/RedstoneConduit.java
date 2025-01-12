package com.enderio.conduits.common.conduit.type.redstone;

import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.conduits.common.redstone.RedstoneExtractFilter;
import com.enderio.conduits.common.redstone.RedstoneInsertFilter;
import com.enderio.conduits.common.tag.ConduitTags;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RedstoneConduit(ResourceLocation texture, ResourceLocation activeTexture, Component description)
        implements Conduit<RedstoneConduit, RedstoneConduitConnectionConfig> {

    public static MapCodec<RedstoneConduit> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
            .group(ResourceLocation.CODEC.fieldOf("texture").forGetter(RedstoneConduit::texture),
                    ResourceLocation.CODEC.fieldOf("active_texture").forGetter(RedstoneConduit::activeTexture),
                    ComponentSerialization.CODEC.fieldOf("description").forGetter(RedstoneConduit::description))
            .apply(builder, RedstoneConduit::new));

    private static final RedstoneConduitTicker TICKER = new RedstoneConduitTicker();

    @Override
    public int graphTickRate() {
        return 2;
    }

    @Override
    public ConduitType<RedstoneConduit> type() {
        return ConduitTypes.REDSTONE.get();
    }

    @Override
    public RedstoneConduitTicker getTicker() {
        return TICKER;
    }

    @Override
    public boolean canApplyFilter(SlotType slotType, ResourceFilter resourceFilter) {
        return switch (slotType) {
        case FILTER_EXTRACT -> resourceFilter instanceof RedstoneExtractFilter;
        case FILTER_INSERT -> resourceFilter instanceof RedstoneInsertFilter;
        default -> false;
        };
    }

    @Override
    public ResourceLocation getTexture(@Nullable CompoundTag extraWorldData) {
        if (extraWorldData != null) {
            return extraWorldData.contains("IsActive") && extraWorldData.getBoolean("IsActive") ? activeTexture()
                    : texture();
        }

        return texture();
    }

    @Override
    public void onConnectionsUpdated(ConduitNode node, Level level, BlockPos pos, Set<Direction> connectedSides) {
        node.markDirty();
    }

    @Override
    public boolean canConnectToBlock(Level level, BlockPos conduitPos, Direction direction) {
        BlockPos neighbor = conduitPos.relative(direction);
        BlockState blockState = level.getBlockState(neighbor);
        return blockState.is(ConduitTags.Blocks.REDSTONE_CONNECTABLE)
                || blockState.canRedstoneConnectTo(level, neighbor, direction.getOpposite());
    }

    @Override
    public boolean canForceConnectToBlock(Level level, BlockPos conduitPos, Direction direction) {
        BlockPos neighbor = conduitPos.relative(direction);
        BlockState blockState = level.getBlockState(neighbor);
        return !blockState.isAir();
    }

    @Override
    public ConnectionConfigType<RedstoneConduitConnectionConfig> connectionConfigType() {
        return RedstoneConduitConnectionConfig.TYPE;
    }

    @Override
    public RedstoneConduitConnectionConfig convertConnection(boolean isInsert, boolean isExtract, DyeColor inputChannel,
            DyeColor outputChannel, RedstoneControl redstoneControl, DyeColor redstoneChannel) {
        return new RedstoneConduitConnectionConfig(isInsert, inputChannel, isExtract, outputChannel, false);
    }

    @Override
    public CompoundTag getExtraWorldData(ConduitBundleReader conduitBundle, ConduitNode node) {
        var tag = new CompoundTag();

        if (node.getNetwork() == null) {
            return tag;
        }

        var context = node.getNetwork().getContext(RedstoneConduitNetworkContext.TYPE);
        if (context != null) {
            tag.putBoolean("IsActive", context.isActive());
        }

        return tag;
    }

    @Override
    public int compareTo(@NotNull RedstoneConduit o) {
        return 0;
    }
}
