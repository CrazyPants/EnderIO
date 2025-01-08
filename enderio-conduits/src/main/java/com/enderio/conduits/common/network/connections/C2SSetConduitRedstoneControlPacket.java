package com.enderio.conduits.common.network.connections;

import com.enderio.base.api.EnderIO;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.Conduit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.DyeColor;

public record C2SSetConduitRedstoneControlPacket(
    BlockPos pos,
    Direction side,
    Holder<Conduit<?>> conduit,
    RedstoneControl redstoneControl
) implements C2SConduitConnectionPacket, CustomPacketPayload {

    public static Type<C2SSetConduitRedstoneControlPacket> TYPE = new Type<>(EnderIO.loc("conduit_redstone_control"));

    public static StreamCodec<RegistryFriendlyByteBuf, C2SSetConduitRedstoneControlPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        C2SSetConduitRedstoneControlPacket::pos,
        Direction.STREAM_CODEC,
        C2SSetConduitRedstoneControlPacket::side,
        Conduit.STREAM_CODEC,
        C2SSetConduitRedstoneControlPacket::conduit,
        RedstoneControl.STREAM_CODEC,
        C2SSetConduitRedstoneControlPacket::redstoneControl,
        C2SSetConduitRedstoneControlPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
