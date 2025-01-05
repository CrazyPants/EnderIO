package com.enderio.conduits.common.conduit;

import com.enderio.base.common.init.EIOCreativeTabs;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.EnderIOConduits;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import com.enderio.conduits.common.init.ConduitBlocks;
import com.enderio.conduits.common.init.ConduitComponents;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.core.common.util.TooltipUtil;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = EnderIOConduits.MODULE_MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ConduitBlockItem extends BlockItem {

    public ConduitBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack getStackFor(Holder<Conduit<?>> conduit, int count) {
        var stack = new ItemStack(ConduitBlocks.CONDUIT.asItem(), count);
        stack.set(ConduitComponents.CONDUIT, conduit);
        return stack;
    }

    @Override
    public Component getName(ItemStack pStack) {
        Holder<Conduit<?>> conduit = pStack.get(ConduitComponents.CONDUIT);
        if (conduit == null) {
            return super.getName(pStack);
        }

        return conduit.value().description();
    }

    // Do not use block description, as that is Conduit Bundle.
    // This will ensure missing conduits use the missing conduit string.
    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        @Nullable
        Player player = context.getPlayer();
        BlockPos blockpos = context.getClickedPos();

        // Allow placing from the edge of an adjacent block
        BlockState blockState = level.getBlockState(blockpos);
        if (!blockState.canBeReplaced()) {
            // noinspection DataFlowIssue
            return blockState
                    .useItemOn(context.getItemInHand(), level, player, context.getHand(),
                            context.getHitResult().withPosition(blockpos))
                    .result();
        }

        return super.place(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        Holder<Conduit<?>> conduit = stack.get(ConduitComponents.CONDUIT);
        if (conduit != null) {
            conduit.value().addToTooltip(context, tooltipComponents::add, tooltipFlag);

            boolean showDetailTooltip = !tooltipFlag.hasShiftDown()
                    && (conduit.value().hasAdvancedTooltip() || conduit.value().showDebugTooltip());

            if (conduit.value().showDebugTooltip() && tooltipFlag.hasShiftDown()) {
                tooltipComponents.add(TooltipUtil.styledWithArgs(ConduitLang.GRAPH_TICK_RATE_TOOLTIP,
                        20 / conduit.value().graphTickRate()));
            }

            if (showDetailTooltip) {
                tooltipComponents.add(EIOLang.SHOW_DETAIL_TOOLTIP);
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // High priority so conduits appear at the top of the conduits tab.
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == EIOCreativeTabs.CONDUITS_TAB.get()) {
            var registry = event.getParameters().holders().lookupOrThrow(EnderIOConduitsRegistries.Keys.CONDUIT);
            var conduitTypes = registry.listElements().toList();

            var conduitClassTypes = conduitTypes.stream()
                    .map(e -> e.value().getClass())
                    .sorted(Comparator.comparing(Class::getName))
                    .distinct()
                    .toList();

            for (var conduitClass : conduitClassTypes) {
                var matchingConduitTypes = conduitTypes.stream()
                        .filter(e -> e.value().getClass() == conduitClass)
                        // GRIM...
                        .sorted((o1, o2) -> compareConduitTo(o1.value(), o2.value()))
                        .toList();

                for (var conduitType : matchingConduitTypes) {
                    event.accept(getStackFor(conduitType, 1), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
        }
    }

    private static <T extends Conduit<T>> int compareConduitTo(Conduit<T> o1, Conduit<?> o2) {
        return o1.compareTo((T) o2);
    }
}
