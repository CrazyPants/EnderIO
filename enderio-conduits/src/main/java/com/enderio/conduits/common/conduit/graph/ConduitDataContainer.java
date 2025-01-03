package com.enderio.conduits.common.conduit.graph;

import com.enderio.conduits.api.ConduitDataAccessor;
import com.enderio.conduits.api.ConduitDataType;
import com.enderio.conduits.api.ConduitData;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * A safe way to store conduit data.
 */
public class ConduitDataContainer implements ConduitDataAccessor {

    public static Codec<ConduitDataContainer> CODEC = ExtraCodecs.optionalEmptyMap(ConduitData.CODEC)
        .xmap(ConduitDataContainer::new, i -> Optional.ofNullable(i.data));

    public static StreamCodec<RegistryFriendlyByteBuf, ConduitDataContainer> STREAM_CODEC = ByteBufCodecs.optional(ConduitData.STREAM_CODEC)
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

    public boolean hasData(ConduitDataType<?> type) {
        return data != null && data.type() == type;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends ConduitData<T>> T getData(ConduitDataType<T> type) {
        if (data != null && type == data.type()) {
            return (T)data;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends ConduitData<T>> T getOrCreateData(ConduitDataType<T> type) {
        if (data != null && type == data.type()) {
            return (T)data;
        }

        data = type.factory().get();
        return (T)data;
    }

    public void handleClientChanges(ConduitDataContainer clientDataContainer) {
        // Ensure the type we contain matches the client.
        // If it does not, assume the client is out of date and ignore it.
        if (data != null && !clientDataContainer.hasData(data.type())) {
            return;
        }

        if (data != null) {
            data = applyClientChanges(data.type(), clientDataContainer);
        } else {
            data = applyClientChanges(clientDataContainer.data.type(), clientDataContainer);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends ConduitData<T>> T applyClientChanges(ConduitDataType<T> type, ConduitDataContainer clientDataContainer) {
        T myData = getOrCreateData(type);
        T clientData = clientDataContainer.getData(type);

        if (clientData == null) {
            return myData;
        }

        return myData.withClientChanges(clientData);
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
