package com.enderio.conduits.common.conduit.type.item;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.io.ChanneledIOConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.connection.config.io.IOConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.enderio.conduits.common.init.ConduitTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;

public record ItemConduitConnectionConfig(
    boolean canInsert,
    DyeColor insertChannel,
    boolean canExtract,
    DyeColor extractChannel,
    RedstoneControl redstoneControl,
    DyeColor redstoneChannel,
    boolean isRoundRobin,
    boolean isSelfFeed,
    int priority
) implements ChanneledIOConnectionConfig, RedstoneControlledConnection {

    public static ItemConduitConnectionConfig DEFAULT = new ItemConduitConnectionConfig(false, DyeColor.GREEN, true, DyeColor.GREEN,
        RedstoneControl.NEVER_ACTIVE, DyeColor.RED, false, false, 0);

    public static MapCodec<ItemConduitConnectionConfig> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.BOOL.fieldOf("can_insert").forGetter(ItemConduitConnectionConfig::canInsert),
            DyeColor.CODEC.fieldOf("insert_channel").forGetter(ItemConduitConnectionConfig::insertChannel),
            Codec.BOOL.fieldOf("can_extract").forGetter(ItemConduitConnectionConfig::canExtract),
            DyeColor.CODEC.fieldOf("extract_channel").forGetter(ItemConduitConnectionConfig::extractChannel),
            RedstoneControl.CODEC.fieldOf("redstone_control").forGetter(ItemConduitConnectionConfig::redstoneControl),
            DyeColor.CODEC.fieldOf("redstone_channel").forGetter(ItemConduitConnectionConfig::redstoneChannel),
            Codec.BOOL.fieldOf("is_round_robin").forGetter(ItemConduitConnectionConfig::isRoundRobin),
            Codec.BOOL.fieldOf("is_self_feed").forGetter(ItemConduitConnectionConfig::isSelfFeed),
            Codec.INT.fieldOf("priority").forGetter(ItemConduitConnectionConfig::priority)
        ).apply(instance, ItemConduitConnectionConfig::new)
    );

    public static ConnectionConfigType<ItemConduitConnectionConfig> TYPE = new ConnectionConfigType<>(ItemConduitConnectionConfig.class, CODEC, () -> DEFAULT);

    @Override
    public ConnectionConfig reconnected() {
        return new ItemConduitConnectionConfig(DEFAULT.canInsert, DEFAULT.insertChannel, DEFAULT.canExtract, DEFAULT.extractChannel, redstoneControl,
            redstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    @Override
    public IOConnectionConfig withInsert(boolean canInsert) {
        return new ItemConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    @Override
    public IOConnectionConfig withExtract(boolean canExtract) {
        return new ItemConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    @Override
    public ItemConduitConnectionConfig withInputChannel(DyeColor inputChannel) {
        return new ItemConduitConnectionConfig(canInsert, inputChannel, canExtract, extractChannel, redstoneControl, redstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    @Override
    public ItemConduitConnectionConfig withOutputChannel(DyeColor outputChannel) {
        return new ItemConduitConnectionConfig(canInsert, insertChannel, canExtract, outputChannel, redstoneControl, redstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    @Override
    public ItemConduitConnectionConfig withRedstoneControl(RedstoneControl redstoneControl) {
        return new ItemConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    @Override
    public RedstoneControlledConnection withRedstoneChannel(DyeColor redstoneChannel) {
        return new ItemConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withRoundRobin(boolean isRoundRobin) {
        return new ItemConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withSelfFeed(boolean isSelfFeed) {
        return new ItemConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    public ItemConduitConnectionConfig withPriority(int priority) {
        return new ItemConduitConnectionConfig(canInsert, insertChannel, canExtract, extractChannel, redstoneControl, redstoneChannel, isRoundRobin, isSelfFeed, priority);
    }

    @Override
    public ConnectionConfigType<ItemConduitConnectionConfig> type() {
        return ConduitTypes.ConnectionTypes.ITEM.get();
    }
}
