package com.enderio.conduits.common.conduit.type.redstone;

import com.enderio.conduits.api.network.ConduitNetworkContext;
import com.enderio.conduits.api.network.ConduitNetworkContextType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;

import java.util.HashMap;
import java.util.Map;

public class RedstoneConduitNetworkContext implements ConduitNetworkContext<RedstoneConduitNetworkContext> {

    // Redstone context is not saved as it is recalculated every graph tick if the network is active.
    public static ConduitNetworkContextType<RedstoneConduitNetworkContext> TYPE = new ConduitNetworkContextType<>(null, RedstoneConduitNetworkContext::new);

    private boolean isActive;
    private Map<DyeColor, Integer> channelSignals = new HashMap<>();

    public RedstoneConduitNetworkContext() {
    }

    private RedstoneConduitNetworkContext(boolean isActive, HashMap<DyeColor, Integer> channelSignals) {
        this.isActive = isActive;
        this.channelSignals = channelSignals;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isActive(DyeColor color) {
        return channelSignals.containsKey(color);
    }

    public int getSignal(DyeColor color) {
        return channelSignals.getOrDefault(color, 0);
    }

    public void clear() {
        channelSignals.clear();
        isActive = false;
    }

    public void setSignal(DyeColor color, int signal) {
        channelSignals.put(color, signal);
        isActive = channelSignals.values().stream().anyMatch(i -> i > 0);
    }

    @Override
    public RedstoneConduitNetworkContext mergeWith(RedstoneConduitNetworkContext other) {
        // Ticker will be responsible for updating it.
        return copy();
    }

    @Override
    public RedstoneConduitNetworkContext copy() {
        // Ticker will be responsible for updating it.
        return new RedstoneConduitNetworkContext(false, new HashMap<>());
    }

    @Override
    public ConduitNetworkContextType<RedstoneConduitNetworkContext> type() {
        return TYPE;
    }
}
