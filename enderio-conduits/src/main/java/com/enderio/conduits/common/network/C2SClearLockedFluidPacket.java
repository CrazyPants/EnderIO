package com.enderio.conduits.common.network;

import com.enderio.base.api.EnderIO;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record C2SClearLockedFluidPacket(BlockPos pos) implements CustomPacketPayload {

    public static final Type<C2SClearLockedFluidPacket> TYPE = new Type<>(EnderIO.loc("clear_locked_fluid"));

    public static StreamCodec<ByteBuf, C2SClearLockedFluidPacket> STREAM_CODEC = BlockPos.STREAM_CODEC
            .map(C2SClearLockedFluidPacket::new, C2SClearLockedFluidPacket::pos);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
