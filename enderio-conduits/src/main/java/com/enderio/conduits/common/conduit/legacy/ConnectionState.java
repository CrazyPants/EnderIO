package com.enderio.conduits.common.conduit.legacy;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.NotImplementedException;

@Deprecated(forRemoval = true, since = "7.2")
public sealed interface ConnectionState permits StaticConnectionStates, DynamicConnectionState {

    Codec<ConnectionState> CODEC =
        Codec.either(StaticConnectionStates.CODEC, DynamicConnectionState.CODEC)
            .xmap(e -> e.left().isPresent() ? e.left().get() : e.right().get(), e -> {
                if (e instanceof StaticConnectionStates staticConnectionStates) {
                    return Either.left(staticConnectionStates);
                } else if (e instanceof DynamicConnectionState dynamicConnectionState) {
                    return Either.right(dynamicConnectionState);
                }

                throw new NotImplementedException();
            });

    StreamCodec<RegistryFriendlyByteBuf, ConnectionState> STREAM_CODEC =
        ByteBufCodecs.either(StaticConnectionStates.STREAM_CODEC, DynamicConnectionState.STREAM_CODEC)
            .map(e -> e.left().isPresent() ? e.left().get() : e.right().get(), e -> {
                if (e instanceof StaticConnectionStates staticConnectionStates) {
                    return Either.left(staticConnectionStates);
                } else if (e instanceof DynamicConnectionState dynamicConnectionState) {
                    return Either.right(dynamicConnectionState);
                }

                throw new NotImplementedException();
            });

    boolean isConnection();
}
