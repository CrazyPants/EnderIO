package com.enderio.conduits.api.menu;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.ConnectionConfigType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ConduitMenuType<TConnectionConfig extends ConnectionConfig> {

    private final ConnectionConfigType<TConnectionConfig> connectionConfigType;
    private final ConduitMenuLayout layout;
    private final List<ConduitMenuComponent<TConnectionConfig>> components;

    private ConduitMenuType(ConnectionConfigType<TConnectionConfig> connectionConfigType, ConduitMenuLayout layout, List<ConduitMenuComponent<TConnectionConfig>> components) {
        this.connectionConfigType = connectionConfigType;
        this.layout = layout;
        this.components = List.copyOf(components);
    }

    public ConnectionConfigType<TConnectionConfig> connectionType() {
        return connectionConfigType;
    }

    public ConduitMenuLayout layout() {
        return layout;
    }

    public List<ConduitMenuComponent<TConnectionConfig>> components() {
        return components;
    }

    public static <TConduit extends Conduit<TConduit, TConnectionConfig>, TConnectionConfig extends ConnectionConfig> Builder<TConduit, TConnectionConfig> builder(ConnectionConfigType<TConnectionConfig> connectionConfigType) {
        return new Builder<>(connectionConfigType);
    }

    public static class Builder<TConduit extends Conduit<TConduit, TConnectionConfig>, TConnectionConfig extends ConnectionConfig> {
        private final ConnectionConfigType<TConnectionConfig> connectionConfigType;

        @Nullable
        private ConduitMenuLayout layout;

        private List<ConduitMenuComponent<TConnectionConfig>> components = new ArrayList<>();

        private Builder(ConnectionConfigType<TConnectionConfig> connectionConfigType) {
            this.connectionConfigType = connectionConfigType;
        }

        public Builder<TConduit, TConnectionConfig> layout(ConduitMenuLayout layout) {
            this.layout = layout;
            return this;
        }

        public Builder<TConduit, TConnectionConfig> addComponent(ConduitMenuComponent<TConnectionConfig> component) {
            this.components.add(component);
            return this;
        }

        public ConduitMenuType<TConnectionConfig> build() {
            if (layout == null) {
                throw new IllegalStateException("Layout must be set");
            }

            return new ConduitMenuType<>(connectionConfigType, layout, components);
        }
    }

}
