package com.enderio.conduits.common.network.connections;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetConduitConnectionConfigPacket(
    int containerId,
    ConnectionConfig connectionConfig
) implements CustomPacketPayload {

    public static Type<SetConduitConnectionConfigPacket> TYPE = new Type<>(EnderIO.loc("client_set_conduit_conection_config"));

    public static StreamCodec<RegistryFriendlyByteBuf, SetConduitConnectionConfigPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT,
        SetConduitConnectionConfigPacket::containerId,
        ConnectionConfig.STREAM_CODEC,
        SetConduitConnectionConfigPacket::connectionConfig,
        SetConduitConnectionConfigPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
