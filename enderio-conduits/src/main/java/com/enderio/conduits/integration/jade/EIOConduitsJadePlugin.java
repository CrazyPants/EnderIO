package com.enderio.conduits.integration.jade;

import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.client.model.conduit.facades.FacadeHelper;
import com.enderio.conduits.common.init.ConduitBlocks;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class EIOConduitsJadePlugin implements IWailaPlugin {

    // TODO: Could implement stuff like a waila tooltip for bound souls.

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Show the correct conduit (or facade item)
        registration.usePickedResult(ConduitBlocks.CONDUIT.get());

        // Completely replace the block accessor with the facade block if it exists
        registration.addRayTraceCallback((hitResult, accessor, originalAccessor) -> {
            if (accessor instanceof BlockAccessor blockAccessor) {
                if (blockAccessor.getBlockEntity() instanceof ConduitBundleReader conduitBundle && conduitBundle.hasFacade() && FacadeHelper.areFacadesVisible()) {
                    return registration.blockAccessor().from(blockAccessor).blockState(conduitBundle.getFacadeBlock().defaultBlockState()).build();
                }
            }
            return accessor;
        });
    }
}
