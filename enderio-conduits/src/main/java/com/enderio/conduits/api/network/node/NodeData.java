package com.enderio.conduits.api.network.node;

import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.mojang.serialization.Codec;

/**
 * Data which is stored on each conduit node. This is not synced to the client.
 * If you want to sync data from here to the client, use {@link com.enderio.conduits.api.Conduit#getClientDataTag(ConduitNode)}.
 * For connection-related settings, use {@link ConnectionConfig}.
 */
public interface NodeData {
    Codec<NodeData> GENERIC_CODEC = EnderIOConduitsRegistries.CONDUIT_NODE_DATA_TYPE.byNameCodec()
        .dispatch(NodeData::type, NodeDataType::codec);

    NodeDataType<?> type();
}
