package com.enderio.conduits.common.network;

public class ConduitClientPayloadHandler {
    private static final ConduitClientPayloadHandler INSTANCE = new ConduitClientPayloadHandler();

    public static ConduitClientPayloadHandler getInstance() {
        return INSTANCE;
    }
}
