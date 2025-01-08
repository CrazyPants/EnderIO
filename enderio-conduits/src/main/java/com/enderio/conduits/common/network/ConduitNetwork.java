package com.enderio.conduits.common.network;

import com.enderio.conduits.EnderIOConduits;
import com.enderio.conduits.common.network.connections.C2SSetConduitChannelPacket;
import com.enderio.conduits.common.network.connections.C2SSetConduitRedstoneChannelPacket;
import com.enderio.conduits.common.network.connections.C2SSetConduitRedstoneControlPacket;
import com.enderio.core.EnderCore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = EnderIOConduits.MODULE_MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ConduitNetwork {
    private static final String PROTOCOL_VERSION = "1.0";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event
            .registrar(EnderCore.MOD_ID)
            .versioned(PROTOCOL_VERSION);

        registrar.playToServer(C2SSetConduitConnectionState.TYPE, C2SSetConduitConnectionState.STREAM_CODEC,
            ConduitServerPayloadHandler.getInstance()::handleConduitConnectionState);

        registrar.playToServer(C2SSetConduitExtendedData.TYPE, C2SSetConduitExtendedData.STREAM_CODEC,
            ConduitServerPayloadHandler.getInstance()::handleConduitExtendedData);

        registrar.playToServer(ConduitMenuSelectionPacket.TYPE, ConduitMenuSelectionPacket.STREAM_CODEC,
            ConduitServerPayloadHandler.getInstance()::handleConduitMenuSelection);

        registrar.playToServer(DoubleChannelPacket.TYPE, DoubleChannelPacket.STREAM_CODEC,
            ConduitServerPayloadHandler.getInstance()::handleDoubleChannelFilter);

        registrar.playToServer(TimerFilterPacket.TYPE, TimerFilterPacket.STREAM_CODEC,
            ConduitServerPayloadHandler.getInstance()::handleTimerFilter);

        registrar.playToServer(CountFilterPacket.TYPE, CountFilterPacket.STREAM_CODEC,
            ConduitServerPayloadHandler.getInstance()::handleCountFilter);

        registrar.playToServer(C2SSetConduitChannelPacket.TYPE, C2SSetConduitChannelPacket.STREAM_CODEC,
            ConduitServerPayloadHandler.getInstance()::handleSetConduitChannelPacket);

        registrar.playToServer(C2SSetConduitRedstoneControlPacket.TYPE, C2SSetConduitRedstoneControlPacket.STREAM_CODEC,
            ConduitServerPayloadHandler.getInstance()::handleSetConduitRedstoneControlPacket);

        registrar.playToServer(C2SSetConduitRedstoneChannelPacket.TYPE, C2SSetConduitRedstoneChannelPacket.STREAM_CODEC,
            ConduitServerPayloadHandler.getInstance()::handleSetConduitRedstoneChannelPacket);
    }

}
