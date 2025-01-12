package com.enderio.conduits.common.conduit.type.item;

import com.enderio.conduits.api.network.node.NodeData;
import com.enderio.conduits.api.network.node.NodeDataType;
import com.enderio.conduits.common.init.ConduitTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Direction;

/**
 * Stores round-robin indexes for this node.
 */
public final class ItemConduitNodeData implements NodeData {

    public static MapCodec<ItemConduitNodeData> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Codec.unboundedMap(Direction.CODEC, Codec.INT)
                    .fieldOf("round_robin_indexes")
                    .forGetter(i -> i.roundRobinIndexes)).apply(instance, ItemConduitNodeData::new));

    public static NodeDataType<ItemConduitNodeData> TYPE = new NodeDataType<>(ItemConduitNodeData.CODEC,
            ItemConduitNodeData::new);
    private final Map<Direction, Integer> roundRobinIndexes;

    public ItemConduitNodeData() {
        this(Map.of());
    }

    public ItemConduitNodeData(Map<Direction, Integer> roundRobinIndexes) {
        this.roundRobinIndexes = new HashMap<>(roundRobinIndexes);
    }

    @Override
    public NodeDataType<?> type() {
        return ConduitTypes.NodeData.ITEM.get();
    }

    public int getIndex(Direction side) {
        return roundRobinIndexes.getOrDefault(side, 0);
    }

    public void setIndex(Direction side, int index) {
        roundRobinIndexes.put(side, index);
    }
}
