package com.enderio.conduits.api;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.api.facade.ConduitFacadeProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;

public class ConduitCapabilities {
    public static final ItemCapability<ConduitFacadeProvider, Void> CONDUIT_FACADE_PROVIDER = ItemCapability
            .createVoid(EnderIO.loc("conduit_facade_provider"), ConduitFacadeProvider.class);
}
