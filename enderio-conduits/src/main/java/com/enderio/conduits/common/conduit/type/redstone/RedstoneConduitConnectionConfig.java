package com.enderio.conduits.common.conduit.type.redstone;

import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.connection.config.io.ChanneledIOConnectionConfig;
import com.enderio.conduits.api.connection.config.io.IOConnectionConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

public record RedstoneConduitConnectionConfig(
    boolean canInsert,
    DyeColor insertChannel,
    boolean canExtract,
    DyeColor extractChannel
) implements ChanneledIOConnectionConfig {

    public static RedstoneConduitConnectionConfig DEFAULT = new RedstoneConduitConnectionConfig(false, DyeColor.GREEN, true, DyeColor.RED);

    public static MapCodec<RedstoneConduitConnectionConfig> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.BOOL.fieldOf("can_insert").forGetter(RedstoneConduitConnectionConfig::canInsert),
            DyeColor.CODEC.fieldOf("insert_channel").forGetter(RedstoneConduitConnectionConfig::insertChannel),
            Codec.BOOL.fieldOf("can_extract").forGetter(RedstoneConduitConnectionConfig::canExtract),
            DyeColor.CODEC.fieldOf("extract_channel").forGetter(RedstoneConduitConnectionConfig::extractChannel)
        ).apply(instance, RedstoneConduitConnectionConfig::new)
    );

    public static StreamCodec<ByteBuf, RedstoneConduitConnectionConfig> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        RedstoneConduitConnectionConfig::canInsert,
        DyeColor.STREAM_CODEC,
        RedstoneConduitConnectionConfig::insertChannel,
        ByteBufCodecs.BOOL,
        RedstoneConduitConnectionConfig::canExtract,
        DyeColor.STREAM_CODEC,
        RedstoneConduitConnectionConfig::extractChannel,
        RedstoneConduitConnectionConfig::new
    );

    public static ConnectionConfigType<RedstoneConduitConnectionConfig> TYPE = new ConnectionConfigType<>(
            RedstoneConduitConnectionConfig.class, CODEC, STREAM_CODEC.cast(), () -> DEFAULT);

    @Override
    public ConnectionConfig reconnected() {
        return new RedstoneConduitConnectionConfig(DEFAULT.canInsert, DEFAULT.insertChannel, DEFAULT.canExtract, DEFAULT.extractChannel);
    }

    @Override
    public IOConnectionConfig withInsert(boolean canInsert) {
        return new RedstoneConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel);
    }

    @Override
    public IOConnectionConfig withExtract(boolean canExtract) {
        return new RedstoneConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel);
    }

    @Override
    public RedstoneConduitConnectionConfig withInputChannel(DyeColor inputChannel) {
        return new RedstoneConduitConnectionConfig(canInsert, inputChannel, canExtract, extractChannel);
    }

    @Override
    public RedstoneConduitConnectionConfig withOutputChannel(DyeColor outputChannel) {
        return new RedstoneConduitConnectionConfig(canInsert, insertChannel, canExtract, outputChannel);
    }

    @Override
    public ConnectionConfigType<RedstoneConduitConnectionConfig> type() {
        return TYPE;
    }
}
