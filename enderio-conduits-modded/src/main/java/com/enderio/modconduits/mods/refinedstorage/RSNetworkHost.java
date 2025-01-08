package com.enderio.modconduits.mods.refinedstorage;

import com.enderio.base.api.network.DumbStreamCodec;
import com.enderio.conduits.api.network.node.legacy.ConduitData;
import com.enderio.conduits.api.network.node.legacy.ConduitDataType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.common.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeContainerProviderImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RSNetworkHost extends NetworkNodeContainerProviderImpl implements ConduitData<RSNetworkHost> {
    public static final MapCodec<RSNetworkHost> CODEC = RecordCodecBuilder.mapCodec(rsNetworkHostInstance ->
        rsNetworkHostInstance.group(Codec.INT.fieldOf("int").forGetter(i -> i.i)
        ).apply(rsNetworkHostInstance, RSNetworkHost::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RSNetworkHost> STREAM_CODEC = DumbStreamCodec.of(RSNetworkHost::new).cast();

    private int i = 0;
    public ConduitRSNode mainNode;

    public RSNetworkHost() {
        super();
    }

    public RSNetworkHost(int i) {
        super();
    }

    @Override
    public RSNetworkHost deepCopy() {
        return new RSNetworkHost();
    }

    @Override
    public ConduitDataType<RSNetworkHost> type() {
        return RefinedStorageModule.DATA.get();
    }

    public static class ConduitRSNode implements InWorldNetworkNodeContainer {

        private final BlockState blockState;
        private final GlobalPos globalPos;
        private final NetworkNode node;
        private boolean removed;

        public ConduitRSNode(Level level, BlockPos pos) {
            this.blockState = level.getBlockState(pos);
            this.globalPos = GlobalPos.of(level.dimension(), pos);
            this.node = new GridNetworkNode(0);
            this.removed = false;
        }

        @Override
        public BlockState getBlockState() {
            return this.blockState;
        }

        @Override
        public boolean isRemoved() {
            return removed;
        }

        public void setRemoved(boolean removed) {
            this.removed = removed;
        }

        @Override
        public GlobalPos getPosition() {
            return this.globalPos;
        }

        @Override
        public BlockPos getLocalPosition() {
            return this.globalPos.pos();
        }

        @Override
        public String getName() {
            return "RS Conduit";
        }

        @Override
        public NetworkNode getNode() {
            return this.node;
        }

        @Override
        public void addOutgoingConnections(ConnectionSink connectionSink) {
            for (Direction direction : Direction.values()) {
                connectionSink.tryConnectInSameDimension(this.globalPos.pos().relative(direction), direction.getOpposite());
            }
        }

        @Override
        public boolean canAcceptIncomingConnection(Direction direction, BlockState blockState) {
            return true;
        }
    }

}
