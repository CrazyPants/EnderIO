package com.enderio.conduits.common.conduit.type.fluid;

import com.enderio.base.api.filter.FluidStackFilter;
import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;

import java.util.List;

import com.enderio.conduits.api.ticker.IOAwareConduitTicker;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class FluidConduitTicker extends IOAwareConduitTicker<FluidConduit, FluidConduitConnectionConfig, FluidConduitTicker.Connection> {

    private int getScaledFluidRate(FluidConduit conduit) {
        // Adjust for tick rate. Always flow up so we are at minimum meeting the
        // required rate.
        return (int) Math.ceil(conduit.transferRatePerTick() * (20.0 / conduit.graphTickRate()));
    }

    private int doFluidTransfer(FluidStack fluid, Connection receiver, List<Connection> senders) {
        FluidStack extractedFluid = receiver.fluidHandler().drain(fluid, IFluidHandler.FluidAction.SIMULATE);

        if (extractedFluid.isEmpty()) {
            return fluid.getAmount();
        }

        if (receiver.extractFilter() instanceof FluidStackFilter fluidStackFilter) {
            if (!fluidStackFilter.test(extractedFluid)) {
                return fluid.getAmount();
            }
        }

        for (Connection insert : senders) {
            if (insert.insertFilter() instanceof FluidStackFilter fluidStackFilter) {
                if (!fluidStackFilter.test(extractedFluid)) {
                    continue;
                }
            }

            FluidStack transferredFluid = FluidUtil.tryFluidTransfer(insert.fluidHandler(), receiver.fluidHandler(),
                    fluid, true);

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
    public void tickGraph(ServerLevel level, FluidConduit conduit, ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider) {
        super.tickGraph(level, conduit, graph, coloredRedstoneProvider);

        // Update if the network is now locked
        var context = graph.getOrCreateContext(FluidConduitNetworkContext.TYPE);
        if (!context.lockedFluid().equals(context.lastLockedFluid())) {
            context.clearLastLockedFluid();
            for (var node : graph.getNodes()) {
                if (node.isLoaded()) {
                    node.markDirty();
                }
            }
        }
    }

    @Override
    protected void tickColoredGraph(ServerLevel level, FluidConduit conduit, List<Connection> senders,
            List<Connection> receivers, DyeColor color, ConduitNetwork graph,
            ColoredRedstoneProvider coloredRedstoneProvider) {

        final int fluidRate = getScaledFluidRate(conduit);
        var context = graph.getOrCreateContext(FluidConduitNetworkContext.TYPE);

        for (Connection receiver : receivers) {
            IFluidHandler extractHandler = receiver.fluidHandler();

            if (!context.lockedFluid().isSame(Fluids.EMPTY)) {
                doFluidTransfer(new FluidStack(context.lockedFluid(), fluidRate), receiver, senders);
            } else {
                int remaining = fluidRate;

                for (int i = 0; i < extractHandler.getTanks() && remaining > 0; i++) {
                    if (extractHandler.getFluidInTank(i).isEmpty()) {
                        continue;
                    }

                    Fluid fluid = extractHandler.getFluidInTank(i).getFluid();
                    remaining = doFluidTransfer(new FluidStack(fluid, remaining), receiver, senders);

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
    protected @Nullable FluidConduitTicker.Connection createConnection(Level level, ConduitNode node, Direction side) {
        IFluidHandler fluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, node.getPos().relative(side),
                side.getOpposite());
        if (fluidHandler != null) {
            return new Connection(node, side, node.getConnectionConfig(side, FluidConduitConnectionConfig.TYPE), fluidHandler);
        }
        return null;
    }

    protected static class Connection extends SimpleConnection<FluidConduitConnectionConfig> {
        private final IFluidHandler fluidHandler;

        public Connection(ConduitNode node, Direction side, FluidConduitConnectionConfig config, IFluidHandler fluidHandler) {
            super(node, side, config);
            this.fluidHandler = fluidHandler;
        }

        public IFluidHandler fluidHandler() {
            return fluidHandler;
        }
    }
}
