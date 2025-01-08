package com.enderio.modconduits.mods.mekanism;

import com.enderio.conduits.api.ColoredRedstoneProvider;
import com.enderio.conduits.api.network.ConduitNetwork;
import com.enderio.conduits.api.ticker.CapabilityAwareConduitTicker;
import mekanism.api.heat.IHeatHandler;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.List;

public class HeatTicker extends CapabilityAwareConduitTicker<HeatConduit, IHeatHandler> {

    public HeatTicker() {
    }

    @Override
    protected void tickCapabilityGraph(ServerLevel level, HeatConduit type, List<CapabilityConnection> inserts, List<CapabilityConnection> extracts,
        ConduitNetwork graph, ColoredRedstoneProvider coloredRedstoneProvider) {

        for (var extract : extracts) {
            IHeatHandler extractHandler = extract.capability();

            for (var insert : inserts) {
                IHeatHandler insertHandler = insert.capability();

                double heatCapacity = extractHandler.getTotalHeatCapacity();
                double invConduction = insertHandler.getTotalInverseConduction() + extractHandler.getTotalInverseConduction();
                double tempToTransfer = (extractHandler.getTotalTemperature() - insertHandler.getTotalTemperature()) / invConduction; //TODO subtract ambient? - HeatAPI.getAmbientTemp(level, )

                double heatToTransfer = tempToTransfer * heatCapacity;
                if (heatToTransfer > 0) {
                    extractHandler.handleHeat(-heatToTransfer);
                    insertHandler.handleHeat(heatToTransfer);
                }
            }
        }
    }

    @Override
    protected BlockCapability<IHeatHandler, Direction> getCapability() {
        return MekanismModule.Capabilities.HEAT;
    }
}
