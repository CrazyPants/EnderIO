package com.enderio.conduits.common.conduit.type.fluid;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.ConduitRedstoneSignalAware;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.connection.config.IOConnectionConfig;
import com.enderio.conduits.api.connection.config.RedstoneSensitiveConnectionConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

public record FluidConduitConnectionConfig(boolean isSend, DyeColor sendColor, boolean isReceive, DyeColor receiveColor,
        RedstoneControl receiveRedstoneControl, DyeColor receiveRedstoneChannel)
        implements IOConnectionConfig, RedstoneSensitiveConnectionConfig {

    public static FluidConduitConnectionConfig DEFAULT = new FluidConduitConnectionConfig(false, DyeColor.GREEN, true,
            DyeColor.GREEN, RedstoneControl.NEVER_ACTIVE, DyeColor.RED);

    public static MapCodec<FluidConduitConnectionConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.BOOL.fieldOf("is_send").forGetter(FluidConduitConnectionConfig::isSend),
                    DyeColor.CODEC.fieldOf("send_color").forGetter(FluidConduitConnectionConfig::sendColor),
                    Codec.BOOL.fieldOf("is_receive").forGetter(FluidConduitConnectionConfig::isReceive),
                    DyeColor.CODEC.fieldOf("receive_channel").forGetter(FluidConduitConnectionConfig::receiveColor),
                    RedstoneControl.CODEC.fieldOf("receive_redstone_control")
                            .forGetter(FluidConduitConnectionConfig::receiveRedstoneControl),
                    DyeColor.CODEC.fieldOf("receive_redstone_channel")
                            .forGetter(FluidConduitConnectionConfig::receiveRedstoneChannel))
            .apply(instance, FluidConduitConnectionConfig::new));

    // @formatter:off
    public static StreamCodec<ByteBuf, FluidConduitConnectionConfig> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        FluidConduitConnectionConfig::isSend,
        DyeColor.STREAM_CODEC,
        FluidConduitConnectionConfig::sendColor,
        ByteBufCodecs.BOOL,
        FluidConduitConnectionConfig::isReceive,
        DyeColor.STREAM_CODEC,
        FluidConduitConnectionConfig::receiveColor,
        RedstoneControl.STREAM_CODEC,
        FluidConduitConnectionConfig::receiveRedstoneControl,
        DyeColor.STREAM_CODEC,
        FluidConduitConnectionConfig::receiveRedstoneChannel,
        FluidConduitConnectionConfig::new);
    // @formatter:on

    public static ConnectionConfigType<FluidConduitConnectionConfig> TYPE = new ConnectionConfigType<>(CODEC,
            STREAM_CODEC.cast(), () -> DEFAULT);

    @Override
    public ConnectionConfig reconnected() {
        return new FluidConduitConnectionConfig(DEFAULT.isSend, sendColor, DEFAULT.isReceive, receiveColor,
                receiveRedstoneControl, receiveRedstoneChannel);
    }

    @Override
    public ConnectionConfig disconnected() {
        return new FluidConduitConnectionConfig(false, sendColor, false, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel);
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

    @Override
    public List<DyeColor> getRedstoneSignalColors() {
        if (receiveRedstoneControl.isRedstoneSensitive()) {
            return List.of(receiveRedstoneChannel);
        }

        return List.of();
    }

    public FluidConduitConnectionConfig withIsSend(boolean isSend) {
        return new FluidConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel);
    }

    public FluidConduitConnectionConfig withSendColor(DyeColor sendColor) {
        return new FluidConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel);
    }

    public FluidConduitConnectionConfig withIsReceive(boolean isReceive) {
        return new FluidConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel);
    }

    public FluidConduitConnectionConfig withReceiveColor(DyeColor receiveColor) {
        return new FluidConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel);
    }

    public FluidConduitConnectionConfig withReceiveRedstoneControl(RedstoneControl receiveRedstoneControl) {
        return new FluidConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel);
    }

    public FluidConduitConnectionConfig withReceiveRedstoneChannel(DyeColor receiveRedstoneChannel) {
        return new FluidConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, receiveRedstoneControl,
                receiveRedstoneChannel);
    }

    @Override
    public ConnectionConfigType<FluidConduitConnectionConfig> type() {
        return TYPE;
    }
}
