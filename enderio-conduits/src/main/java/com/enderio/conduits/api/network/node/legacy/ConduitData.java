package com.enderio.conduits.api.network.node.legacy;

import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.api.network.node.NodeData;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public interface ConduitData<T extends ConduitData<T>> {
    Codec<ConduitData<?>> CODEC = EnderIOConduitsRegistries.CONDUIT_DATA_TYPE.byNameCodec()
            .dispatch(ConduitData::type, ConduitDataType::codec);
    StreamCodec<RegistryFriendlyByteBuf, ConduitData<?>> STREAM_CODEC = ByteBufCodecs
            .registry(EnderIOConduitsRegistries.Keys.CONDUIT_DATA_TYPE)
            .dispatch(ConduitData::type, ConduitDataType::streamCodec);

    /**
     * Allows copying of data from a client change.
     * By default allows no changes.
     */
    default T withClientChanges(T guiData) {
        // noinspection unchecked
        return (T) this;
    }

    T deepCopy();

    ConduitDataType<T> type();

    /**
     * Convert to modern node data.
     * @return the new node data, or null if this data should be discarded.
     */
    @Nullable
    NodeData toNodeData();
}
