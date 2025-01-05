package com.enderio.conduits.common.conduit.type.fluid;

import com.enderio.base.api.filter.FluidStackFilter;
import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitMenuData;
import com.enderio.conduits.api.ConduitNode;
import com.enderio.conduits.api.ConduitType;
import com.enderio.conduits.api.SlotType;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.core.common.util.TooltipUtil;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public record FluidConduit(ResourceLocation texture, Component description, int transferRatePerTick,
        boolean isMultiFluid) implements Conduit<FluidConduit> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final MapCodec<FluidConduit> CODEC = RecordCodecBuilder
            .mapCodec(
                    builder -> builder
                            .group(ResourceLocation.CODEC.fieldOf("texture").forGetter(FluidConduit::texture),
                                    ComponentSerialization.CODEC.fieldOf("description")
                                            .forGetter(FluidConduit::description),
                                    Codec.INT.fieldOf("transfer_rate").forGetter(FluidConduit::transferRatePerTick),
                                    Codec.BOOL.fieldOf("is_multi_fluid").forGetter(FluidConduit::isMultiFluid))
                            .apply(builder, FluidConduit::new));

    public static final ConduitMenuData NORMAL_MENU_DATA = new ConduitMenuData.Simple(true, true, true, false, false,
            true);
    public static final ConduitMenuData ADVANCED_MENU_DATA = new ConduitMenuData.Simple(true, true, true, true, true,
            true);
    private static final FluidConduitTicker TICKER = new FluidConduitTicker();

    @Override
    public ConduitType<FluidConduit> type() {
        return ConduitTypes.FLUID.get();
    }

    @Override
    public FluidConduitTicker getTicker() {
        return TICKER;
    }

    @Override
    public ConduitMenuData getMenuData() {
        return isMultiFluid ? ADVANCED_MENU_DATA : NORMAL_MENU_DATA;
    }

    @Override
    public boolean canBeInSameBundle(Holder<Conduit<?>> otherConduit) {
        return !(otherConduit.value() instanceof FluidConduit);
    }

    @Override
    public boolean canBeReplacedBy(Holder<Conduit<?>> otherConduit) {
        if (!(otherConduit.value() instanceof FluidConduit otherFluidConduit)) {
            return false;
        }

        return compareTo(otherFluidConduit) < 0;
    }

    @Override
    public boolean canConnectTo(ConduitNode selfNode, ConduitNode otherNode) {
        FluidConduitData selfData = selfNode.getOrCreateData(ConduitTypes.Data.FLUID.get());
        FluidConduitData otherData = otherNode.getOrCreateData(ConduitTypes.Data.FLUID.get());

        return selfData.lockedFluid() == null || otherData.lockedFluid() == null
                || selfData.lockedFluid() == otherData.lockedFluid();
    }

    @Override
    public void onConnectTo(ConduitNode selfNode, ConduitNode otherNode) {
        FluidConduitData selfData = selfNode.getOrCreateData(ConduitTypes.Data.FLUID.get());
        FluidConduitData otherData = otherNode.getOrCreateData(ConduitTypes.Data.FLUID.get());

        if (selfData.lockedFluid() != null) {
            if (otherData.lockedFluid() != null && selfData.lockedFluid() != otherData.lockedFluid()) {
                LOGGER.warn("incompatible fluid conduits merged");
            }

            otherData.setLockedFluid(selfData.lockedFluid());
        } else if (otherData.lockedFluid() != null) {
            selfData.setLockedFluid(otherData.lockedFluid());
        }
    }

    @Override
    public boolean canApplyFilter(SlotType slotType, ResourceFilter resourceFilter) {
        return resourceFilter instanceof FluidStackFilter;
    }

    @Override
    public void addToTooltip(Item.TooltipContext pContext, Consumer<Component> pTooltipAdder,
            TooltipFlag pTooltipFlag) {
        String transferLimitFormatted = String.format("%,d", transferRatePerTick());
        pTooltipAdder
                .accept(TooltipUtil.styledWithArgs(ConduitLang.FLUID_EFFECTIVE_RATE_TOOLTIP, transferLimitFormatted));

        if (isMultiFluid()) {
            pTooltipAdder.accept(ConduitLang.MULTI_FLUID_TOOLTIP);
        }

        if (pTooltipFlag.hasShiftDown()) {
            String rawRateFormatted = String.format("%,d",
                    (int) Math.ceil(transferRatePerTick() * (20.0 / graphTickRate())));
            pTooltipAdder.accept(TooltipUtil.styledWithArgs(ConduitLang.FLUID_RAW_RATE_TOOLTIP, rawRateFormatted));
        }
    }

    @Override
    public boolean hasAdvancedTooltip() {
        return true;
    }

    @Override
    public boolean showDebugTooltip() {
        return true;
    }

    @Override
    public int compareTo(@NotNull FluidConduit o) {
        if (isMultiFluid() && !o.isMultiFluid()) {
            return 1;
        }

        if (transferRatePerTick() < o.transferRatePerTick()) {
            return -1;
        } else if (transferRatePerTick() > o.transferRatePerTick()) {
            return 1;
        }

        return 0;
    }
}
