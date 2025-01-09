package com.enderio.conduits.common.network;

import com.enderio.conduits.common.conduit.menu.NewConduitMenu;
import com.enderio.conduits.common.network.connections.SetConduitConnectionConfigPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConduitCommonPayloadHandler {
    private static final ConduitCommonPayloadHandler INSTANCE = new ConduitCommonPayloadHandler();

    public void handle(SetConduitConnectionConfigPacket packet, IPayloadContext context) {
        // TODO: This is the same handler for client & server, maybe we need a common payload handler?
        context.enqueueWork(() -> {
            if (packet.containerId() == context.player().containerMenu.containerId) {
                if (context.player().containerMenu instanceof NewConduitMenu menu) {
                    menu.setConnectionConfig(packet.connectionConfig());
                }
            }
        });
    }

    public static ConduitCommonPayloadHandler getInstance() {
        return INSTANCE;
    }
}
