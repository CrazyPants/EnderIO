package com.enderio.conduits.common.conduit.legacy;

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

@Deprecated(forRemoval = true, since = "7.2.0-alpha")
public class LegacyFluidConduitData implements ConduitData<LegacyFluidConduitData> {

    public static MapCodec<LegacyFluidConduitData> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.BOOL.fieldOf("should_reset").forGetter(i -> i.shouldReset),
            BuiltInRegistries.FLUID.byNameCodec()
                .optionalFieldOf("locked_fluid", Fluids.EMPTY)
                .forGetter(i -> i.lockedFluid)
        ).apply(instance, LegacyFluidConduitData::new)
    );

    public static StreamCodec<RegistryFriendlyByteBuf, LegacyFluidConduitData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL,
        i -> i.shouldReset,
        ByteBufCodecs.registry(Registries.FLUID),
        i -> i.lockedFluid,
        LegacyFluidConduitData::new
    );

    private Fluid lockedFluid = Fluids.EMPTY;
    private boolean shouldReset = false;

    public LegacyFluidConduitData() {
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public LegacyFluidConduitData(boolean shouldReset, Fluid fluid) {
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
    public LegacyFluidConduitData withClientChanges(LegacyFluidConduitData guiData) {
        this.shouldReset = guiData.shouldReset;

        // TODO: Soon we will swap to records which will mean this will be a new instance.
        //       This API has been designed with this pending change in mind.
        return this;
    }

    @Override
    public LegacyFluidConduitData deepCopy() {
        return new LegacyFluidConduitData(shouldReset, lockedFluid);
    }

    @Override
    public ConduitDataType<LegacyFluidConduitData> type() {
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
