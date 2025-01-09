package com.enderio.base.api.misc;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum RedstoneControl implements StringRepresentable {

    ALWAYS_ACTIVE(0, "always_active", bool -> true, false),
    ACTIVE_WITH_SIGNAL(1, "active_with_signal", bool -> bool, true),
    ACTIVE_WITHOUT_SIGNAL(2, "active_without_signal", bool -> !bool, true),
    NEVER_ACTIVE(3, "never_active", bool -> false, false);

    public static final Codec<RedstoneControl> CODEC = StringRepresentable.fromEnum(RedstoneControl::values);
    public static final IntFunction<RedstoneControl> BY_ID = ByIdMap.continuous(key -> key.id, values(),
            ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, RedstoneControl> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, v -> v.id);

    private final int id;
    private final String name;
    private final UnaryOperator<Boolean> isActive;
    private final boolean isRedstoneSensitive;

    RedstoneControl(int id, String name, UnaryOperator<Boolean> isActive, boolean isRedstoneSensitive) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
        this.isRedstoneSensitive = isRedstoneSensitive;
    }

    public boolean isActive(boolean hasRedstone) {
        return isActive.apply(hasRedstone);
    }

    public boolean isRedstoneSensitive() {
        return isRedstoneSensitive;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public Tag save(HolderLookup.Provider lookupProvider) {
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    public static RedstoneControl parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
    }
}
