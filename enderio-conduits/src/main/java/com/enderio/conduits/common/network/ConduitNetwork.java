package com.enderio.conduits.common.network;

import com.enderio.conduits.EnderIOConduits;
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
        final PayloadRegistrar registrar = event.registrar(EnderCore.MOD_ID).versioned(PROTOCOL_VERSION);

        registrar.playToServer(DoubleChannelPacket.TYPE, DoubleChannelPacket.STREAM_CODEC,
                ConduitServerPayloadHandler.getInstance()::handleDoubleChannelFilter);

        registrar.playToServer(TimerFilterPacket.TYPE, TimerFilterPacket.STREAM_CODEC,
                ConduitServerPayloadHandler.getInstance()::handleTimerFilter);

        registrar.playToServer(CountFilterPacket.TYPE, CountFilterPacket.STREAM_CODEC,
                ConduitServerPayloadHandler.getInstance()::handleCountFilter);

        registrar.playToServer(ConduitMenuSelectionPacket.TYPE, ConduitMenuSelectionPacket.STREAM_CODEC,
                ConduitServerPayloadHandler.getInstance()::handleConduitMenuSelection);

        registrar.playToClient(S2CConduitExtraGuiDataPacket.TYPE, S2CConduitExtraGuiDataPacket.STREAM_CODEC,
                ConduitClientPayloadHandler.getInstance()::handle);

        registrar.playToClient(S2CConduitListPacket.TYPE, S2CConduitListPacket.STREAM_CODEC,
                ConduitClientPayloadHandler.getInstance()::handle);

        registrar.playBidirectional(SetConduitConnectionConfigPacket.TYPE,
                SetConduitConnectionConfigPacket.STREAM_CODEC, ConduitCommonPayloadHandler.getInstance()::handle);
    }

}
