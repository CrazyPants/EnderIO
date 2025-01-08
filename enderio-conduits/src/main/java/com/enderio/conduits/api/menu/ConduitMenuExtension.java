package com.enderio.conduits.api.menu;

import com.enderio.conduits.api.network.node.ConduitNode;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.function.Consumer;

@ApiStatus.Experimental
public class ConduitMenuExtension {
    // Enables custom button actions (like changing round robin mode) etc. for the menu.
    // this is simple enough for 90% of use cases, and others can go ahead and use a packet.
    private Map<Integer, Consumer<ConduitNode>> customButtonActions;

    @ApiStatus.Internal
    public boolean onMenuButtonClicked(ConduitNode node, int id) {
        if (customButtonActions.containsKey(id)) {
            customButtonActions.get(id).accept(node);
            return true;
        }

        return false;
    }

    public static class Builder {
        private Builder() {
        }

        public Builder addCustomButtonAction(int buttonId, Runnable action) {
            // TODO: Add to map.

            return this;
        }
    }
}
