package com.enderio.conduits.api.bundle;

public enum SlotType {
    FILTER_EXTRACT, FILTER_INSERT, UPGRADE_EXTRACT;

    public static final int Y_POSITION = 71;

    public int getX() {
        return switch (this) {
        case FILTER_EXTRACT -> 113;
        case FILTER_INSERT -> 23;
        case UPGRADE_EXTRACT -> 131;
        };
    }

    public int getY() {
        return Y_POSITION;
    }
}
