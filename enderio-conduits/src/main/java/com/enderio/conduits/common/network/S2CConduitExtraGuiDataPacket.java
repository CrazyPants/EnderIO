package com.enderio.conduits.common.network;

import com.enderio.base.api.EnderIO;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

public record S2CConduitExtraGuiDataPacket(int containerId, @Nullable CompoundTag extraGuiData)
        implements CustomPacketPayload {

    public static Type<S2CConduitExtraGuiDataPacket> TYPE = new Type<>(EnderIO.loc("conduit_extra_gui_data"));

    public static StreamCodec<ByteBuf, S2CConduitExtraGuiDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2CConduitExtraGuiDataPacket::containerId,
            ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG).map(opt -> opt.orElse(null), Optional::ofNullable),
            S2CConduitExtraGuiDataPacket::extraGuiData, S2CConduitExtraGuiDataPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
