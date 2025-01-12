package com.enderio.conduits.common.conduit.type.item;

import com.enderio.base.api.filter.ItemStackFilter;
import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.api.bundle.SlotType;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.network.node.legacy.ConduitDataAccessor;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.core.common.util.TooltipUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ItemConduit(ResourceLocation texture, Component description, int transferRatePerCycle, int graphTickRate)
        implements Conduit<ItemConduit, ItemConduitConnectionConfig> {

    public static final MapCodec<ItemConduit> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
            .group(ResourceLocation.CODEC.fieldOf("texture").forGetter(ItemConduit::texture),
                    ComponentSerialization.CODEC.fieldOf("description").forGetter(ItemConduit::description),
                    // Using optionals in order to support the old conduit format.
                    Codec.INT.optionalFieldOf("transfer_rate", 4).forGetter(ItemConduit::transferRatePerCycle),
                    Codec.intRange(1, 20).optionalFieldOf("ticks_per_cycle", 20).forGetter(ItemConduit::graphTickRate))
            .apply(builder, ItemConduit::new));

    private static final ItemConduitTicker TICKER = new ItemConduitTicker();

    @Override
    public ConduitType<ItemConduit> type() {
        return ConduitTypes.ITEM.get();
    }

    @Override
    public ItemConduitTicker getTicker() {
        return TICKER;
    }

    @Override
    public boolean canApplyFilter(SlotType slotType, ResourceFilter resourceFilter) {
        return resourceFilter instanceof ItemStackFilter;
    }

    @Override
    public void addToTooltip(Item.TooltipContext pContext, Consumer<Component> pTooltipAdder,
            TooltipFlag pTooltipFlag) {
        String calculatedTransferLimitFormatted = String.format("%,d",
                (int) Math.floor(transferRatePerCycle() * (20.0 / graphTickRate())));
        pTooltipAdder.accept(
                TooltipUtil.styledWithArgs(ConduitLang.ITEM_EFFECTIVE_RATE_TOOLTIP, calculatedTransferLimitFormatted));

        if (pTooltipFlag.hasShiftDown()) {
            String transferLimitFormatted = String.format("%,d", transferRatePerCycle());
            pTooltipAdder.accept(TooltipUtil.styledWithArgs(ConduitLang.ITEM_RAW_RATE_TOOLTIP, transferLimitFormatted));
        }
    }

    @Override
    public boolean hasAdvancedTooltip() {
        return true;
    }

    @Override
    public boolean showDebugTooltip() {
        return true;
    }

    @Override
    public boolean canConnectToBlock(Level level, BlockPos conduitPos, Direction direction) {
        IItemHandler capability = level.getCapability(Capabilities.ItemHandler.BLOCK, conduitPos.relative(direction),
                direction.getOpposite());
        return capability != null;
    }

    @Override
    public ConnectionConfigType<ItemConduitConnectionConfig> connectionConfigType() {
        return ConduitTypes.ConnectionTypes.ITEM.get();
    }

    // TODO: Move conversions into the connection config type?
    @Override
    public ItemConduitConnectionConfig convertConnection(boolean isInsert, boolean isExtract, DyeColor inputChannel,
            DyeColor outputChannel, RedstoneControl redstoneControl, DyeColor redstoneChannel) {
        return new ItemConduitConnectionConfig(isInsert, inputChannel, isExtract, outputChannel, redstoneControl,
                redstoneChannel, false, false, 0);
    }

    @Override
    public void copyLegacyData(ConduitNode node, ConduitDataAccessor legacyDataAccessor) {
        var legacyData = legacyDataAccessor.getData(ConduitTypes.Data.ITEM.get());
        if (legacyData == null) {
            return;
        }

        // Copy connection config
        for (Direction side : Direction.values()) {
            if (node.isConnectedTo(side)) {
                var oldSideConfig = legacyData.get(side);
                var currentConfig = node.getConnectionConfig(side, ItemConduitConnectionConfig.TYPE);

                node.setConnectionConfig(side,
                        currentConfig.withIsRoundRobin(oldSideConfig.isRoundRobin)
                                .withIsSelfFeed(oldSideConfig.isSelfFeed)
                                .withPriority(oldSideConfig.priority));
            }
        }
    }

    @Override
    @Nullable
    public CompoundTag getExtraGuiData(ConduitBundleReader conduitBundle, ConduitNode node, Direction side) {
        if (!node.isConnectedTo(side)) {
            return null;
        }

        var config = node.getConnectionConfig(side, connectionConfigType());
        if (!config.receiveRedstoneControl().isRedstoneSensitive()) {
            return null;
        }

        CompoundTag tag = new CompoundTag();
        tag.putBoolean("HasRedstoneSignal", node.hasRedstoneSignal(config.receiveRedstoneChannel()));
        tag.putBoolean("HasRedstoneConduit", conduitBundle.hasConduitByType(ConduitTypes.REDSTONE.get()));
        return tag;
    }

    @Override
    public int compareTo(@NotNull ItemConduit o) {
        double selfEffectiveSpeed = transferRatePerCycle() * (20.0 / graphTickRate());
        double otherEffectiveSpeed = o.transferRatePerCycle() * (20.0 / o.graphTickRate());

        if (selfEffectiveSpeed < otherEffectiveSpeed) {
            return -1;
        } else if (selfEffectiveSpeed > otherEffectiveSpeed) {
            return 1;
        }

        return 0;
    }
}
