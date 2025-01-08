package com.enderio.conduits.common.init;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.facade.FacadeType;
import com.enderio.conduits.common.redstone.DoubleRedstoneChannel;
import com.enderio.conduits.common.redstone.RedstoneCountFilter;
import com.enderio.conduits.common.redstone.RedstoneTLatchFilter;
import com.enderio.conduits.common.redstone.RedstoneTimerFilter;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ConduitComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister
            .createDataComponents(EnderIO.NAMESPACE);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<Conduit<?, ?>>>> CONDUIT = DATA_COMPONENT_TYPES
            .registerComponentType("conduit",
                    builder -> builder.persistent(Conduit.CODEC).networkSynchronized(Conduit.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FacadeType>> FACADE_TYPE = DATA_COMPONENT_TYPES
            .registerComponentType("facade_type",
                    builder -> builder.persistent(FacadeType.CODEC).networkSynchronized(FacadeType.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> EXTRACTION_SPEED_UPGRADE_TIER = DATA_COMPONENT_TYPES
            .registerComponentType("extraction_speed_upgrade_tier",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DoubleRedstoneChannel.Component>> REDSTONE_AND_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_and_filter",
                    builder -> builder.persistent(DoubleRedstoneChannel.Component.CODEC)
                            .networkSynchronized(DoubleRedstoneChannel.Component.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RedstoneCountFilter.Component>> REDSTONE_COUNT_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_count_filter",
                    builder -> builder.persistent(RedstoneCountFilter.Component.CODEC)
                            .networkSynchronized(RedstoneCountFilter.Component.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DoubleRedstoneChannel.Component>> REDSTONE_NAND_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_nand_filter",
                    builder -> builder.persistent(DoubleRedstoneChannel.Component.CODEC)
                            .networkSynchronized(DoubleRedstoneChannel.Component.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DoubleRedstoneChannel.Component>> REDSTONE_NOR_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_nor_filter",
                    builder -> builder.persistent(DoubleRedstoneChannel.Component.CODEC)
                            .networkSynchronized(DoubleRedstoneChannel.Component.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> REDSTONE_NOT_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_not_filter", builder -> builder.persistent(Codec.unit(Unit.INSTANCE))
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DoubleRedstoneChannel.Component>> REDSTONE_OR_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_or_filter",
                    builder -> builder.persistent(DoubleRedstoneChannel.Component.CODEC)
                            .networkSynchronized(DoubleRedstoneChannel.Component.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> REDSTONE_SENSOR_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_sensor_filter", builder -> builder.persistent(Codec.unit(Unit.INSTANCE))
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RedstoneTimerFilter.Component>> REDSTONE_TIMER_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_timer_filter",
                    builder -> builder.persistent(RedstoneTimerFilter.Component.CODEC)
                            .networkSynchronized(RedstoneTimerFilter.Component.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RedstoneTLatchFilter.Component>> REDSTONE_TLATCH_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_tlatch_filter",
                    builder -> builder.persistent(RedstoneTLatchFilter.Component.CODEC)
                            .networkSynchronized(RedstoneTLatchFilter.Component.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DoubleRedstoneChannel.Component>> REDSTONE_XNOR_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_xnor_filter",
                    builder -> builder.persistent(DoubleRedstoneChannel.Component.CODEC)
                            .networkSynchronized(DoubleRedstoneChannel.Component.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DoubleRedstoneChannel.Component>> REDSTONE_XOR_FILTER = DATA_COMPONENT_TYPES
            .registerComponentType("redstone_xor_filter",
                    builder -> builder.persistent(DoubleRedstoneChannel.Component.CODEC)
                            .networkSynchronized(DoubleRedstoneChannel.Component.STREAM_CODEC));

    public static void register(IEventBus bus) {
        DATA_COMPONENT_TYPES.register(bus);
    }
}
