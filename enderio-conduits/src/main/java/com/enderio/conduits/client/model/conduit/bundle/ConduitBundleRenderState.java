package com.enderio.conduits.client.model.conduit.bundle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.api.connection.ConnectionStatus;
import com.enderio.conduits.common.conduit.OffsetHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.liliandev.ensure.ensures.EnsureSide;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

public class ConduitBundleRenderState {
    public static final ModelProperty<ConduitBundleRenderState> PROPERTY = new ModelProperty<>();

    private Direction.Axis mainAxis;
    private List<Holder<Conduit<?, ?>>> conduits;
    private Map<Holder<Conduit<?, ?>>, CompoundTag> extraWorldData;
    private Map<Direction, List<Holder<Conduit<?, ?>>>> conduitsByDirection;
    private Map<Direction, Map<Holder<Conduit<?, ?>>, ConduitConnectionRenderState>> conduitConnections;

    private boolean hasFacade;
    private BlockState facadeBlockstate;
    private boolean doesFacadeHideConduits;

    @EnsureSide(EnsureSide.Side.CLIENT)
    public static ConduitBundleRenderState of(ConduitBundleReader bundle) {
        var renderState = new ConduitBundleRenderState();

        renderState.mainAxis = OffsetHelper.findMainAxis(bundle);
        renderState.conduits = List.copyOf(bundle.getConduits());

        renderState.extraWorldData = new HashMap<>();
        for (var conduit : renderState.conduits) {
            var tag = bundle.getConduitExtraWorldData(conduit);
            if (tag != null) {
                renderState.extraWorldData.put(conduit, tag.copy());
            }
        }

        renderState.conduitsByDirection = new HashMap<>();
        for (var side : Direction.values()) {
            renderState.conduitsByDirection.put(side, bundle.getConnectedConduits(side));
        }

        renderState.conduitConnections = new HashMap<>();
        for (var side : Direction.values()) {
            HashMap<Holder<Conduit<?, ?>>, ConduitConnectionRenderState> conduits = new HashMap<>();
            for (var conduit : renderState.conduits) {
                if (bundle.getConnectionStatus(side, conduit) == ConnectionStatus.CONNECTED_BLOCK) {
                    var connectionConfig = bundle.getConnectionConfig(side, conduit);
                    var connectionRenderState = ConduitConnectionRenderState.of(conduit, connectionConfig);
                    conduits.put(conduit, connectionRenderState);
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

    public List<Holder<Conduit<?, ?>>> conduits() {
        return conduits;
    }

    @Nullable
    public CompoundTag getExtraWorldData(Holder<Conduit<?, ?>> conduit) {
        return extraWorldData.get(conduit);
    }

    public List<Holder<Conduit<?, ?>>> getConnectedConduits(Direction side) {
        return conduitsByDirection.getOrDefault(side, List.of());
    }

    public boolean isConnectionEndpoint(Direction side) {
        return !conduitConnections.get(side).isEmpty();
    }

    public ConduitConnectionRenderState getConnectionState(Direction side, Holder<Conduit<?, ?>> conduit) {
        return conduitConnections.get(side).get(conduit);
    }

    public Direction.Axis mainAxis() {
        return mainAxis;
    }

    public ResourceLocation getTexture(Holder<Conduit<?, ?>> conduit) {
        return conduit.value().getTexture(getExtraWorldData(conduit));
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
