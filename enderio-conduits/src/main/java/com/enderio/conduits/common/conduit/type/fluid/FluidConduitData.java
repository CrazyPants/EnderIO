package com.enderio.conduits.common.conduit.type.fluid;

import com.enderio.conduits.api.network.node.NodeData;
import com.enderio.conduits.api.network.node.legacy.ConduitData;
import com.enderio.conduits.api.network.node.legacy.ConduitDataType;
import com.enderio.conduits.common.init.ConduitTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FluidConduitData implements ConduitData<FluidConduitData> {

    public static MapCodec<FluidConduitData> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.BOOL.fieldOf("should_reset").forGetter(i -> i.shouldReset),
            BuiltInRegistries.FLUID.byNameCodec()
                .optionalFieldOf("locked_fluid", Fluids.EMPTY)
                .forGetter(i -> i.lockedFluid)
        ).apply(instance, FluidConduitData::new)
    );

    public static StreamCodec<RegistryFriendlyByteBuf, FluidConduitData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        i -> i.shouldReset,
        ByteBufCodecs.registry(Registries.FLUID),
        i -> i.lockedFluid,
        FluidConduitData::new
    );

    private Fluid lockedFluid = Fluids.EMPTY;
    private boolean shouldReset = false;

    public FluidConduitData() {
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public FluidConduitData(boolean shouldReset, Fluid fluid) {
        this.shouldReset = shouldReset;
        this.lockedFluid = fluid;
    }

    public Fluid lockedFluid() {
        return lockedFluid;
    }

    public void setLockedFluid(Fluid lockedFluid) {
        this.lockedFluid = lockedFluid;
    }

    public boolean shouldReset() {
        return shouldReset;
    }

    public void setShouldReset(boolean shouldReset) {
        this.shouldReset = shouldReset;
    }

    @Override
    public FluidConduitData withClientChanges(FluidConduitData guiData) {
        this.shouldReset = guiData.shouldReset;

        // TODO: Soon we will swap to records which will mean this will be a new instance.
        //       This API has been designed with this pending change in mind.
        return this;
    }

    @Override
    public FluidConduitData deepCopy() {
        return new FluidConduitData(shouldReset, lockedFluid);
    }

    @Override
    public ConduitDataType<FluidConduitData> type() {
        return ConduitTypes.Data.FLUID.get();
    }

    @Override
    public @Nullable NodeData toNodeData() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shouldReset, lockedFluid);
    }
}
