package com.enderio.conduits.common.conduit.bundle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitCapabilities;
import com.enderio.conduits.client.model.conduit.facades.FacadeHelper;
import com.enderio.conduits.client.particle.ConduitBreakParticle;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.conduit.RightClickAction;
import com.enderio.conduits.common.init.ConduitBlockEntities;
import com.enderio.conduits.common.init.ConduitComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NewConduitBundleBlock extends Block implements EntityBlock {

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public NewConduitBundleBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(WATERLOGGED, false));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ConduitBlockEntities.CONDUIT.create(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
        BlockEntityType<T> blockEntityType) {
        //return createTickerHelper(blockEntityType, typeSupplier.get(), EIOBlockEntity::tick);
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof NewConduitBundleBlockEntity conduitBundleBlockEntity) {
                if (level.isClientSide) {
                    conduitBundleBlockEntity.clientTick();
                } else {
                    conduitBundleBlockEntity.serverTick();
                }
                conduitBundleBlockEntity.endTick();
            }
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getBundleShape(level, pos, true);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getBundleShape(level, pos, false);
    }

    private VoxelShape getBundleShape(BlockGetter level, BlockPos pos, boolean canHideFacade) {
        if (level.getBlockEntity(pos) instanceof NewConduitBundleBlockEntity conduit) {
            if (conduit.hasFacade() && (!canHideFacade || FacadeHelper.areFacadesVisible())) {
                return Shapes.block();
            }

            // Ensure if a bundle is bugged with 0 conduits that it can be broken.
            if (!conduit.getConduits().isEmpty()) {
                return conduit.getShape().getTotalShape();
            }
        }

        // If there's no block entity, no shape - this will stop a bounding box flash when the bundle is first placed
        return Shapes.empty();
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return false;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof NewConduitBundleBlockEntity conduit) {
            conduit.updateConnections(level, pos, neighborPos, true);
        }

        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    // region Water-logging

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(WATERLOGGED,
            context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
        BlockPos currentPos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (level.getBlockEntity(currentPos) instanceof NewConduitBundleBlockEntity conduit) {
            // TODO..
            //conduit.updateShape();
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // endregion

    // region Place & Destroy Logic

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!(placer instanceof Player player)) {
            return;
        }

        if (level.getBlockEntity(pos) instanceof NewConduitBundleBlockEntity conduitBundle) {
            Holder<Conduit<?>> conduit = stack.get(ConduitComponents.CONDUIT);
            if (conduit != null) {
                conduitBundle.addConduit(conduit, player);
            } else {
                // We might be placed using a facade item. If we are, apply the facade to the new bundle now.
                var facadeProvider = stack.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER);
                if (facadeProvider != null && facadeProvider.isValid()) {
                    conduitBundle.setFacadeProvider(stack);
                }
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        // TODO: Destroying the last conduit in the block has a laggy disconnect for the neighbours...

        HitResult hit = player.pick(player.blockInteractionRange() + 5, 1, false);

        if (level.getBlockEntity(pos) instanceof NewConduitBundleBlockEntity conduitBundle) {
            // Client side does nothing special to the bundle.
            /*if (level.isClientSide) {
                // Only do it on the server - otherwise we get a strange flicker as the connections are removed.
                // TODO: is this the right strategy, or should we do bundle remove logic on the client too...
                return false;
            }*/

            if (conduitBundle.hasFacade() && FacadeHelper.areFacadesVisible()) {
                SoundType soundtype = state.getSoundType(level, pos, player);
                level.playSound(player, pos, soundtype.getBreakSound(), SoundSource.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                if (!player.getAbilities().instabuild) {
                    conduitBundle.dropFacadeItem();
                }

                int lightLevelBefore = level.getLightEmission(pos);

                conduitBundle.clearFacade();

                // Handle light update
                if (lightLevelBefore != level.getLightEmission(pos)) {
                    level.getLightEngine().checkBlock(pos);
                }

                if (conduitBundle.isEmpty()) {
                    return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
                } else {
                    level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
                    return false;
                }
            } else {
                Holder<Conduit<?>> conduit = conduitBundle.getShape()
                    .getConduit(((BlockHitResult) hit).getBlockPos(), hit);
                if (conduit == null) {
                    if (!conduitBundle.getConduits().isEmpty()) {
                        level.playSound(player, pos, SoundEvents.GENERIC_SMALL_FALL, SoundSource.BLOCKS, 1F, 1F);
                        return false;
                    }

                    return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
                }

                SoundType soundtype = state.getSoundType(level, pos, player);
                level.playSound(player, pos, soundtype.getBreakSound(), SoundSource.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                conduitBundle.removeConduit(conduit, player);

                if (conduitBundle.isEmpty()) {
                    return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
                } else {
                    level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));

                    if (level.isClientSide) {
                        ConduitBreakParticle.addDestroyEffects(pos, conduit.value());
                    }
                    return false;
                }
            }

        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    // endregion

    // region Item Interactions

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
        BlockHitResult hitResult) {

        if (level.getBlockEntity(pos) instanceof NewConduitBundleBlockEntity conduitBundle) {
            var result = addConduit(stack, level, pos, player, conduitBundle);
            if (result != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
                return result;
            }

            // TODO: Yeta wrench handling

            result = addFacade(stack, level, pos, player, conduitBundle);
            if (result != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
                return result;
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    private ItemInteractionResult addConduit(ItemStack stack, Level level, BlockPos pos, Player player, NewConduitBundleBlockEntity conduitBundle) {
        // Get the conduit from the item
        Holder<Conduit<?>> conduit = stack.get(ConduitComponents.CONDUIT);
        if (conduit == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!conduitBundle.canAddConduit(conduit)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Do not consult the bundle on the client.
        if (!level.isClientSide()) {
            // Attempt to add to the bundle
            RightClickAction addResult = conduitBundle.addConduit(conduit, player);

            if (addResult instanceof RightClickAction.Upgrade upgradeResult) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    player.getInventory()
                        .placeItemBackInInventory(ConduitBlockItem.getStackFor(upgradeResult.replacedConduit(), 1));
                }
            } else if (addResult instanceof RightClickAction.Insert addedResult) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            } else {
                if (!FMLLoader.isProduction()) {
                    throw new IllegalStateException("ConduitBundleAccessor#canAddConduit returned true, but addConduit returned BLOCKED");
                }

                return ItemInteractionResult.FAIL;
            }

            BlockState blockState = level.getBlockState(pos);
            SoundType soundtype = blockState.getSoundType(level, pos, player);
            level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, blockState));
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    private ItemInteractionResult addFacade(ItemStack stack, Level level, BlockPos pos, Player player, NewConduitBundleBlockEntity conduitBundle) {
        var facadeProvider = stack.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER);
        if (facadeProvider == null || !facadeProvider.isValid()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (conduitBundle.hasFacade()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide()) {
            int lightLevelBefore = level.getLightEmission(pos);

            conduitBundle.setFacadeProvider(stack);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            // Handle light change
            if (lightLevelBefore != level.getLightEmission(pos)) {
                level.getLightEngine().checkBlock(pos);
            }

            // Block place effects
            BlockState blockState = level.getBlockState(pos);
            SoundType soundtype = blockState.getSoundType(level, pos, player);
            level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, blockState));
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    // endregion

    // region Hardcoded Redstone Logic

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        if (direction == null) {
            return false;
        }

        if (level.getBlockEntity(pos) instanceof NewConduitBundleBlockEntity conduitBundle) {
            // TODO
        }

        return super.canConnectRedstone(state, level, pos, direction);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return super.getSignal(state, level, pos, direction);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return super.getDirectSignal(state, level, pos, direction);
    }

    // endregion

    // region Facade Behaviours

    private Optional<Block> getFacadeBlock(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof NewConduitBundleBlockEntity conduitBundle) {
            if (conduitBundle.hasFacade()) {
                return Optional.of(conduitBundle.getFacadeBlock());
            }
        }

        return Optional.empty();
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
        @Nullable BlockState queryState, @Nullable BlockPos queryPos) {

        Optional<Block> facade = getFacadeBlock(level, pos);
        if (facade.isPresent()) {
            return facade.get().defaultBlockState();
        }

        return super.getAppearance(state, level, pos, side, queryState, queryPos);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        Optional<Block> facade = getFacadeBlock(level, pos);
        if (facade.isPresent()) {
            return facade.get().getLightEmission(facade.get().defaultBlockState(), level, pos);
        }

        return super.getLightEmission(state, level, pos);
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        Optional<Block> facade = getFacadeBlock(level, pos);
        if (facade.isPresent()) {
            return facade.get().getFriction(facade.get().defaultBlockState(), level, pos, entity);
        }

        return super.getFriction(state, level, pos, entity);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        Optional<Block> facade = getFacadeBlock(level, pos);
        if (facade.isPresent()) {
            return facade.get().getSoundType(facade.get().defaultBlockState(), level, pos, entity);
        }

        return super.getSoundType(state, level, pos, entity);
    }

    @Override
    public boolean supportsExternalFaceHiding(BlockState state) {
        return true;
    }

    // endregion

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        // Disabled for custom handling.
    }
}
