package com.enderio.conduits.common.conduit.type.redstone;

import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.connection.config.NewIOConnectionConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

public record RedstoneConduitConnectionConfig(
    boolean isSend,
    DyeColor sendColor,
    boolean isReceive,
    DyeColor receiveColor,
    boolean isStrongOutputSignal
) implements NewIOConnectionConfig {

    public static RedstoneConduitConnectionConfig DEFAULT = new RedstoneConduitConnectionConfig(false, DyeColor.GREEN, true, DyeColor.RED, false);

    public static MapCodec<RedstoneConduitConnectionConfig> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.BOOL.fieldOf("is_send").forGetter(RedstoneConduitConnectionConfig::isSend),
            DyeColor.CODEC.fieldOf("send_color").forGetter(RedstoneConduitConnectionConfig::sendColor),
            Codec.BOOL.fieldOf("is_receive").forGetter(RedstoneConduitConnectionConfig::isReceive),
            DyeColor.CODEC.fieldOf("receive_color").forGetter(RedstoneConduitConnectionConfig::receiveColor),
            Codec.BOOL.fieldOf("is_strong_output_signal").forGetter(RedstoneConduitConnectionConfig::isStrongOutputSignal)
        ).apply(instance, RedstoneConduitConnectionConfig::new)
    );

    public static StreamCodec<ByteBuf, RedstoneConduitConnectionConfig> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        RedstoneConduitConnectionConfig::isSend,
        DyeColor.STREAM_CODEC,
        RedstoneConduitConnectionConfig::sendColor,
        ByteBufCodecs.BOOL,
        RedstoneConduitConnectionConfig::isReceive,
        DyeColor.STREAM_CODEC,
        RedstoneConduitConnectionConfig::receiveColor,
        ByteBufCodecs.BOOL,
        RedstoneConduitConnectionConfig::isStrongOutputSignal,
        RedstoneConduitConnectionConfig::new
    );

    public static ConnectionConfigType<RedstoneConduitConnectionConfig> TYPE = new ConnectionConfigType<>(
            RedstoneConduitConnectionConfig.class, CODEC, STREAM_CODEC.cast(), () -> DEFAULT);

    @Override
    public ConnectionConfig reconnected() {
        return new RedstoneConduitConnectionConfig(DEFAULT.isSend, sendColor, DEFAULT.isReceive, receiveColor, isStrongOutputSignal);
    }

    public RedstoneConduitConnectionConfig withIsSend(boolean isSend) {
        return new RedstoneConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, isStrongOutputSignal);
    }

    public RedstoneConduitConnectionConfig withSendColor(DyeColor sendColor) {
        return new RedstoneConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, isStrongOutputSignal);
    }

    public RedstoneConduitConnectionConfig withIsReceive(boolean isReceive) {
        return new RedstoneConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, isStrongOutputSignal);
    }

    public RedstoneConduitConnectionConfig withReceiveColor(DyeColor receiveColor) {
        return new RedstoneConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, isStrongOutputSignal);
    }

    public RedstoneConduitConnectionConfig withIsStrongOutputSignal(boolean isStrongOutputSignal) {
        return new RedstoneConduitConnectionConfig(isSend, sendColor, isReceive, receiveColor, isStrongOutputSignal);
    }

    @Override
    public ConnectionConfigType<RedstoneConduitConnectionConfig> type() {
        return TYPE;
    }
}
