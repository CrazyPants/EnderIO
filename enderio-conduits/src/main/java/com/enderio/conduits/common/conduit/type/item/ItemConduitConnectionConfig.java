package com.enderio.conduits.common.conduit.type.item;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.base.api.network.MassiveStreamCodec;
import com.enderio.conduits.api.ConduitRedstoneSignalAware;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.connection.config.NewIOConnectionConfig;
import com.enderio.conduits.api.connection.config.RedstoneSensitiveConnectionConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

public record ItemConduitConnectionConfig(boolean isSend, DyeColor sendColor, boolean isReceive, DyeColor receiveColor,
        RedstoneControl receiveRedstoneControl, DyeColor receiveRedstoneChannel, boolean isRoundRobin,
        boolean isSelfFeed, int priority) implements NewIOConnectionConfig, RedstoneSensitiveConnectionConfig {


    public static ItemConduitConnectionConfig DEFAULT = new ItemConduitConnectionConfig(false, DyeColor.GREEN, true,
        DyeColor.GREEN, RedstoneControl.NEVER_ACTIVE, DyeColor.RED, false, false, 0);

    public static MapCodec<ItemConduitConnectionConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.BOOL.fieldOf("is_send").forGetter(ItemConduitConnectionConfig::isSend),
                    DyeColor.CODEC.fieldOf("send_color").forGetter(ItemConduitConnectionConfig::sendColor),
                    Codec.BOOL.fieldOf("is_receive").forGetter(ItemConduitConnectionConfig::isReceive),
                    DyeColor.CODEC.fieldOf("receive_color").forGetter(ItemConduitConnectionConfig::receiveColor),
                    RedstoneControl.CODEC.fieldOf("receive_redstone_control")
                            .forGetter(ItemConduitConnectionConfig::receiveRedstoneControl),
                    DyeColor.CODEC.fieldOf("receive_redstone_channel")
                            .forGetter(ItemConduitConnectionConfig::receiveRedstoneChannel),
                    Codec.BOOL.fieldOf("is_round_robin").forGetter(ItemConduitConnectionConfig::isRoundRobin),
                    Codec.BOOL.fieldOf("is_self_feed").forGetter(ItemConduitConnectionConfig::isSelfFeed),
                    Codec.INT.fieldOf("priority").forGetter(ItemConduitConnectionConfig::priority))
            .apply(instance, ItemConduitConnectionConfig::new));

    public static StreamCodec<ByteBuf, ItemConduitConnectionConfig> STREAM_CODEC = MassiveStreamCodec.composite(
            ByteBufCodecs.BOOL, ItemConduitConnectionConfig::isSend, DyeColor.STREAM_CODEC,
            ItemConduitConnectionConfig::sendColor, ByteBufCodecs.BOOL, ItemConduitConnectionConfig::isReceive,
            DyeColor.STREAM_CODEC, ItemConduitConnectionConfig::receiveColor, RedstoneControl.STREAM_CODEC,
            ItemConduitConnectionConfig::receiveRedstoneControl, DyeColor.STREAM_CODEC,
            ItemConduitConnectionConfig::receiveRedstoneChannel, ByteBufCodecs.BOOL,
            ItemConduitConnectionConfig::isRoundRobin, ByteBufCodecs.BOOL, ItemConduitConnectionConfig::isSelfFeed,
            ByteBufCodecs.INT, ItemConduitConnectionConfig::priority, ItemConduitConnectionConfig::new);

    public static ConnectionConfigType<ItemConduitConnectionConfig> TYPE = new ConnectionConfigType<>(
            ItemConduitConnectionConfig.class, CODEC, STREAM_CODEC.cast(), () -> DEFAULT);

    @Override
    public ConnectionConfig reconnected() {
        return new ItemConduitConnectionConfig(false, sendColor, true, receiveColor,
            receiveRedstoneControl, receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    @Override
    public ConnectionConfig disconnected() {
        return new ItemConduitConnectionConfig(false, sendColor, false, receiveColor,
            receiveRedstoneControl, receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    @Override
    public boolean canSend(ConduitRedstoneSignalAware signalAware) {
        // TODO: sendRedstoneControl
        return isSend();
    }

    @Override
    public boolean canReceive(ConduitRedstoneSignalAware signalAware) {
        if (!isReceive()) {
            return false;
        }

        if (receiveRedstoneControl.isRedstoneSensitive()) {
            return receiveRedstoneControl.isActive(signalAware.hasRedstoneSignal(receiveRedstoneChannel));
        } else {
            return true;
        }
    }

    // Generate with methods for each field
    public ItemConduitConnectionConfig withIsSend(boolean isSend) {
        return new ItemConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withSendColor(DyeColor sendColor) {
        return new ItemConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withIsReceive(boolean isReceive) {
        return new ItemConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withReceiveColor(DyeColor receiveColor) {
        return new ItemConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withReceiveRedstoneControl(RedstoneControl receiveRedstoneControl) {
        return new ItemConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withReceiveRedstoneChannel(DyeColor receiveRedstoneChannel) {
        return new ItemConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withIsRoundRobin(boolean isRoundRobin) {
        return new ItemConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withIsSelfFeed(boolean isSelfFeed) {
        return new ItemConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withPriority(int priority) {
        return new ItemConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    @Override
    public ConnectionConfigType<ItemConduitConnectionConfig> type() {
        return TYPE;
    }

    @Override
    public List<DyeColor> getRedstoneSignalColors() {
        if (receiveRedstoneControl.isRedstoneSensitive()) {
            return List.of(receiveRedstoneChannel);
        }

        return List.of();
    }
}
