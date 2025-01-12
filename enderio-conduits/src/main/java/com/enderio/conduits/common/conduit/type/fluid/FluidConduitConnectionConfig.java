package com.enderio.conduits.common.conduit.type.fluid;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.connection.config.io.ChanneledIOConnectionConfig;
import com.enderio.conduits.api.connection.config.io.IOConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

public record FluidConduitConnectionConfig(
    boolean canInsert,
    DyeColor insertChannel,
    boolean canExtract,
    DyeColor extractChannel,
    RedstoneControl redstoneControl,
    DyeColor redstoneChannel
) implements ChanneledIOConnectionConfig, RedstoneControlledConnection {

    public static FluidConduitConnectionConfig DEFAULT = new FluidConduitConnectionConfig(false, DyeColor.GREEN, true, DyeColor.GREEN,
        RedstoneControl.NEVER_ACTIVE, DyeColor.RED);

    public static MapCodec<FluidConduitConnectionConfig> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.BOOL.fieldOf("can_insert").forGetter(FluidConduitConnectionConfig::canInsert),
            DyeColor.CODEC.fieldOf("insert_channel").forGetter(FluidConduitConnectionConfig::insertChannel),
            Codec.BOOL.fieldOf("can_extract").forGetter(FluidConduitConnectionConfig::canExtract),
            DyeColor.CODEC.fieldOf("extract_channel").forGetter(FluidConduitConnectionConfig::extractChannel),
            RedstoneControl.CODEC.fieldOf("redstone_control").forGetter(FluidConduitConnectionConfig::redstoneControl),
            DyeColor.CODEC.fieldOf("redstone_channel").forGetter(FluidConduitConnectionConfig::redstoneChannel)
        ).apply(instance, FluidConduitConnectionConfig::new)
    );

    public static StreamCodec<ByteBuf, FluidConduitConnectionConfig> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        FluidConduitConnectionConfig::canInsert,
        DyeColor.STREAM_CODEC,
        FluidConduitConnectionConfig::insertChannel,
        ByteBufCodecs.BOOL,
        FluidConduitConnectionConfig::canExtract,
        DyeColor.STREAM_CODEC,
        FluidConduitConnectionConfig::extractChannel,
        RedstoneControl.STREAM_CODEC,
        FluidConduitConnectionConfig::redstoneControl,
        DyeColor.STREAM_CODEC,
        FluidConduitConnectionConfig::redstoneChannel,
        FluidConduitConnectionConfig::new
    );

    public static ConnectionConfigType<FluidConduitConnectionConfig> TYPE = new ConnectionConfigType<>(
            FluidConduitConnectionConfig.class, CODEC, STREAM_CODEC.cast(), () -> DEFAULT);

    @Override
    public ConnectionConfig reconnected() {
        return new FluidConduitConnectionConfig(DEFAULT.canInsert, DEFAULT.insertChannel, DEFAULT.canExtract, DEFAULT.extractChannel, redstoneControl,
            redstoneChannel);
    }

    @Override
    public IOConnectionConfig withInsert(boolean canInsert) {
        return new FluidConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel);
    }

    @Override
    public IOConnectionConfig withExtract(boolean canExtract) {
        return new FluidConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel);
    }

    @Override
    public FluidConduitConnectionConfig withInputChannel(DyeColor inputChannel) {
        return new FluidConduitConnectionConfig(canInsert, inputChannel, canExtract, extractChannel, redstoneControl, redstoneChannel);
    }

    @Override
    public FluidConduitConnectionConfig withOutputChannel(DyeColor outputChannel) {
        return new FluidConduitConnectionConfig(canInsert, insertChannel, canExtract, outputChannel, redstoneControl, redstoneChannel);
    }

    @Override
    public FluidConduitConnectionConfig withRedstoneControl(RedstoneControl redstoneControl) {
        return new FluidConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel);
    }

    @Override
    public RedstoneControlledConnection withRedstoneChannel(DyeColor redstoneChannel) {
        return new FluidConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel);
    }

    @Override
    public ConnectionConfigType<FluidConduitConnectionConfig> type() {
        return TYPE;
    }
}
