package com.enderio.conduits.api.bundle;

import com.enderio.conduits.api.ConduitMenuData;

public enum SlotType {
    FILTER_EXTRACT,
    FILTER_INSERT,
    UPGRADE_EXTRACT;

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

    public boolean isAvailableFor(ConduitMenuData data) {
        return switch (this) {
            case FILTER_INSERT -> data.hasFilterInsert();
            case FILTER_EXTRACT -> data.hasFilterExtract();
            case UPGRADE_EXTRACT -> data.hasUpgrade();
        };
    }
}
