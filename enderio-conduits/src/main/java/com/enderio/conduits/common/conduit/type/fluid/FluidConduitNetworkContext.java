package com.enderio.conduits.common.conduit.type.fluid;

import com.enderio.conduits.api.network.ConduitNetworkContext;
import com.enderio.conduits.api.network.ConduitNetworkContextType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

// TODO: packet to clear the fluid lock.
public class FluidConduitNetworkContext implements ConduitNetworkContext<FluidConduitNetworkContext> {

    public static Codec<FluidConduitNetworkContext> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(BuiltInRegistries.FLUID.byNameCodec()
                    .optionalFieldOf("locked_fluid", Fluids.EMPTY)
                    .forGetter(FluidConduitNetworkContext::lockedFluid))
            .apply(instance, FluidConduitNetworkContext::new));

    public static ConduitNetworkContextType<FluidConduitNetworkContext> TYPE = new ConduitNetworkContextType<>(CODEC,
            FluidConduitNetworkContext::new);

    private Fluid lockedFluid;

    public FluidConduitNetworkContext() {
        this(Fluids.EMPTY);
    }

    public FluidConduitNetworkContext(Fluid lockedFluid) {
        this.lockedFluid = lockedFluid;
    }

    public Fluid lockedFluid() {
        return lockedFluid;
    }

    public void setLockedFluid(Fluid lockedFluid) {
        this.lockedFluid = lockedFluid;
    }

    @Override
    public FluidConduitNetworkContext mergeWith(FluidConduitNetworkContext other) {
        // Not doing anything here because these graph's should not merge unless the
        // locked fluid is the same.
        return this;
    }

    @Override
    public FluidConduitNetworkContext copy() {
        return new FluidConduitNetworkContext(lockedFluid);
    }

    @Override
    public ConduitNetworkContextType<FluidConduitNetworkContext> type() {
        return TYPE;
    }
}
