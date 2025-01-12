package com.enderio.conduits.common.conduit.graph;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.bundle.ConduitInventory;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

// TODO: Did the interface for now but honestly maybe this should just be an object because its not API public.
public interface ConduitConnectionHost {
    Holder<Conduit<?, ?>> conduit();

    BlockPos pos();

    boolean isConnectedTo(Direction side);

    ConnectionConfig getConnectionConfig(Direction side);

    void setConnectionConfig(Direction side, ConnectionConfig connectionConfig);

    ConduitInventory inventory();

    void onNodeDirty();

    boolean isLoaded();

    boolean hasRedstoneSignal(@Nullable DyeColor signalColor);
}
