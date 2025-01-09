package com.enderio.conduits.common.network;

import com.enderio.conduits.common.conduit.menu.NewConduitMenu;
import com.enderio.conduits.common.network.connections.SetConduitConnectionConfigPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConduitClientPayloadHandler {
    private static final ConduitClientPayloadHandler INSTANCE = new ConduitClientPayloadHandler();

    public static ConduitClientPayloadHandler getInstance() {
        return INSTANCE;
    }
}
