package com.enderio.modconduits.mods.mekanism;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.network.node.ConduitNode;
import com.enderio.conduits.api.ticker.CapabilityAwareConduitTicker;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.List;

public class ChemicalTicker extends CapabilityAwareConduitTicker<ChemicalConduit, IChemicalHandler> {

    private int getScaledTransferRate(ChemicalConduit conduit, CapabilityConnection extractingConnection) {
        // Adjust for tick rate. Always flow up so we are at minimum meeting the required rate.
        return  (int)Math.ceil(conduit.transferRatePerTick() * (20.0 / conduit.graphTickRate()));
    }

    @Override
    public void tickGraph(ServerLevel level, ChemicalConduit conduit, List<ConduitNode> loadedNodes, ConduitNetwork graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {
        boolean shouldReset = false;
        for (var loadedNode : loadedNodes) {
            ChemicalConduitData fluidExtendedData = loadedNode.getOrCreateData(MekanismModule.CHEMICAL_DATA_TYPE.get());
            if (fluidExtendedData.shouldReset()) {
                shouldReset = true;
                fluidExtendedData.setShouldReset(false);
            }
        }

        if (shouldReset) {
            for (var loadedNode : loadedNodes) {
                ChemicalConduitData fluidExtendedData = loadedNode.getOrCreateData(MekanismModule.CHEMICAL_DATA_TYPE.get());
                fluidExtendedData.setLockedChemical(ChemicalStack.EMPTY);
            }
        }
        super.tickGraph(level, conduit, loadedNodes, graph, coloredRedstoneProvider);
    }

    @Override
    protected void tickCapabilityGraph(ServerLevel level,
        ChemicalConduit conduit,
        List<CapabilityConnection> inserts,
        List<CapabilityConnection> extracts,
        ConduitNetwork graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {

        for (var extract : extracts) {
            tickExtractCapability(conduit, extract, inserts);
        }
    }

    private void tickExtractCapability(ChemicalConduit conduit, CapabilityConnection extract, List<CapabilityConnection> insertCaps) {
        final int transferRate = getScaledTransferRate(conduit, extract);

        IChemicalHandler extractHandler = extract.capability();
        ConduitNode node = extract.node();

        ChemicalConduitData data = node.getOrCreateData(MekanismModule.CHEMICAL_DATA_TYPE.get());

        Chemical extractType = extractHandler.extractChemical(Long.MAX_VALUE, Action.SIMULATE).getChemical();
        ChemicalStack result;

        if (!data.lockedChemical().isEmpty()) {
            if (data.lockedChemical().getChemical() != extractType) {
                return;
            }

            result = extractHandler.extractChemical(data.lockedChemical().getChemical().getStack(transferRate), Action.SIMULATE);
        } else {
            result = extractHandler.extractChemical(transferRate, Action.SIMULATE);
        }

        if (result.isEmpty()) {
            return;
        }

        if (extract.extractFilter() instanceof ChemicalFilter filter) {
            if (!filter.test(result)) {
                return;
            }
        }

        long transferred = 0;
        for (var insert : insertCaps) {
            if (insert.insertFilter() instanceof ChemicalFilter filter) {
                if (!filter.test(result)) {
                    continue;
                }
            }
            IChemicalHandler destinationHandler = insert.capability();
            ChemicalStack transferredChemical;
            if (!data.lockedChemical().isEmpty()) {
                transferredChemical = tryChemicalTransfer(destinationHandler, extractHandler, data.lockedChemical().getChemical().getStack(transferRate - transferred), true);
            } else {
                transferredChemical = tryChemicalTransfer(destinationHandler, extractHandler, transferRate - transferred, true);
            }

            transferred += transferredChemical.getAmount();
            if (!conduit.isMultiChemical() && data.lockedChemical().isEmpty()) {
                data.setLockedChemical(extractType.getStack(1));
            }
            if (transferred >= transferRate) {
                break;
            }
        }
    }

    private static ChemicalStack tryChemicalTransfer(IChemicalHandler chemicalDestination, IChemicalHandler chemicalSource, long maxAmount, boolean doTransfer) {
        var drainable = chemicalSource.extractChemical(maxAmount, Action.SIMULATE);
        if (!drainable.isEmpty()) {
            return tryChemicalTransfer_Internal(chemicalDestination, chemicalSource, drainable, doTransfer);
        }

        return ChemicalStack.EMPTY;
    }

    private static ChemicalStack tryChemicalTransfer(IChemicalHandler chemicalDestination, IChemicalHandler chemicalSource, ChemicalStack resource, boolean doTransfer) {
        var drainable = chemicalSource.extractChemical(resource, Action.SIMULATE);
        if (!drainable.isEmpty() && resource.equals(drainable)) {
            return tryChemicalTransfer_Internal(chemicalDestination, chemicalSource, drainable, doTransfer);
        }

        return ChemicalStack.EMPTY;
    }

    private static ChemicalStack tryChemicalTransfer_Internal(IChemicalHandler chemicalDestination, IChemicalHandler chemicalSource, ChemicalStack drainable, boolean doTransfer) {
        long fillableAmount = drainable.getAmount() - chemicalDestination.insertChemical(drainable, Action.SIMULATE).getAmount();
        if (fillableAmount > 0) {
            drainable.setAmount(fillableAmount);
            if (doTransfer) {
                var drained = chemicalSource.extractChemical(drainable, Action.EXECUTE);
                if (!drained.isEmpty()) {
                    drained.setAmount(chemicalDestination.insertChemical(drained, Action.EXECUTE).getAmount());
                    return drained;
                }
            } else {
                return drainable;
            }
        }
        return ChemicalStack.EMPTY;
    }

    @Override
    protected BlockCapability<IChemicalHandler, Direction> getCapability() {
        return MekanismModule.Capabilities.CHEMICAL;
    }
}
