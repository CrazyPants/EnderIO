package com.enderio.conduits.common.conduit.graph;

import com.enderio.conduits.api.network.node.legacy.ConduitData;
import com.enderio.conduits.api.network.node.legacy.ConduitDataAccessor;
import com.enderio.conduits.api.network.node.legacy.ConduitDataType;
import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

/**
 * A safe way to store conduit data.
 */
@Deprecated(forRemoval = true, since = "7.2")
public class ConduitDataContainer implements ConduitDataAccessor {

    public static Codec<ConduitDataContainer> CODEC = ExtraCodecs.optionalEmptyMap(ConduitData.CODEC)
            .xmap(ConduitDataContainer::new, i -> Optional.ofNullable(i.data));

    public static StreamCodec<RegistryFriendlyByteBuf, ConduitDataContainer> STREAM_CODEC = ByteBufCodecs
            .optional(ConduitData.STREAM_CODEC)
            .map(ConduitDataContainer::new, i -> Optional.ofNullable(i.data));

    @Nullable
    private ConduitData<?> data;

    public ConduitDataContainer() {
    }

    public ConduitDataContainer(@Nullable ConduitData<?> data) {
        this.data = data;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private ConduitDataContainer(Optional<ConduitData<?>> data) {
        this.data = data.orElse(null);
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean hasData(ConduitDataType<?> type) {
        return data != null && data.type() == type;
    }

    @Nullable
    public ConduitData<?> getData() {
        return data;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends ConduitData<T>> T getData(ConduitDataType<T> type) {
        if (data != null && type == data.type()) {
            return (T) data;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends ConduitData<T>> T getOrCreateData(ConduitDataType<T> type) {
        if (data != null && type == data.type()) {
            return (T) data;
        }

        data = type.factory().get();
        return (T) data;
    }

    public Tag save(HolderLookup.Provider lookupProvider) {
        return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getPartialOrThrow();
    }

    public static ConduitDataContainer parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag).getPartialOrThrow();
    }

    public ConduitDataContainer deepCopy() {
        return new ConduitDataContainer(data == null ? null : data.deepCopy());
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
