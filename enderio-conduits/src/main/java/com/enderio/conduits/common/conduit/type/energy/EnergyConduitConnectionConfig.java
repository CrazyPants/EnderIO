package com.enderio.conduits.common.conduit.type.energy;

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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

import java.util.List;

public record EnergyConduitConnectionConfig(boolean isSend, boolean isReceive, RedstoneControl receiveRedstoneControl,
                                            DyeColor receiveRedstoneChannel) implements IOConnectionConfig, RedstoneSensitiveConnectionConfig {

    public static EnergyConduitConnectionConfig DEFAULT = new EnergyConduitConnectionConfig(true, true,
            RedstoneControl.ALWAYS_ACTIVE, DyeColor.RED);

    public static MapCodec<EnergyConduitConnectionConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.BOOL.fieldOf("is_send").forGetter(EnergyConduitConnectionConfig::isSend),
                    Codec.BOOL.fieldOf("is_receive").forGetter(EnergyConduitConnectionConfig::isReceive),
                    RedstoneControl.CODEC.fieldOf("receive_redstone_control")
                            .forGetter(EnergyConduitConnectionConfig::receiveRedstoneControl),
                    DyeColor.CODEC.fieldOf("receive_redstone_channel")
                            .forGetter(EnergyConduitConnectionConfig::receiveRedstoneChannel))
            .apply(instance, EnergyConduitConnectionConfig::new));

    public static StreamCodec<ByteBuf, EnergyConduitConnectionConfig> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EnergyConduitConnectionConfig::isSend, ByteBufCodecs.BOOL,
            EnergyConduitConnectionConfig::isReceive, RedstoneControl.STREAM_CODEC,
            EnergyConduitConnectionConfig::receiveRedstoneControl, DyeColor.STREAM_CODEC,
            EnergyConduitConnectionConfig::receiveRedstoneChannel, EnergyConduitConnectionConfig::new);

    public static final ConnectionConfigType<EnergyConduitConnectionConfig> TYPE = new ConnectionConfigType<>(CODEC, STREAM_CODEC.cast(), () -> DEFAULT);

    @Override
    public ConnectionConfig reconnected() {
        return new EnergyConduitConnectionConfig(DEFAULT.isSend, DEFAULT.isReceive, receiveRedstoneControl, receiveRedstoneChannel);
    }

    @Override
    public ConnectionConfig disconnected() {
        return new EnergyConduitConnectionConfig(false, false, receiveRedstoneControl, receiveRedstoneChannel);
    }

    @Override
    public DyeColor sendColor() {
        return DyeColor.RED;
    }

    @Override
    public DyeColor receiveColor() {
        return DyeColor.RED;
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

    public EnergyConduitConnectionConfig withIsSend(boolean isSend) {
        return new EnergyConduitConnectionConfig(isSend, isReceive, receiveRedstoneControl, receiveRedstoneChannel);
    }

    public EnergyConduitConnectionConfig withIsReceive(boolean isReceive) {
        return new EnergyConduitConnectionConfig(isSend, isReceive, receiveRedstoneControl, receiveRedstoneChannel);
    }

    public EnergyConduitConnectionConfig withReceiveRedstoneControl(RedstoneControl receiveRedstoneControl) {
        return new EnergyConduitConnectionConfig(isSend, isReceive, receiveRedstoneControl, receiveRedstoneChannel);
    }

    public EnergyConduitConnectionConfig withReceiveRedstoneChannel(DyeColor receiveRedstoneChannel) {
        return new EnergyConduitConnectionConfig(isSend, isReceive, receiveRedstoneControl, receiveRedstoneChannel);
    }

    @Override
    public ConnectionConfigType<?> type() {
        return TYPE;
    }
}
