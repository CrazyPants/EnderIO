package com.enderio.conduits.common.network;

import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.conduits.api.bundle.ConduitBundleAccessor;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.io.ChanneledIOConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlockEntity;
import com.enderio.conduits.common.menu.ConduitMenu;
import com.enderio.conduits.common.network.connections.C2SConduitConnectionPacket;
import com.enderio.conduits.common.network.connections.C2SSetConduitChannelPacket;
import com.enderio.conduits.common.network.connections.C2SSetConduitRedstoneChannelPacket;
import com.enderio.conduits.common.network.connections.C2SSetConduitRedstoneControlPacket;
import com.enderio.conduits.common.redstone.DoubleRedstoneChannel;
import com.enderio.conduits.common.redstone.RedstoneCountFilter;
import com.enderio.conduits.common.redstone.RedstoneTimerFilter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.util.TriConsumer;

public class ConduitServerPayloadHandler {
    private static final ConduitServerPayloadHandler INSTANCE = new ConduitServerPayloadHandler();

    public static ConduitServerPayloadHandler getInstance() {
        return INSTANCE;
    }

    public void handleConduitConnectionState(final C2SSetConduitConnectionState packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            BlockEntity be = level.getBlockEntity(packet.pos());
            if (be instanceof ConduitBundleBlockEntity conduitBundleBlockEntity) {
                conduitBundleBlockEntity.handleConnectionStateUpdate(packet.direction(), packet.conduit(), packet.connectionState());
            }
        });
    }

    public void handleConduitExtendedData(final C2SSetConduitExtendedData packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            BlockEntity be = level.getBlockEntity(packet.pos());
            if (be instanceof ConduitBundleBlockEntity conduitBundleBlockEntity) {
                conduitBundleBlockEntity.handleConduitDataUpdate(packet.conduit(), packet.conduitDataContainer());
            }
        });
    }

    public void handleConduitMenuSelection(final ConduitMenuSelectionPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof ConduitMenu menu) {
                menu.setConduit(packet.conduit());
            }
        });
    }

    public void handleDoubleChannelFilter(DoubleChannelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack mainHandItem = context.player().getMainHandItem();
            var channels = mainHandItem.getCapability(EIOCapabilities.Filter.ITEM);
            if (channels instanceof DoubleRedstoneChannel doubleRedstoneChannel) {
                doubleRedstoneChannel.setChannels(packet.channel1(), packet.channel2());
            }
        });
    }

    public void handleTimerFilter(TimerFilterPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack mainHandItem = context.player().getMainHandItem();
            var channels = mainHandItem.getCapability(EIOCapabilities.Filter.ITEM);
            if (channels instanceof RedstoneTimerFilter timer) {
                timer.setTimer(packet.ticks(), packet.maxTicks());
            }
        });
    }

    public void handleCountFilter(CountFilterPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack mainHandItem = context.player().getMainHandItem();
            var channels = mainHandItem.getCapability(EIOCapabilities.Filter.ITEM);
            if (channels instanceof RedstoneCountFilter count) {
                count.setState(packet);
            }
        });
    }

    public void handleSetConduitChannelPacket(C2SSetConduitChannelPacket packet, IPayloadContext context) {
        handleConduitConfigPacket(packet, context, (p, conduitBundle, currentConfig) -> {
            if (currentConfig instanceof ChanneledIOConnectionConfig channelledConnectionConfig) {
                if (packet.channelSide() == C2SSetConduitChannelPacket.Side.INPUT) {
                    conduitBundle.setConnectionConfig(p.side(), p.conduit(),
                        channelledConnectionConfig.withInputChannel(p.channelColor()));
                } else {
                    conduitBundle.setConnectionConfig(p.side(), p.conduit(),
                        channelledConnectionConfig.withOutputChannel(p.channelColor()));
                }
            }
        });
    }

    public void handleSetConduitRedstoneControlPacket(C2SSetConduitRedstoneControlPacket packet, IPayloadContext context) {
        handleConduitConfigPacket(packet, context, (p, conduitBundle, currentConfig) -> {
            if (currentConfig instanceof RedstoneControlledConnection redstoneControlledConnection) {
                conduitBundle.setConnectionConfig(p.side(), p.conduit(),
                    redstoneControlledConnection.withRedstoneControl(p.redstoneControl()));
            }
        });
    }

    public void handleSetConduitRedstoneChannelPacket(C2SSetConduitRedstoneChannelPacket packet, IPayloadContext context) {
        handleConduitConfigPacket(packet, context, (p, conduitBundle, currentConfig) -> {
            if (currentConfig instanceof RedstoneControlledConnection redstoneControlledConnection) {
                conduitBundle.setConnectionConfig(p.side(), p.conduit(),
                    redstoneControlledConnection.withRedstoneChannel(p.redstoneChannel()));
            }
        });
    }

    private <T extends C2SConduitConnectionPacket> void handleConduitConfigPacket(T packet, IPayloadContext context, TriConsumer<T, ConduitBundleAccessor, ConnectionConfig> packetConsumer) {
        context.enqueueWork(() -> {
            var level = context.player().level();
            BlockEntity be = level.getBlockEntity(packet.pos());
            if (be instanceof ConduitBundleAccessor conduitBundle) {
                var currentConfig = conduitBundle.getConnectionConfig(packet.side(), packet.conduit());
                packetConsumer.accept(packet, conduitBundle, currentConfig);
            }
        });
    }
}
