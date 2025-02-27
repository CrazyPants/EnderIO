package com.enderio.base.common.item.tool;

import com.enderio.base.common.config.BaseConfig;
import com.enderio.base.common.handler.TravelHandler;
import com.enderio.base.common.init.EIODataComponents;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.client.item.AdvancedTooltipProvider;
import com.enderio.core.client.item.EnergyBarDecorator;
import com.enderio.core.common.energy.ItemStackEnergy;
import com.enderio.core.common.item.CreativeTabVariants;
import com.enderio.core.common.util.TooltipUtil;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class TravelStaffItem extends Item implements AdvancedTooltipProvider, CreativeTabVariants {

    public static ICapabilityProvider<ItemStack, Void, IEnergyStorage> ENERGY_STORAGE_PROVIDER = (stack,
            v) -> new ComponentEnergyStorage(stack, EIODataComponents.ENERGY.get(), TravelStaffItem.getMaxEnergy());

    public TravelStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (getActivationStatus(stack).isAir()) {
            if (tryPerformAction(level, player, stack)) {
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (getActivationStatus(context.getItemInHand()).isBlock()) {
            if (context.getPlayer() != null
                    && tryPerformAction(context.getLevel(), context.getPlayer(), context.getItemInHand())) {
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
            }

            return InteractionResult.FAIL;
        }

        return super.useOn(context);
    }

    private boolean tryPerformAction(Level level, Player player, ItemStack stack) {
        boolean isCreative = player.isCreative();
        if (hasResources(stack) || isCreative) {
            if (performAction(level, player, stack)) {
                if (!level.isClientSide() && !isCreative) {
                    consumeResources(stack);
                }

                return true;
            }

            return false;
        }

        return false;
    }

    /**
     * Perform your action
     * @return true if it was a success and you want to consume the resources
     */
    public boolean performAction(Level level, Player player, ItemStack stack) {
        if (player.isShiftKeyDown()) {
            if (TravelHandler.shortTeleport(level, player)) {
                player.getCooldowns().addCooldown(this, BaseConfig.COMMON.ITEMS.TRAVELLING_BLINK_DISABLED_TIME.get());
                return true;
            }
        } else {
            if (TravelHandler.blockTeleport(level, player)) {
                player.getCooldowns().addCooldown(this, BaseConfig.COMMON.ITEMS.TRAVELLING_BLINK_DISABLED_TIME.get());
                return true;
            }
        }
        return false;
    }

    public static int getMaxEnergy() {
        return BaseConfig.COMMON.ITEMS.TRAVELLING_STAFF_MAX_ENERGY.get();
    }

    public boolean hasResources(ItemStack stack) {
        return ItemStackEnergy.hasEnergy(stack, BaseConfig.COMMON.ITEMS.TRAVELLING_STAFF_ENERGY_USE.get());
    }

    public void consumeResources(ItemStack stack) {
        ItemStackEnergy.extractEnergy(stack, BaseConfig.COMMON.ITEMS.TRAVELLING_STAFF_ENERGY_USE.get(), false);
    }

    protected ActivationStatus getActivationStatus(ItemStack stack) {
        return ActivationStatus.ALL;
    }

    @Override
    public void addAllVariants(CreativeModeTab.Output modifier) {
        modifier.accept(this);
        ItemStack is = new ItemStack(this);
        ItemStackEnergy.setFull(is);
        modifier.accept(is);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var energyStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage != null) {
            return Math.round(energyStorage.getEnergyStored() * 13.0F / energyStorage.getMaxEnergyStored());
        }

        return 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return EnergyBarDecorator.BAR_COLOR;
    }

    @Override
    public void addCommonTooltips(ItemStack itemStack, @org.jetbrains.annotations.Nullable Player player,
            List<Component> tooltips) {
        String energy = String.format("%,d", ItemStackEnergy.getEnergyStored(itemStack)) + "/"
                + String.format("%,d", ItemStackEnergy.getMaxEnergyStored(itemStack));
        tooltips.add(TooltipUtil.styledWithArgs(EIOLang.ENERGY_AMOUNT, energy));
    }

    protected enum ActivationStatus {
        BLOCK(true, false), AIR(false, true), ALL(true, true);

        private final boolean isBlock;
        private final boolean isAir;

        ActivationStatus(boolean isBlock, boolean isAir) {
            this.isBlock = isBlock;
            this.isAir = isAir;
        }

        public boolean isBlock() {
            return isBlock;
        }

        public boolean isAir() {
            return isAir;
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }

}
