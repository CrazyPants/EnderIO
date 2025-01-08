package com.enderio.conduits.common.network.connections;

import com.enderio.base.api.EnderIO;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.Conduit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.DyeColor;

public record C2SSetConduitChannelPacket(
    BlockPos pos,
    Direction side,
    Holder<Conduit<?, ?>> conduit,
    Side channelSide,
    DyeColor channelColor
) implements C2SConduitConnectionPacket, CustomPacketPayload {

    public static Type<C2SSetConduitChannelPacket> TYPE = new Type<>(EnderIO.loc("conduit_channel"));

    public static StreamCodec<RegistryFriendlyByteBuf, C2SSetConduitChannelPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        C2SSetConduitChannelPacket::pos,
        Direction.STREAM_CODEC,
        C2SSetConduitChannelPacket::side,
        Conduit.STREAM_CODEC,
        C2SSetConduitChannelPacket::conduit,
        ByteBufCodecs.BOOL.map(b -> b ? Side.INPUT : Side.OUTPUT, s -> s == Side.INPUT),
        C2SSetConduitChannelPacket::channelSide,
        DyeColor.STREAM_CODEC,
        C2SSetConduitChannelPacket::channelColor,
        C2SSetConduitChannelPacket::new
    );

    public enum Side {
        INPUT, OUTPUT,
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
