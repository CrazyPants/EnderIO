package com.enderio.conduits.common.conduit.bundle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitCapabilities;
import com.enderio.conduits.api.ConduitRedstoneSignalAware;
import com.enderio.conduits.api.bundle.AddConduitResult;
import com.enderio.conduits.api.connection.ConnectionStatus;
import com.enderio.conduits.client.model.conduit.facades.FacadeHelper;
import com.enderio.conduits.client.particle.ConduitBreakParticle;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.conduit.menu.ConduitMenu;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitConnectionConfig;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitNetworkContext;
import com.enderio.conduits.common.init.ConduitBlockEntities;
import com.enderio.conduits.common.init.ConduitComponents;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.conduits.common.redstone.RedstoneInsertFilter;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
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

public class ConduitBundleBlock extends Block implements EntityBlock {

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ConduitBundleBlock(Properties properties) {
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
        // return createTickerHelper(blockEntityType, typeSupplier.get(),
        // EIOBlockEntity::tick);
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof ConduitBundleBlockEntity conduitBundleBlockEntity) {
                if (level.isClientSide) {
                    conduitBundleBlockEntity.clientTick();
                } else {
                    conduitBundleBlockEntity.serverTick();
                }
                conduitBundleBlockEntity.endTick();
            }
        };
    }

    // region Block Shape

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getBundleShape(level, pos, true);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return super.getInteractionShape(state, level, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return getBundleShape(level, pos, false);
    }

    private VoxelShape getBundleShape(BlockGetter level, BlockPos pos, boolean canHideFacade) {
        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduit) {
            if (conduit.hasFacade() && (!canHideFacade || FacadeHelper.areFacadesVisible())) {
                return Shapes.block();
            }

            // Ensure if a bundle is bugged with 0 conduits that it can be broken.
            if (!conduit.getConduits().isEmpty()) {
                return conduit.getShape().getTotalShape();
            }
        }

        // If there's no block entity, no shape - this will stop a bounding box flash
        // when the bundle is first placed
        return Shapes.empty();
    }

    // endregion

    // TODO: Review, I'm sure this could be neater
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
            Player player) {
        if (level instanceof Level realLevel
                && state.getOptionalValue(BlockStateProperties.WATERLOGGED).orElse(false)) {
            var hitResult = Item.getPlayerPOVHitResult(realLevel, player, ClipContext.Fluid.NONE);
            if (hitResult.getType() == HitResult.Type.MISS) {
                return Items.AIR.getDefaultInstance();
            }

            if (hitResult.getBlockPos().equals(pos)) {
                target = hitResult;
            } else {
                return level.getBlockState(hitResult.getBlockPos())
                        .getCloneItemStack(hitResult, level, hitResult.getBlockPos(), player);
            }
        }

        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity blockEntity) {
            if (blockEntity.hasFacade() && FacadeHelper.areFacadesVisible()) {
                return blockEntity.getFacadeBlock().asItem().getDefaultInstance();
            }

            Holder<Conduit<?, ?>> conduit = blockEntity.getShape().getConduit(pos, target);
            if (conduit == null) {
                if (blockEntity.getConduits().isEmpty()) {
                    return ItemStack.EMPTY;
                }

                conduit = blockEntity.getConduits().getFirst();
            }

            return ConduitBlockItem.getStackFor(conduit, 1);
        }

        return super.getCloneItemStack(state, target, level, pos, player);
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
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {

        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduit) {
            conduit.updateNeighborRedstone();
            conduit.updateConnections(level, pos, neighborPos, true);

            // Invalidate caps in case of redstone update or something else.
            level.invalidateCapabilities(pos);
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

        if (level.getBlockEntity(currentPos) instanceof ConduitBundleBlockEntity conduit) {
            conduit.updateShape();
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        if (!(placer instanceof Player player)) {
            return;
        }

        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBundle) {
            Holder<Conduit<?, ?>> conduit = stack.get(ConduitComponents.CONDUIT);
            if (conduit != null) {
                conduitBundle.addConduit(conduit, player);
            } else {
                // We might be placed using a facade item. If we are, apply the facade to the
                var facadeProvider = stack.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER);
                if (facadeProvider != null && facadeProvider.isValid()) {
                    conduitBundle.setFacadeProvider(stack);
                }
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest,
            FluidState fluid) {
        // TODO: Destroying the last conduit in the block has a laggy disconnect for the
        // neighbours...

        HitResult hit = player.pick(player.blockInteractionRange() + 5, 1, false);

        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBundle) {
            if (conduitBundle.hasFacade() && FacadeHelper.areFacadesVisible()) {
                SoundType soundtype = state.getSoundType(level, pos, player);
                level.playSound(player, pos, soundtype.getBreakSound(), SoundSource.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                if (!level.isClientSide()) {
                    if (!player.getAbilities().instabuild) {
                        conduitBundle.dropFacadeItem();
                    }
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
                // TODO: accessibility feature flag
                Holder<Conduit<?, ?>> conduit = null;
//                if (true) {
//                    // If the player is holding a conduit and this flag is enabled, they purposely want to break the held conduit.
//                    conduit = ConduitA11yManager.getHeldConduit();
//
//                    // If we don't have the held conduit, exit now.
//                    if (conduit != null && !conduitBundle.hasConduitStrict(conduit)) {
//                        level.playSound(player, pos, SoundEvents.GENERIC_SMALL_FALL, SoundSource.BLOCKS, 1F, 1F);
//                        return false;
//                    }
//
//                    // TODO: If we adopt the strategy of only showing a bigger box when we're holding a conduit, we need to
//                    // fire a packet to the server because we can't read whether the player is using the accessibility option on the server.
//
//                    // TODO: It could also be possible to leave this in? Idk if this would accidentally fire if the client state is up to date...
//                }

                if (conduit == null) {
                    conduit = conduitBundle.getShape().getConduit(((BlockHitResult) hit).getBlockPos(), hit);
                }

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

                if (level.isClientSide) {
                    ConduitBreakParticle.addDestroyEffects(pos, state, conduit.value());
                }

                conduitBundle.removeConduit(conduit, player);

                if (conduitBundle.isEmpty()) {
                    return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
                } else {
                    level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
                    return false;
                }
            }

        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    // endregion

    // region Open Menu

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {

        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBundle) {
            // TODO: The connection shouldn't include the plate.. if we hit the plate open
            // the first conduit?
            var conduitConnection = conduitBundle.getShape().getConnectionFromHit(pos, hitResult);

            if (conduitConnection != null) {
                if (conduitBundle.getConnectionStatus(conduitConnection.getFirst(),
                        conduitConnection.getSecond()) == ConnectionStatus.CONNECTED_BLOCK) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        ConduitMenu.openConduitMenu(serverPlayer, conduitBundle, conduitConnection.getFirst(),
                                conduitConnection.getSecond());
                    }

                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    // endregion

    // region Item Interactions

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBundle) {
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

    private ItemInteractionResult addConduit(ItemStack stack, Level level, BlockPos pos, Player player,
            ConduitBundleBlockEntity conduitBundle) {
        // Get the conduit from the item
        Holder<Conduit<?, ?>> conduit = stack.get(ConduitComponents.CONDUIT);
        if (conduit == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!conduitBundle.canAddConduit(conduit)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Attempt to add to the bundle
        AddConduitResult addResult = conduitBundle.addConduit(conduit, player);

        if (addResult instanceof AddConduitResult.Upgrade(Holder<Conduit<?, ?>> replacedConduit)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                player.getInventory().placeItemBackInInventory(ConduitBlockItem.getStackFor(replacedConduit, 1));
            }
        } else if (addResult instanceof AddConduitResult.Insert) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        } else {
            if (!FMLLoader.isProduction()) {
                throw new IllegalStateException(
                        "ConduitBundleAccessor#canAddConduit returned true, but addConduit returned BLOCKED");
            }

            return ItemInteractionResult.FAIL;
        }

        BlockState blockState = level.getBlockState(pos);
        SoundType soundtype = blockState.getSoundType(level, pos, player);
        level.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, blockState));

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    private ItemInteractionResult addFacade(ItemStack stack, Level level, BlockPos pos, Player player,
            ConduitBundleBlockEntity conduitBundle) {
        var facadeProvider = stack.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER);
        if (facadeProvider == null || !facadeProvider.isValid()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (conduitBundle.hasFacade()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

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

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    // endregion

    // region Hardcoded Redstone Logic

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos,
            @Nullable Direction direction) {
        if (direction == null) {
            return false;
        }

        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBundle) {
            // Get the redstone conduit
            var redstoneConduit = conduitBundle.getConduitByType(ConduitTypes.REDSTONE.get());
            if (redstoneConduit == null) {
                return false;
            }

            var status = conduitBundle.getConnectionStatus(direction, redstoneConduit);
            if (status != ConnectionStatus.CONNECTED_BLOCK) {
                return false;
            }

            var config = conduitBundle.getConnectionConfig(direction, redstoneConduit,
                    RedstoneConduitConnectionConfig.TYPE);
            return config.canSend(ConduitRedstoneSignalAware.NONE);
        }

        return false;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getNetworkSignal(level, pos, direction, false);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getNetworkSignal(level, pos, direction, true);
    }

    private int getNetworkSignal(BlockGetter level, BlockPos pos, Direction direction, boolean isDirectSignal) {
        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBundle) {
            // No client redstone, network is server-only.
            if (conduitBundle.getLevel().isClientSide()) {
                return 0;
            }

            // Get the redstone conduit
            var redstoneConduit = conduitBundle.getConduitByType(ConduitTypes.REDSTONE.get());
            if (redstoneConduit == null) {
                return 0;
            }

            var status = conduitBundle.getConnectionStatus(direction.getOpposite(), redstoneConduit);
            if (status != ConnectionStatus.CONNECTED_BLOCK) {
                return 0;
            }

            var config = conduitBundle.getConnectionConfig(direction.getOpposite(), redstoneConduit,
                    RedstoneConduitConnectionConfig.TYPE);
            if (!config.canSend(ConduitRedstoneSignalAware.NONE)) {
                return 0;
            }

            if (isDirectSignal && !config.isStrongOutputSignal()) {
                return 0;
            }

            var node = conduitBundle.getConduitNode(redstoneConduit);
            var network = node.getNetwork();
            if (network == null) {
                return 0;
            }

            var context = network.getContext(RedstoneConduitNetworkContext.TYPE);
            if (context == null) {
                return 0;
            }

            if (node.getInsertFilter(direction) instanceof RedstoneInsertFilter redstoneInsertFilter) {
                return redstoneInsertFilter.getOutputSignal(context, config.sendColor());
            }

            return context.getSignal(config.sendColor());
        }

        return 0;
    }

    // endregion

    // region Facade Behaviours

    private Optional<Block> getFacadeBlock(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBundle) {
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
}
