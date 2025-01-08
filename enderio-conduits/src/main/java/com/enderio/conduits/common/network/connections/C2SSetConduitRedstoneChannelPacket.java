package com.enderio.conduits.common.network.connections;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.api.Conduit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.DyeColor;

public record C2SSetConduitRedstoneChannelPacket(
    BlockPos pos,
    Direction side,
    Holder<Conduit<?>> conduit,
    DyeColor redstoneChannel
) implements C2SConduitConnectionPacket, CustomPacketPayload {

    public static Type<C2SSetConduitRedstoneChannelPacket> TYPE = new Type<>(EnderIO.loc("conduit_redstone_channel"));

    public static StreamCodec<RegistryFriendlyByteBuf, C2SSetConduitRedstoneChannelPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        C2SSetConduitRedstoneChannelPacket::pos,
        Direction.STREAM_CODEC,
        C2SSetConduitRedstoneChannelPacket::side,
        Conduit.STREAM_CODEC,
        C2SSetConduitRedstoneChannelPacket::conduit,
        DyeColor.STREAM_CODEC,
        C2SSetConduitRedstoneChannelPacket::redstoneChannel,
        C2SSetConduitRedstoneChannelPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
