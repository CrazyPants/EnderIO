package com.enderio.conduits.common.init;

import com.enderio.base.api.EnderIO;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import com.enderio.conduits.api.network.ConduitNetworkContextType;
import com.enderio.conduits.api.network.node.NodeDataType;
import com.enderio.conduits.api.network.node.legacy.ConduitDataType;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.common.conduit.type.energy.EnergyConduit;
import com.enderio.conduits.common.conduit.type.energy.EnergyConduitConnectionConfig;
import com.enderio.conduits.common.conduit.type.energy.EnergyConduitNetworkContext;
import com.enderio.conduits.common.conduit.type.fluid.FluidConduit;
import com.enderio.conduits.common.conduit.type.fluid.FluidConduitData;
import com.enderio.conduits.common.conduit.type.fluid.FluidConduitNetworkContext;
import com.enderio.conduits.common.conduit.type.item.ItemConduit;
import com.enderio.conduits.common.conduit.type.item.ItemConduitConnectionConfig;
import com.enderio.conduits.common.conduit.type.item.ItemConduitData;
import com.enderio.conduits.common.conduit.type.item.ItemConduitNodeData;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduit;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitData;
import java.util.function.Supplier;

import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitNetworkContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ConduitTypes {
    private static final DeferredRegister<ConduitType<?>> CONDUIT_TYPES = DeferredRegister
            .create(EnderIOConduitsRegistries.CONDUIT_TYPE, EnderIO.NAMESPACE);

    public static final Supplier<ConduitType<EnergyConduit>> ENERGY = CONDUIT_TYPES.register("energy",
            () -> ConduitType.builder(EnergyConduit.CODEC).exposeCapability(Capabilities.EnergyStorage.BLOCK).build());

    public static final Supplier<ConduitType<RedstoneConduit>> REDSTONE = CONDUIT_TYPES.register("redstone",
            () -> ConduitType.of(RedstoneConduit.CODEC));

    public static final Supplier<ConduitType<FluidConduit>> FLUID = CONDUIT_TYPES.register("fluid",
            () -> ConduitType.of(FluidConduit.CODEC));

    public static final Supplier<ConduitType<ItemConduit>> ITEM = CONDUIT_TYPES.register("item",
            () -> ConduitType.of(ItemConduit.CODEC));

    public static class Data {
        private static final DeferredRegister<ConduitDataType<?>> CONDUIT_DATA_TYPES = DeferredRegister
                .create(EnderIOConduitsRegistries.CONDUIT_DATA_TYPE, EnderIO.NAMESPACE);

        public static final Supplier<ConduitDataType<ItemConduitData>> ITEM = CONDUIT_DATA_TYPES.register("item",
                () -> new ConduitDataType<>(ItemConduitData.CODEC, ItemConduitData.STREAM_CODEC, ItemConduitData::new));

        public static final Supplier<ConduitDataType<FluidConduitData>> FLUID = CONDUIT_DATA_TYPES.register("fluid",
                () -> new ConduitDataType<>(FluidConduitData.CODEC, FluidConduitData.STREAM_CODEC,
                        FluidConduitData::new));

        public static final Supplier<ConduitDataType<RedstoneConduitData>> REDSTONE = CONDUIT_DATA_TYPES
                .register("redstone", () -> new ConduitDataType<>(RedstoneConduitData.CODEC,
                        RedstoneConduitData.STREAM_CODEC, RedstoneConduitData::new));
    }

    public static class ConnectionTypes {
        private static final DeferredRegister<ConnectionConfigType<?>> CONNECTION_TYPES = DeferredRegister
                .create(EnderIOConduitsRegistries.CONDUIT_CONNECTION_CONFIG_TYPE, EnderIO.NAMESPACE);

        public static final Supplier<ConnectionConfigType<ItemConduitConnectionConfig>> ITEM = CONNECTION_TYPES.register("item",
                () -> ItemConduitConnectionConfig.TYPE);

        public static final Supplier<ConnectionConfigType<EnergyConduitConnectionConfig>> ENERGY = CONNECTION_TYPES.register("energy",
                () -> EnergyConduitConnectionConfig.TYPE);
    }

    public static class NodeData {
        private static final DeferredRegister<NodeDataType<?>> NODE_DATA_TYPES = DeferredRegister
                .create(EnderIOConduitsRegistries.CONDUIT_NODE_DATA_TYPE, EnderIO.NAMESPACE);

        public static final Supplier<NodeDataType<ItemConduitNodeData>> ITEM = NODE_DATA_TYPES
                .register("item", () -> ItemConduitNodeData.TYPE);
    }

    public static class ContextTypes {
        public static final DeferredRegister<ConduitNetworkContextType<?>> CONDUIT_NETWORK_CONTEXT_TYPES = DeferredRegister
            .create(EnderIOConduitsRegistries.CONDUIT_NETWORK_CONTEXT_TYPE, EnderIO.NAMESPACE);

        public static final Supplier<ConduitNetworkContextType<EnergyConduitNetworkContext>> ENERGY = CONDUIT_NETWORK_CONTEXT_TYPES
            .register("energy", () -> EnergyConduitNetworkContext.TYPE);

        public static final Supplier<ConduitNetworkContextType<RedstoneConduitNetworkContext>> REDSTONE = CONDUIT_NETWORK_CONTEXT_TYPES
            .register("redstone", () -> RedstoneConduitNetworkContext.TYPE);

        public static final Supplier<ConduitNetworkContextType<FluidConduitNetworkContext>> FLUID = CONDUIT_NETWORK_CONTEXT_TYPES
            .register("fluid", () -> FluidConduitNetworkContext.TYPE);
    }

    public static void register(IEventBus bus) {
        CONDUIT_TYPES.register(bus);
        Data.CONDUIT_DATA_TYPES.register(bus);
        ConnectionTypes.CONNECTION_TYPES.register(bus);
        NodeData.NODE_DATA_TYPES.register(bus);
        ContextTypes.CONDUIT_NETWORK_CONTEXT_TYPES.register(bus);
    }
}
