package com.enderio.conduits.api.connection;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum ConduitConnectionMode implements StringRepresentable {
    /**
     * In mode - for conduits which deposit resources or signals into the attached block.
     */
    IN("in", true, false),

    /**
     * Out mode - for conduits which extract resources or signals from the attached block.
     */

    OUT("out", false, true),

    /**
     * Both mode - for conduits which can both insert and extract resources or signals from the attached block.
     */
    BOTH("both", true, true);

    public static final Codec<ConduitConnectionMode> CODEC = StringRepresentable
            .fromEnum(ConduitConnectionMode::values);
    public static final IntFunction<ConduitConnectionMode> BY_ID = ByIdMap.continuous(Enum::ordinal, values(),
            ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ConduitConnectionMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID,
            Enum::ordinal);

    private final String name;
    private final boolean canInput;
    private final boolean canOutput;

    ConduitConnectionMode(String name, boolean canInput, boolean canOutput) {
        this.name = name;
        this.canInput = canInput;
        this.canOutput = canOutput;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public boolean canInput() {
        return canInput;
    }

    public boolean canOutput() {
        return canOutput;
    }
}
