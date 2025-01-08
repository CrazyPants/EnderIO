package com.enderio.modconduits.mods.appeng;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import com.enderio.base.api.network.DumbStreamCodec;
import com.enderio.conduits.api.network.node.legacy.ConduitData;
import com.enderio.conduits.api.network.node.legacy.ConduitDataType;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public class ConduitInWorldGridNodeHost implements IInWorldGridNodeHost, ConduitData<ConduitInWorldGridNodeHost> {

    public static final MapCodec<ConduitInWorldGridNodeHost> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            CompoundTag.CODEC.fieldOf("main_node").forGetter(ConduitInWorldGridNodeHost::saveMainNode)
        ).apply(instance, ConduitInWorldGridNodeHost::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ConduitInWorldGridNodeHost> STREAM_CODEC = DumbStreamCodec.of(ConduitInWorldGridNodeHost::new).cast();

    @Nullable
    private IManagedGridNode mainNode = null;

    @Nullable
    private CompoundTag savedMainNode = null;

    private boolean isDense;

    public ConduitInWorldGridNodeHost() {
    }

    public ConduitInWorldGridNodeHost(CompoundTag mainNodeTag) {
        savedMainNode = mainNodeTag;
    }

    @Override
    public ConduitInWorldGridNodeHost deepCopy() {
        return this;
    }

    @Override
    public ConduitDataType<ConduitInWorldGridNodeHost> type() {
        return AE2ConduitsModule.DATA.get();
    }

    @Nullable
    public IManagedGridNode getMainNode() {
        return mainNode;
    }

    public void setMainNode(IManagedGridNode mainNode, boolean isDense) {
        this.mainNode = mainNode;
        this.isDense = isDense;
    }

    public void clearMainNode() {
        this.mainNode = null;
        this.isDense = false;
    }

    protected CompoundTag saveMainNode() {
        var tag = new CompoundTag();
        if (mainNode != null) {
            mainNode.saveToNBT(tag);
        }
        return tag;
    }

    public void loadMainNode() {
        if (mainNode == null) {
            throw new IllegalStateException("mainNode cannot be null.");
        }

        if (savedMainNode == null) {
            return;
        }

        this.mainNode.loadFromNBT(savedMainNode);
        savedMainNode = null;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(Direction dir) {
        return mainNode != null ? mainNode.getNode() : null;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        if (isDense) {
            return AECableType.DENSE_SMART;
        }

        return AECableType.SMART;
    }
}
