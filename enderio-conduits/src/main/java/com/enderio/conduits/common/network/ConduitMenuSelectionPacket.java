package com.enderio.conduits.common.network;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConduitMenuSelectionPacket(
    Holder<Conduit<?, ?>> conduit
) implements CustomPacketPayload {

    public static final Type<ConduitMenuSelectionPacket> TYPE = new Type<>(EnderIO.loc("conduit_menu_selection"));

    public static StreamCodec<RegistryFriendlyByteBuf, ConduitMenuSelectionPacket> STREAM_CODEC =
        ByteBufCodecs.holderRegistry(EnderIOConduitsRegistries.Keys.CONDUIT)
            .map(ConduitMenuSelectionPacket::new, ConduitMenuSelectionPacket::conduit);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
