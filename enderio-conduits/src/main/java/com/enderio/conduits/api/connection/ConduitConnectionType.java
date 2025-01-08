package com.enderio.conduits.api.connection;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;

public enum ConduitConnectionType implements StringRepresentable {
    /**
     * This conduit is not connected to anything.
     */
    NONE("none"),

    /**
     * This conduit is connected to a block for extract.
     */
    CONNECTED_BLOCK("connected_block"),

    /**
     * This conduit is connected to another conduit.
     */
    CONNECTED_CONDUIT("connected_conduit"),

    /**
     * Intentionally disabled by the player, should not automatically reconnect without Yeta Wrench.
     */
    DISABLED("disabled");

    public static final StringRepresentable.EnumCodec<ConduitConnectionType> CODEC = StringRepresentable
            .fromEnum(ConduitConnectionType::values);
    public static final IntFunction<ConduitConnectionType> BY_ID = ByIdMap.continuous(Enum::ordinal, values(),
            ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ConduitConnectionType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID,
            Enum::ordinal);

    private final String name;

    ConduitConnectionType(String name) {
        this.name = name;
    }

    /**
     * @return Whether a new connection can be made from this face.
     */
    public boolean canConnect() {
        return this == NONE;
    }

    /**
     * @return Whether this face is connected.
     */
    public boolean isConnected() {
        return this != NONE && this != DISABLED;
    }

    /**
     * @return Whether this face is connected to a block.
     */
    public boolean isEndpoint() {
        return this == CONNECTED_BLOCK;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Nullable
    public static ConduitConnectionType byName(@Nullable String name) {
        return CODEC.byName(name);
    }
}
