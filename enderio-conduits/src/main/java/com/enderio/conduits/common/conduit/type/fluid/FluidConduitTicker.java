package com.enderio.conduits.common.conduit.type.fluid;

import com.enderio.base.api.filter.FluidStackFilter;
import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.ticker.CapabilityAwareConduitTicker;
import com.enderio.conduits.common.init.ConduitTypes;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

public class FluidConduitTicker extends CapabilityAwareConduitTicker<FluidConduit, IFluidHandler> {

    private int getScaledFluidRate(FluidConduit conduit, CapabilityConnection extractingConnection) {
        // Adjust for tick rate. Always flow up so we are at minimum meeting the required rate.
        int rate = (int)Math.ceil(conduit.transferRatePerTick() * (20.0 / conduit.graphTickRate()));
        return rate;
    }

    private int doFluidTransfer(FluidStack fluid, CapabilityConnection extract, List<CapabilityConnection> inserts) {
        FluidStack extractedFluid = extract.capability().drain(fluid, IFluidHandler.FluidAction.SIMULATE);

        if (extractedFluid.isEmpty()) {
            return fluid.getAmount();
        }

        if (extract.extractFilter() instanceof FluidStackFilter fluidStackFilter) {
            if (!fluidStackFilter.test(extractedFluid)) {
                return fluid.getAmount();
            }
        }

        for (CapabilityConnection insert : inserts) {
            if (insert.insertFilter() instanceof FluidStackFilter fluidStackFilter) {
                if (!fluidStackFilter.test(extractedFluid)) {
                    continue;
                }
            }

            FluidStack transferredFluid = FluidUtil.tryFluidTransfer(insert.capability(), extract.capability(), fluid, true);

            if (!transferredFluid.isEmpty()) {
                fluid.shrink(transferredFluid.getAmount());
            }

            if (fluid.getAmount() <= 0) {
                break;
            }
        }

        return fluid.getAmount();
    }

    @Override
    public void tickGraph(
        ServerLevel level,
        FluidConduit conduit,
        List<ConduitNode> loadedNodes,
        ConduitNetwork graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {

//        boolean shouldReset = false;
//        for (var loadedNode : loadedNodes) {
//            FluidConduitData fluidExtendedData = loadedNode.getOrCreateData(ConduitTypes.Data.FLUID.get());
//            if (fluidExtendedData.shouldReset()) {
//                shouldReset = true;
//                fluidExtendedData.setShouldReset(false);
//            }
//        }
//
//        if (shouldReset) {
//            for (var loadedNode : loadedNodes) {
//                FluidConduitData fluidExtendedData = loadedNode.getOrCreateData(ConduitTypes.Data.FLUID.get());
//                fluidExtendedData.setLockedFluid(Fluids.EMPTY);
//            }
//        }
        super.tickGraph(level, conduit, loadedNodes, graph, coloredRedstoneProvider);
    }

    @Override
    protected void tickCapabilityGraph(
        ServerLevel level,
        FluidConduit conduit,
        List<CapabilityConnection> inserts,
        List<CapabilityConnection> extracts,
        ConduitNetwork graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {

        var context = graph.getOrCreateContext(FluidConduitNetworkContext.TYPE);

        for (CapabilityConnection extract : extracts) {
            IFluidHandler extractHandler = extract.capability();

            final int fluidRate = getScaledFluidRate(conduit, extract);

            if (!context.lockedFluid().isSame(Fluids.EMPTY)) {
                doFluidTransfer(new FluidStack(context.lockedFluid(), fluidRate), extract, inserts);
            } else {
                int remaining = fluidRate;

                for (int i = 0; i < extractHandler.getTanks() && remaining > 0; i++) {
                    if (extractHandler.getFluidInTank(i).isEmpty()) {
                        continue;
                    }

                    Fluid fluid = extractHandler.getFluidInTank(i).getFluid();
                    remaining = doFluidTransfer(new FluidStack(fluid, remaining), extract, inserts);

                    if (!conduit.isMultiFluid() && remaining < fluidRate) {
                        if (fluid instanceof FlowingFluid flowing) {
                            fluid = flowing.getSource();
                        }

                        context.setLockedFluid(fluid);

                        break;
                    }
                }
            }
        }
    }

    @Override
    protected BlockCapability<IFluidHandler, Direction> getCapability() {
        return Capabilities.FluidHandler.BLOCK;
    }
}
