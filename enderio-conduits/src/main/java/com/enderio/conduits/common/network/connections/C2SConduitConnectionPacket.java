package com.enderio.conduits.common.network.connections;

import com.enderio.conduits.api.Conduit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;

public interface C2SConduitConnectionPacket {
    BlockPos pos();
    Direction side();
    Holder<Conduit<?, ?>> conduit();
}
