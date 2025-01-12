package com.enderio.conduits.common.conduit.type.redstone;

import com.enderio.conduits.api.network.ConduitNetworkContext;
import com.enderio.conduits.api.network.ConduitNetworkContextType;
import net.minecraft.world.item.DyeColor;

import java.util.HashMap;
import java.util.Map;

public class RedstoneConduitNetworkContext implements ConduitNetworkContext<RedstoneConduitNetworkContext> {

    // Redstone context is not saved as it is recalculated every graph tick if the network is active.
    public static ConduitNetworkContextType<RedstoneConduitNetworkContext> TYPE = new ConduitNetworkContextType<>(null, RedstoneConduitNetworkContext::new);

    private boolean isActive;
    private Map<DyeColor, Integer> previousChannelSignals = new HashMap<>();
    private Map<DyeColor, Integer> channelSignals = new HashMap<>();

    private NewNetworkDelay newNetworkDelay = NewNetworkDelay.NEW;

    public RedstoneConduitNetworkContext() {
    }

    private RedstoneConduitNetworkContext(boolean isActive, HashMap<DyeColor, Integer> channelSignals) {
        this.isActive = isActive;
        this.channelSignals = channelSignals;
    }

    public boolean isNew() {
        return newNetworkDelay != NewNetworkDelay.OLD;
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

    // Used for change detection in the ticker.
    public int getSignalLastTick(DyeColor color) {
        return previousChannelSignals.getOrDefault(color, 0);
    }

    public void nextTick() {
        if (newNetworkDelay == NewNetworkDelay.NEW) {
            newNetworkDelay = NewNetworkDelay.NEW_DECAY;
        } else if (newNetworkDelay == NewNetworkDelay.NEW_DECAY) {
            newNetworkDelay = NewNetworkDelay.OLD;
        }

        // Save for change detection
        for (DyeColor color : DyeColor.values()) {
            previousChannelSignals.put(color, getSignal(color));
        }

        channelSignals.clear();
        isActive = false;
    }

    public void setSignal(DyeColor color, int signal) {
        if (getSignal(color) < signal) {
            channelSignals.put(color, signal);
        }

        isActive = channelSignals.values().stream().anyMatch(i -> i > 0);
    }

    @Override
    public RedstoneConduitNetworkContext mergeWith(RedstoneConduitNetworkContext other) {
        return copy();
    }

    @Override
    public RedstoneConduitNetworkContext copy() {
        return new RedstoneConduitNetworkContext(isActive, new HashMap<>(channelSignals));
    }

    @Override
    public ConduitNetworkContextType<RedstoneConduitNetworkContext> type() {
        return TYPE;
    }

    // Because the context is cleared before the graph ticks, we need to represent "newness" as three states.
    private enum NewNetworkDelay {
        NEW, NEW_DECAY, OLD
    }
}
