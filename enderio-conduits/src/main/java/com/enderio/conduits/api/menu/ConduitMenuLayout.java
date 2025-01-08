package com.enderio.conduits.api.menu;

import net.minecraft.network.chat.Component;

public sealed interface ConduitMenuLayout permits ConduitMenuLayout.SingleColumn, ConduitMenuLayout.TwoColumns {
    record SingleColumn(Component title, Runnable toggled) implements ConduitMenuLayout {}

    record TwoColumns(Component leftTitle, Runnable toggleLeft, Component rightTitle, Runnable toggleRight) implements
        ConduitMenuLayout {}
}
