package com.enderio.conduits.client.model.conduit.modifier;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.enderio.conduits.api.model.ConduitModelModifier;
import net.minecraft.core.Direction;

public class RedstoneConduitModelModifier implements ConduitModelModifier {
    @Override
    public boolean shouldShowFakeConnection(ConduitBundleReader reader, Direction side) {
        if (!reader.isEndpoint(side)) {
            return false;
        }

        // Find a conduit that might be using our signal.
        for (var conduit : reader.getConnectedConduits(side)) {
            var config = reader.getConnectionConfig(side, conduit);

            if (config instanceof RedstoneControlledConnection redstoneControlledConnection) {
                return redstoneControlledConnection.redstoneControl() != RedstoneControl.ALWAYS_ACTIVE &&
                    redstoneControlledConnection.redstoneControl() != RedstoneControl.NEVER_ACTIVE;
            }
        }

        return false;
    }
}
