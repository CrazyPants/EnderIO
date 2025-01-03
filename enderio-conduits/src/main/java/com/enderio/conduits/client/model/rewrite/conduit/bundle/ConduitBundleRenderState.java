package com.enderio.conduits.client.model.rewrite.conduit.bundle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.api.connection.ConduitConnection;
import com.enderio.conduits.api.connection.ConduitConnectionType;
import com.enderio.conduits.common.conduit.OffsetHelper;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class ConduitBundleRenderState {
    public static final ModelProperty<ConduitBundleRenderState> PROPERTY = new ModelProperty<>();

    private Direction.Axis mainAxis;

    private List<Holder<Conduit<?>>> conduits;

    // TODO: Due to the API design, we need to capture the nodes at the moment.
    // it could be changed to accept ConduitDataAccessor instead, then we can clone
    // the data storage.
    private Map<Holder<Conduit<?>>, ConduitGraphObject> conduitNodes;

    private Map<Direction, List<Holder<Conduit<?>>>> conduitsByDirection;

    private Map<Direction, Map<Holder<Conduit<?>>, ConduitConnection>> conduitConnections;

    private boolean hasFacade;
    private BlockState facadeBlockstate;
    private boolean doesFacadeHideConduits;

    public static ConduitBundleRenderState of(ConduitBundleReader bundle) {
        var renderState = new ConduitBundleRenderState();

        renderState.mainAxis = OffsetHelper.findMainAxis(bundle);
        renderState.conduits = List.copyOf(bundle.getConduits());
        renderState.conduitNodes = bundle.getConduits()
                .stream()
                .collect(HashMap::new, (m, c) -> m.put(c, bundle.getConduitNode(c).deepCopy()), Map::putAll);
        renderState.conduitsByDirection = new HashMap<>();
        for (var side : Direction.values()) {
            renderState.conduitsByDirection.put(side, bundle.getConnectedConduits(side));
        }

        renderState.conduitConnections = new HashMap<>();
        for (var side : Direction.values()) {
            HashMap<Holder<Conduit<?>>, ConduitConnection> conduits = new HashMap<>();
            for (var conduit : renderState.conduits) {
                if (bundle.getConnectionType(side, conduit) == ConduitConnectionType.CONNECTED_BLOCK) {
                    conduits.put(conduit, bundle.getConnection(side, conduit));
                }
            }

            renderState.conduitConnections.put(side, conduits);
        }

        renderState.hasFacade = bundle.hasFacade();
        if (renderState.hasFacade) {
            renderState.facadeBlockstate = bundle.getFacadeBlock().defaultBlockState();
            renderState.doesFacadeHideConduits = bundle.getFacadeType().doesHideConduits();
        } else {
            renderState.facadeBlockstate = Blocks.AIR.defaultBlockState();
            renderState.doesFacadeHideConduits = false;
        }

        return renderState;
    }

    public List<Holder<Conduit<?>>> conduits() {
        return conduits;
    }

    public ConduitGraphObject getNode(Holder<Conduit<?>> conduit) {
        return conduitNodes.get(conduit);
    }

    public List<Holder<Conduit<?>>> getConnectedConduits(Direction side) {
        return conduitsByDirection.getOrDefault(side, List.of());
    }

    public boolean isConnectionEndpoint(Direction side) {
        return !conduitConnections.get(side).isEmpty();
    }

    public ConduitConnection getConnectionState(Direction side, Holder<Conduit<?>> conduit) {
        return conduitConnections.get(side).get(conduit);
    }

    public Direction.Axis mainAxis() {
        return mainAxis;
    }

    public ResourceLocation getTexture(Holder<Conduit<?>> conduit) {
        var node = getNode(conduit);
        return conduit.value().getTexture(node);
    }

    public boolean hasFacade() {
        return hasFacade;
    }

    public BlockState facade() {
        return facadeBlockstate;
    }

    public boolean doesFacadeHideConduits() {
        return doesFacadeHideConduits;
    }

}
