package com.enderio.conduits.api.connection;

import com.enderio.base.api.misc.RedstoneControl;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

// If a conduit does not support redstone control, dye control etc. then set them to a sensible value and ignore it.
// TODO: In EnderIO 8 I would like this to be merged into ConduitData somehow.
//       we could do this in EIO 7, but it would be a much more significant API/save break.
public record ConduitConnection(
    ConduitConnectionMode mode,
    DyeColor inputChannel,
    DyeColor outputChannel,
    RedstoneControl redstoneControl,
    DyeColor redstoneChannel
) {

    public static Codec<ConduitConnection> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            ConduitConnectionMode.CODEC.fieldOf("mode").forGetter(ConduitConnection::mode),
            DyeColor.CODEC.fieldOf("input_channel").forGetter(ConduitConnection::inputChannel),
            DyeColor.CODEC.fieldOf("output_channel").forGetter(ConduitConnection::outputChannel),
            RedstoneControl.CODEC.fieldOf("redstone_control").forGetter(ConduitConnection::redstoneControl),
            DyeColor.CODEC.fieldOf("redstone_channel").forGetter(ConduitConnection::redstoneChannel)
        ).apply(instance, ConduitConnection::new)
    );

    public static StreamCodec<ByteBuf, ConduitConnection> STREAM_CODEC = StreamCodec.composite(
        ConduitConnectionMode.STREAM_CODEC,
        ConduitConnection::mode,
        DyeColor.STREAM_CODEC,
        ConduitConnection::inputChannel,
        DyeColor.STREAM_CODEC,
        ConduitConnection::outputChannel,
        RedstoneControl.STREAM_CODEC,
        ConduitConnection::redstoneControl,
        DyeColor.STREAM_CODEC,
        ConduitConnection::redstoneChannel,
        ConduitConnection::new
    );

    // No-channel constructor
    public ConduitConnection(ConduitConnectionMode mode) {
        this(mode, DyeColor.GREEN, DyeColor.GREEN);
    }

    // No-redstone constructor
    public ConduitConnection(ConduitConnectionMode mode, DyeColor inputChannel, DyeColor outputChannel) {
        this(mode, inputChannel, outputChannel, RedstoneControl.ALWAYS_ACTIVE, DyeColor.RED);
    }

    public boolean canInput() {
        return mode.canInput();
    }

    public boolean canOutput() {
        return mode.canOutput();
    }

    public ConduitConnection withMode(ConduitConnectionMode mode) {
        return new ConduitConnection(mode, inputChannel, outputChannel, redstoneControl, redstoneChannel);
    }

    public ConduitConnection withInsertChannel(DyeColor insertChannel) {
        return new ConduitConnection(mode, insertChannel, outputChannel, redstoneControl, redstoneChannel);
    }

    public ConduitConnection withExtractChannel(DyeColor extractChannel) {
        return new ConduitConnection(mode, inputChannel, extractChannel, redstoneControl, redstoneChannel);
    }

    public ConduitConnection withRedstoneControl(RedstoneControl redstoneControl) {
        return new ConduitConnection(mode, inputChannel, outputChannel, redstoneControl, redstoneChannel);
    }

    public ConduitConnection withRedstoneChannel(DyeColor redstoneChannel) {
        return new ConduitConnection(mode, inputChannel, outputChannel, redstoneControl, redstoneChannel);
    }

}
