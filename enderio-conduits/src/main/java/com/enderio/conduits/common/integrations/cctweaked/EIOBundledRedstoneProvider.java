package com.enderio.conduits.common.integrations.cctweaked;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.common.conduit.block.ConduitBundleBlockEntity;
import com.enderio.conduits.common.conduit.connection.ConnectionState;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitData;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.conduits.common.init.Conduits;
import dan200.computercraft.api.redstone.BundledRedstoneProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EIOBundledRedstoneProvider implements BundledRedstoneProvider {

    public static EIOBundledRedstoneProvider INSTANCE = new EIOBundledRedstoneProvider();

    @Override
    public int getBundledRedstoneOutput(Level world, BlockPos pos, Direction side) {
        BlockEntity be = world.getBlockEntity(pos);

        Holder<Conduit<?, ?>> redstoneConduit = world.holderOrThrow(Conduits.REDSTONE);

//        if (be instanceof ConduitBundleBlockEntity conduit) {
//            ConnectionState connectionState = conduit.getBundle().getConnectionState(side, redstoneConduit);
//            if (connectionState instanceof DynamicConnectionState dyn && dyn.isInsert()) {
//                RedstoneConduitData data = conduit.getBundle().getNodeFor(redstoneConduit).getData(ConduitTypes.Data.REDSTONE.get());
//                if (data == null) {
//                    return -1;
//                }
//
//                int out = 0;
//
//                for (DyeColor color : DyeColor.values()) {
//                    out |= (data.isActive(color) ? 1 : 0) << color.getId();
//                }
//                return out;
//            }
//        }
        return -1;
    }
}
