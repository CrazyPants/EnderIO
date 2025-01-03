package com.enderio.conduits.common.conduit.connection;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/**
 * This class is for all static ConnectionStates without being connected to a block. For this use {@link DynamicConnectionState} <br>
 * It's intended usage is to remove the full nullability things of ConnectionStates
 * {@linkplain StaticConnectionStates#CONNECTED CONNECTED} is for all conduits that extends in a direction, but are not showing a connector
 * {@linkplain StaticConnectionStates#CONNECTED_ACTIVE CONNECTED_ACTIVE} is for all conduits that extends in a direction, but are not showing a connector and should use their deactivated texture and emissiveness
 * {@linkplain StaticConnectionStates#DISCONNECTED DISCONNECTED} is for no Connection
 * {@linkplain StaticConnectionStates#DISABLED DISABLED} is for no connection and activly disabled, so they don't connect, when a conduit is placed next to it (this state is set using the wrench or when IO is both disabled the state returns to this)
 */
@Deprecated(forRemoval = true, since = "7.2")
public enum StaticConnectionStates implements ConnectionState, StringRepresentable {
    CONNECTED(0, "connected"), CONNECTED_ACTIVE(1, "connected_active"), DISCONNECTED(2, "disconnected"),
    DISABLED(3, "disabled");

    public static final Codec<StaticConnectionStates> CODEC = StringRepresentable
            .fromEnum(StaticConnectionStates::values);
    public static final IntFunction<StaticConnectionStates> BY_ID = ByIdMap.continuous(key -> key.id, values(),
            ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, StaticConnectionStates> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID,
            v -> v.id);

    private final int id;
    private final String name;

    StaticConnectionStates(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean isConnection() {
        return this == CONNECTED || this == CONNECTED_ACTIVE;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
