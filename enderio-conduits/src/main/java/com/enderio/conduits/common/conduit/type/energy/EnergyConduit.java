package com.enderio.conduits.common.conduit.type.energy;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitMenuData;
import com.enderio.conduits.api.ConduitNode;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.connection.ConduitConnection;
import com.enderio.conduits.api.connection.ConduitConnectionMode;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.core.common.util.TooltipUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record EnergyConduit(ResourceLocation texture, Component description, int transferRatePerTick)
        implements Conduit<EnergyConduit> {

    public static final MapCodec<EnergyConduit> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
            .group(ResourceLocation.CODEC.fieldOf("texture").forGetter(Conduit::texture),
                    ComponentSerialization.CODEC.fieldOf("description").forGetter(Conduit::description),
                    Codec.INT.fieldOf("transfer_rate").forGetter(EnergyConduit::transferRatePerTick))
            .apply(builder, EnergyConduit::of));

    public static EnergyConduit of(ResourceLocation texture, Component description, int transferRate) {
        return new EnergyConduit(texture, description, transferRate);
    }

    private static final EnergyConduitTicker TICKER = new EnergyConduitTicker();
    private static final ConduitMenuData MENU_DATA = new ConduitMenuData.Simple(false, false, false, false, false,
            true);

    // Not configurable - energy is instantaneous
    @Override
    public int graphTickRate() {
        return 1;
    }

    @Override
    public ConduitType<EnergyConduit> type() {
        return ConduitTypes.ENERGY.get();
    }

    @Override
    public EnergyConduitTicker getTicker() {
        return TICKER;
    }

    @Override
    public ConduitMenuData getMenuData() {
        return MENU_DATA;
    }

    @Override
    public boolean canBeInSameBundle(Holder<Conduit<?>> otherConduit) {
        return !(otherConduit.value() instanceof EnergyConduit);
    }

    @Override
    public boolean canBeReplacedBy(Holder<Conduit<?>> otherConduit) {
        if (!(otherConduit.value() instanceof EnergyConduit otherEnergyConduit)) {
            return false;
        }

        return compareTo(otherEnergyConduit) < 0;
    }

    @Override
    public <TCap, TContext> @Nullable TCap proxyCapability(BlockCapability<TCap, TContext> capability, ConduitNode node,
            Level level, BlockPos pos, @Nullable TContext context) {

        if (Capabilities.EnergyStorage.BLOCK == capability && (context == null || context instanceof Direction)) {
            if (context != null) {
                var state = node.getIOState((Direction) context);
                if (state.isPresent() && !state.get().isExtract()) {
                    return null;
                }
            }

            // noinspection unchecked
            return (TCap) new EnergyConduitStorage(transferRatePerTick(), node);
        }

        return null;
    }

    @Override
    public void onRemoved(ConduitNode node, Level level, BlockPos pos) {
        level.invalidateCapabilities(pos);
    }

    @Override
    public ConduitConnection getDefaultConnection(Level level, BlockPos pos, Direction side) {
        IEnergyStorage capability = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(side),
                side.getOpposite());
        if (capability != null) {
            ConduitConnectionMode mode;

            if (capability.canReceive() && capability.canExtract()) {
                mode = ConduitConnectionMode.BOTH;
            } else if (capability.canReceive()) {
                mode = ConduitConnectionMode.IN;
            } else {
                // This ensures that if there's an energy capability that might be pushing but
                // won't allow pulling is present, we can still interact
                // For example Thermal's Dynamos report false until they have energy in them and
                // flux networks always refuse.
                mode = ConduitConnectionMode.OUT;
            }

            // TODO: Old EnderIO was red, to not break EIO 7 we'll stick to green.
            return new ConduitConnection(mode, DyeColor.GREEN, DyeColor.GREEN);
        }

        return Conduit.super.getDefaultConnection(level, pos, side);
    }

    @Override
    public void addToTooltip(Item.TooltipContext pContext, Consumer<Component> pTooltipAdder,
            TooltipFlag pTooltipFlag) {
        String transferLimitFormatted = String.format("%,d", transferRatePerTick());
        pTooltipAdder.accept(TooltipUtil.styledWithArgs(ConduitLang.ENERGY_RATE_TOOLTIP, transferLimitFormatted));
    }

    @Override
    public int compareTo(@NotNull EnergyConduit o) {
        if (transferRatePerTick() < o.transferRatePerTick()) {
            return -1;
        } else if (transferRatePerTick() > o.transferRatePerTick()) {
            return 1;
        }

        return 0;
    }
}
