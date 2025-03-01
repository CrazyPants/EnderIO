package com.enderio.machines.common.blockentity.sync;

import com.enderio.core.common.network.NetworkDataSlot;
import com.enderio.machines.common.io.energy.IMachineEnergyStorage;
import com.enderio.machines.common.io.energy.ImmutableMachineEnergyStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@Deprecated(forRemoval = true, since = "7.1")
public record EnergySyncData(int energyStored, int maxEnergyStored, int maxEnergyUse, int energyReceived, int energyUsed) {

    public static Codec<EnergySyncData> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Codec.INT.fieldOf("EnergyStored").forGetter(EnergySyncData::energyStored),
                    Codec.INT.fieldOf("MaxEnergyStored").forGetter(EnergySyncData::maxEnergyStored),
                    Codec.INT.fieldOf("MaxEnergyUse").forGetter(EnergySyncData::maxEnergyUse),
            Codec.INT.fieldOf("EnergyReceived").forGetter(EnergySyncData::energyReceived),
                Codec.INT.fieldOf("EnergyUsed").forGetter(EnergySyncData::energyUsed))
            .apply(instance, EnergySyncData::new));

    public static StreamCodec<ByteBuf, EnergySyncData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT,
            EnergySyncData::energyStored, ByteBufCodecs.INT, EnergySyncData::maxEnergyStored, ByteBufCodecs.INT,
            EnergySyncData::maxEnergyUse, ByteBufCodecs.INT,
        EnergySyncData::energyReceived,ByteBufCodecs.INT,
        EnergySyncData::energyUsed, EnergySyncData::new);

    public static NetworkDataSlot.CodecType<EnergySyncData> DATA_SLOT_TYPE = new NetworkDataSlot.CodecType<>(CODEC,
            STREAM_CODEC.cast());

    public static EnergySyncData from(IMachineEnergyStorage storage) {
        return new EnergySyncData(storage.getEnergyStored(), storage.getMaxEnergyStored(), storage.getMaxEnergyUse(), storage.getEnergyReceived(),
            storage.getEnergyUsed());
    }

    public ImmutableMachineEnergyStorage toImmutableStorage() {
        return new ImmutableMachineEnergyStorage(energyStored, maxEnergyStored, maxEnergyUse, energyReceived,energyUsed );
    }
}
