package com.enderio.conduits.api.screen;

import com.enderio.conduits.api.network.node.legacy.ConduitDataAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import org.joml.Vector2i;

import java.util.List;
import java.util.function.Supplier;

/**
 * Extend the conduit screen with additional widgets.
 */
public interface ConduitScreenExtension {

    @FunctionalInterface
    interface UpdateDispatcher {
        void sendUpdate();
    }

    // TODO: Update Javadocs.
    /**
     * @param conduitDataAccessor       the conduit data the widgets are for, manipulate the state of it in the widgets
     * @param updateDispatcher                    call this to modify the conduit data.
     * @param direction                         the supplier to get the current direction for this extendedconduitdata
     * @param widgetsStart                      the position on which widgets start
     * @return Widgets that manipulate the extended ConduitData, these changes are synced back to the server
     */
    List<AbstractWidget> createWidgets(Screen screen, ConduitDataAccessor conduitDataAccessor, UpdateDispatcher updateDispatcher,
        Supplier<Direction> direction, Vector2i widgetsStart);
}
