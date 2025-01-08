package com.enderio.conduits.common.conduit.facades;

import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.api.ConduitCapabilities;
import com.enderio.conduits.common.init.ConduitBlocks;
import com.enderio.conduits.common.init.ConduitLang;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ConduitFacadeItem extends BlockItem {
    public ConduitFacadeItem(Properties properties) {
        super(ConduitBlocks.CONDUIT.get(), properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        @Nullable
        Player player = context.getPlayer();
        BlockPos blockpos = context.getClickedPos();

        // Allow placing from the edge of an adjacent block
        BlockState blockState = level.getBlockState(blockpos);
        if (!blockState.isAir()) {
            // noinspection DataFlowIssue
            return blockState
                .useItemOn(context.getItemInHand(), level, player, context.getHand(),
                    context.getHitResult().withPosition(blockpos))
                .result();
        }

        return super.place(context);
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        // Must have a valid facade
        var facade = context.getItemInHand().getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER);
        if (facade == null || !facade.isValid()) {
            return false;
        }

        return super.canPlace(context, state);
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        // Do not register
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        var facade = stack.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER);
        boolean hasFacadeTooltip = facade != null
                && (facade.type().isBlastResistant() || facade.type().doesHideConduits());

        if (hasFacadeTooltip) {
            if (tooltipFlag.hasShiftDown()) {
                if (facade.type().doesHideConduits()) {
                    tooltipComponents.add(ConduitLang.TRANSPARENT_FACADE_TOOLTIP);
                }

                if (facade.type().isBlastResistant()) {
                    tooltipComponents.add(ConduitLang.BLAST_RESIST_FACADE_TOOLTIP);
                }
            } else {
                tooltipComponents.add(EIOLang.SHOW_DETAIL_TOOLTIP);
            }
        }
    }
}
