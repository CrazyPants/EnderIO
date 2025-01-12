package com.enderio.conduits.common.network;

import com.enderio.conduits.common.conduit.menu.ConduitMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConduitClientPayloadHandler {
    private static final ConduitClientPayloadHandler INSTANCE = new ConduitClientPayloadHandler();

    public void handle(S2CConduitExtraGuiDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.containerId() == context.player().containerMenu.containerId) {
                if (context.player().containerMenu instanceof ConduitMenu menu) {
                    menu.setExtraGuiData(packet.extraGuiData());
                }
            }
        });
    }

    public static ConduitClientPayloadHandler getInstance() {
        return INSTANCE;
    }
}
