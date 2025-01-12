package com.enderio.conduits.common.conduit.type.energy;

import com.enderio.conduits.api.network.ConduitNetworkContext;
import com.enderio.conduits.api.network.ConduitNetworkContextType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class EnergyConduitNetworkContext implements ConduitNetworkContext<EnergyConduitNetworkContext> {

    public static final Codec<EnergyConduitNetworkContext> CODEC = RecordCodecBuilder.create(
        builder -> builder.group(
            Codec.INT.fieldOf("energy_stored").forGetter(i -> i.energyStored),
            Codec.INT.fieldOf("rotating_index").forGetter(i -> i.rotatingIndex)
        ).apply(builder, EnergyConduitNetworkContext::new)
    );

    public static ConduitNetworkContextType<EnergyConduitNetworkContext> TYPE = new ConduitNetworkContextType<>(CODEC, EnergyConduitNetworkContext::new);

    private int energyStored = 0;
    private int rotatingIndex = 0;

    public EnergyConduitNetworkContext() {
    }

    public EnergyConduitNetworkContext(int energyStored) {
        this.energyStored = energyStored;
    }

    public EnergyConduitNetworkContext(int energyStored, int rotatingIndex) {
        this.energyStored = energyStored;
        this.rotatingIndex = rotatingIndex;
    }

    /**
     * @implNote Never trust the value stored here, always Min it with the capacity. When the graph splits, this will just be copied across all sides.
     */
    public int energyStored() {
        return energyStored;
    }

    public void setEnergyStored(int energyStored) {
        this.energyStored = energyStored;
    }

    public int rotatingIndex() {
        return rotatingIndex;
    }

    public void setRotatingIndex(int rotatingIndex) {
        this.rotatingIndex = rotatingIndex;
    }

    @Override
    public EnergyConduitNetworkContext mergeWith(EnergyConduitNetworkContext other) {
        return new EnergyConduitNetworkContext(this.energyStored + other.energyStored);
    }

    @Override
    public EnergyConduitNetworkContext copy() {
        return new EnergyConduitNetworkContext(energyStored);
    }

    @Override
    public ConduitNetworkContextType<EnergyConduitNetworkContext> type() {
        return TYPE;
    }
}
