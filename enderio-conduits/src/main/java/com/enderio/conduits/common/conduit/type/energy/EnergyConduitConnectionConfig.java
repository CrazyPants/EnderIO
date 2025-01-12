package com.enderio.conduits.common.conduit.type.energy;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.connection.config.io.IOConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

public record EnergyConduitConnectionConfig(boolean canInsert, boolean canExtract, RedstoneControl redstoneControl,
        DyeColor redstoneChannel) implements IOConnectionConfig, RedstoneControlledConnection {

    public static EnergyConduitConnectionConfig DEFAULT = new EnergyConduitConnectionConfig(true, true,
            RedstoneControl.ALWAYS_ACTIVE, DyeColor.RED);

    public static MapCodec<EnergyConduitConnectionConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.BOOL.fieldOf("can_insert").forGetter(EnergyConduitConnectionConfig::canInsert),
                    Codec.BOOL.fieldOf("can_extract").forGetter(EnergyConduitConnectionConfig::canExtract),
                    RedstoneControl.CODEC.fieldOf("redstone_control")
                            .forGetter(EnergyConduitConnectionConfig::redstoneControl),
                    DyeColor.CODEC.fieldOf("redstone_channel")
                            .forGetter(EnergyConduitConnectionConfig::redstoneChannel))
            .apply(instance, EnergyConduitConnectionConfig::new));

    public static StreamCodec<ByteBuf, EnergyConduitConnectionConfig> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EnergyConduitConnectionConfig::canInsert, ByteBufCodecs.BOOL,
            EnergyConduitConnectionConfig::canExtract, RedstoneControl.STREAM_CODEC,
            EnergyConduitConnectionConfig::redstoneControl, DyeColor.STREAM_CODEC,
            EnergyConduitConnectionConfig::redstoneChannel, EnergyConduitConnectionConfig::new);

    public static final ConnectionConfigType<EnergyConduitConnectionConfig> TYPE = new ConnectionConfigType<>(
            EnergyConduitConnectionConfig.class, CODEC, STREAM_CODEC.cast(), () -> DEFAULT);

    @Override
    public ConnectionConfig reconnected() {
        return new EnergyConduitConnectionConfig(DEFAULT.canInsert, DEFAULT.canExtract, redstoneControl,
                redstoneChannel);
    }

    @Override
    public IOConnectionConfig withInsert(boolean canInsert) {
        return new EnergyConduitConnectionConfig(canInsert, canExtract, redstoneControl, redstoneChannel);
    }

    @Override
    public IOConnectionConfig withExtract(boolean canExtract) {
        return new EnergyConduitConnectionConfig(canInsert, canExtract, redstoneControl, redstoneChannel);
    }

    @Override
    public ConnectionConfigType<?> type() {
        return TYPE;
    }

    @Override
    public RedstoneControlledConnection withRedstoneControl(RedstoneControl redstoneControl) {
        return new EnergyConduitConnectionConfig(canInsert, canExtract, redstoneControl, redstoneChannel);
    }

    @Override
    public RedstoneControlledConnection withRedstoneChannel(DyeColor redstoneChannel) {
        return new EnergyConduitConnectionConfig(canInsert, canExtract, redstoneControl, redstoneChannel);
    }
}
