package com.enderio.conduits.common.conduit.block;

import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.conduits.EnderIOConduits;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.ConduitCapabilities;
import com.enderio.conduits.client.model.conduit.facades.FacadeHelper;
import com.enderio.conduits.common.conduit.ConduitBlockItem;
import com.enderio.conduits.common.conduit.ConduitBundle;
import com.enderio.conduits.common.conduit.ConduitSavedData;
import com.enderio.conduits.common.conduit.RightClickAction;
import com.enderio.conduits.common.conduit.connection.ConnectionState;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.enderio.conduits.common.conduit.connection.StaticConnectionStates;
import com.enderio.conduits.common.conduit.graph.ConduitGraphObject;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitData;
import com.enderio.conduits.common.init.ConduitBlockEntities;
import com.enderio.conduits.common.init.ConduitComponents;
import com.enderio.conduits.common.init.ConduitTypes;
import com.enderio.conduits.common.init.Conduits;
import com.enderio.conduits.common.redstone.RedstoneInsertFilter;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = EnderIOConduits.MODULE_MOD_ID)
public class ConduitBundleBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public ConduitBundleBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ConduitBlockEntities.CONDUIT.create(pos, state);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean canBeReplaced(BlockState pState, Fluid pFluid) {
        return false;
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

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduit) {
            conduit.updateConnections(level, pos, fromPos, true);
        }

        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getBundleShape(level, pos, true);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return getBundleShape(level, pos, false);
    }

    private VoxelShape getBundleShape(BlockGetter level, BlockPos pos, boolean canHideFacade) {
        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduit) {
            if (conduit.getBundle().hasFacade() && (!canHideFacade || FacadeHelper.areFacadesVisible())) {
                return Shapes.block();
            }

            // Ensure if a bundle is bugged with 0 conduits that it can be broken.
            if (!conduit.getBundle().getConduits().isEmpty()) {
                return conduit.getShape().getTotalShape();
            }
        }

        return Shapes.block();
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getVisualShape(state, level, pos, context);
    }

    // region Block Interaction

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand interactionHand, BlockHitResult hit) {

        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity blockEntity) {
            var interactionResult = addConduit(blockEntity, player, itemStack, level.isClientSide());
            if (interactionResult.isPresent()) {
                return interactionResult.get();
            }

            interactionResult = handleYeta(blockEntity, player, itemStack, hit, level.isClientSide());
            if (interactionResult.isPresent()) {
                return interactionResult.get();
            }

            interactionResult = handleFacade(blockEntity, player, itemStack, hit, level.isClientSide());
            if (interactionResult.isPresent()) {
                return interactionResult.get();
            }
        }

        return super.useItemOn(itemStack, state, level, pos, player, interactionHand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ConduitBundleBlockEntity conduit) {
            Optional<InteractionResult> interactionResult = handleScreen(conduit, player, hit, level.isClientSide());

            if (interactionResult.isPresent()) {
                return interactionResult.get();
            }
        }

        return super.useWithoutItem(state, level, pos, player, hit);
    }

    private Optional<ItemInteractionResult> addConduit(ConduitBundleBlockEntity blockEntity, Player player,
            ItemStack stack, boolean isClientSide) {
        if (!(stack.getItem() instanceof ConduitBlockItem)) {
            return Optional.empty();
        }

        Holder<Conduit<?>> conduit = stack.get(ConduitComponents.CONDUIT);
        if (conduit == null) {
            return Optional.empty();
        }

        RightClickAction action = blockEntity.addType(conduit, player);

        ItemInteractionResult result;

        if (action instanceof RightClickAction.Upgrade upgradeAction) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                player.getInventory()
                        .placeItemBackInInventory(ConduitBlockItem.getStackFor(upgradeAction.replacedConduit(), 1));
            }
            result = ItemInteractionResult.sidedSuccess(isClientSide);
        } else if (action instanceof RightClickAction.Insert) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            result = ItemInteractionResult.sidedSuccess(isClientSide);
        } else {
            result = ItemInteractionResult.FAIL;
        }

        if (result != ItemInteractionResult.FAIL) {
            Level level = blockEntity.getLevel();
            BlockPos blockpos = blockEntity.getBlockPos();

            BlockState blockState = level.getBlockState(blockpos);
            SoundType soundtype = blockState.getSoundType(level, blockpos, player);
            level.playSound(player, blockpos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockState));
        }

        return Optional.of(result);
    }

    private Optional<ItemInteractionResult> handleYeta(ConduitBundleBlockEntity blockEntity, Player player,
            ItemStack stack, BlockHitResult hit, boolean isClientSide) {
        if (stack.is(EIOTags.Items.WRENCH)) {
            Holder<Conduit<?>> conduit = blockEntity.getShape().getConduit(hit.getBlockPos(), hit);
            Direction direction = blockEntity.getShape().getDirection(hit.getBlockPos(), hit);
            if (conduit == null) {
                return Optional.empty();
            }

            if (isClientSide) {
                return Optional.of(ItemInteractionResult.sidedSuccess(isClientSide));
            }

            internalHandleYeta(conduit, direction, blockEntity, hit);
            return Optional.of(ItemInteractionResult.sidedSuccess(isClientSide));
        }

        return Optional.empty();
    }

    private void internalHandleYeta(Holder<Conduit<?>> conduit, @Nullable Direction direction,
            ConduitBundleBlockEntity blockEntity, BlockHitResult hit) {
        ConduitBundle bundle = blockEntity.getBundle();

        if (direction != null) {
            ConnectionState connectionState = bundle.getConnectionState(direction, conduit);

            if (connectionState instanceof DynamicConnectionState dyn) {
                bundle.getNodeFor(conduit).clearState(direction);
                blockEntity.dropConnectionItems(dyn);
                bundle.setConnectionState(direction, conduit, StaticConnectionStates.DISABLED);
                blockEntity.updateShape();
                blockEntity.onConnectionsUpdated(conduit);
            } else {
                bundle.setConnectionState(direction, conduit, StaticConnectionStates.DISABLED);
                blockEntity.updateShape();
                blockEntity.onConnectionsUpdated(conduit);

                if (blockEntity.getLevel()
                        .getBlockEntity(blockEntity.getBlockPos()
                                .relative(direction)) instanceof ConduitBundleBlockEntity other) {
                    Direction oppositeDirection = direction.getOpposite();

                    bundle.setConnectionState(oppositeDirection, conduit, StaticConnectionStates.DISABLED);
                    other.updateShape();
                    other.onConnectionsUpdated(conduit);
                    ConduitGraphObject thisNode = bundle.getNodeFor(conduit);
                    ConduitGraphObject otherNode = other.getBundle().getNodeFor(conduit);
                    thisNode.getGraph().removeSingleEdge(thisNode, otherNode);
                    thisNode.getGraph().removeSingleEdge(otherNode, thisNode);
                    ConduitSavedData.addPotentialGraph(conduit, thisNode.getGraph(),
                            (ServerLevel) blockEntity.getLevel());
                    ConduitSavedData.addPotentialGraph(conduit, otherNode.getGraph(), (ServerLevel) other.getLevel());
                }
            }
        } else {
            ConnectionState connectionState = bundle.getConnectionState(hit.getDirection(), conduit);

            if (!connectionState.isConnection()) {
                blockEntity.tryConnectTo(hit.getDirection(), conduit, true, true, true);
            }
        }
    }

    @SubscribeEvent
    public static void handleShiftYeta(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().is(EIOTags.Items.WRENCH)
                && event.getLevel().getBlockEntity(event.getPos()) instanceof ConduitBundleBlockEntity blockEntity
                && event.getEntity().isSteppingCarefully()) {

            Holder<Conduit<?>> conduit = blockEntity.getShape().getConduit(event.getPos(), event.getHitVec());
            if (conduit != null) {
                blockEntity.removeTypeAndDelete(event.getEntity(), conduit);
                event.setCanceled(true);
            }
        }
    }

    private Optional<ItemInteractionResult> handleFacade(ConduitBundleBlockEntity blockEntity, Player player,
            ItemStack stack, BlockHitResult hit, boolean isClientSide) {
        var facade = stack.getCapability(ConduitCapabilities.CONDUIT_FACADE_PROVIDER);
        if (facade == null || !facade.isValid()) {
            return Optional.empty();
        }

        if (blockEntity.getBundle().hasFacade()) {
            return Optional.of(ItemInteractionResult.FAIL);
        }

        Level level = blockEntity.getLevel();
        BlockPos blockpos = blockEntity.getBlockPos();

        int lightLevelBefore = level.getLightEmission(blockpos);

        blockEntity.getBundle().facade(stack);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        BlockState blockState = level.getBlockState(blockpos);

        SoundType soundtype = blockState.getSoundType(level, blockpos, player);
        level.playSound(player, blockpos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockState));

        // Handle light update
        if (lightLevelBefore != level.getLightEmission(blockpos)) {
            level.getLightEngine().checkBlock(blockpos);
        }

        return Optional.of(ItemInteractionResult.sidedSuccess(isClientSide));
    }

    private Optional<InteractionResult> handleScreen(ConduitBundleBlockEntity blockEntity, Player player,
            BlockHitResult hit, boolean isClientSide) {
        Optional<OpenInformation> openInformation = getOpenInformation(blockEntity, hit);
        if (openInformation.isPresent()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(
                        blockEntity.menuProvider(openInformation.get().direction(), openInformation.get().conduit()),
                        buf -> {
                            buf.writeBlockPos(blockEntity.getBlockPos());
                            buf.writeEnum(openInformation.get().direction());
                            Conduit.STREAM_CODEC.encode(buf, openInformation.get().conduit());
                        });
            }

            return Optional.of(InteractionResult.sidedSuccess(isClientSide));
        }

        return Optional.empty();
    }

    private Optional<OpenInformation> getOpenInformation(ConduitBundleBlockEntity blockEntity, BlockHitResult hit) {
        Holder<Conduit<?>> conduit = blockEntity.getShape().getConduit(hit.getBlockPos(), hit);
        Direction direction = blockEntity.getShape().getDirection(hit.getBlockPos(), hit);

        if (direction != null && conduit != null) {
            if (canBeOrIsValidConnection(blockEntity, conduit, direction)) {
                return Optional.of(new OpenInformation(direction, conduit));
            }
        }

        if (conduit != null) {
            direction = hit.getDirection();
            if (canBeValidConnection(blockEntity, conduit, direction)) {
                return Optional.of(new OpenInformation(direction, conduit));
            }
        }

        if (conduit != null) {
            for (Direction potential : Direction.values()) {
                if (canBeValidConnection(blockEntity, conduit, potential)) {
                    return Optional.of(new OpenInformation(potential, conduit));
                }
            }
        }

        ConduitBundle bundle = blockEntity.getBundle();

        // fallback
        for (Direction potential : Direction.values()) {
            if (bundle.isConnectionEnd(potential)) {
                for (Holder<Conduit<?>> potentialType : bundle.getConduits()) {
                    if (bundle.getConnectionState(potential, potentialType) instanceof DynamicConnectionState) {
                        return Optional.of(new OpenInformation(potential, potentialType));
                    }
                }
                throw new IllegalStateException("couldn't find connection even though it should be present");
            }
        }

        for (Direction potential : Direction.values()) {
            if (!(blockEntity.getLevel()
                    .getBlockEntity(
                            blockEntity.getBlockPos().relative(potential)) instanceof ConduitBundleBlockEntity)) {
                for (Holder<Conduit<?>> potentialType : bundle.getConduits()) {
                    if (canBeValidConnection(blockEntity, potentialType, potential)) {
                        return Optional.of(new OpenInformation(potential, potentialType));
                    }
                }
            }
        }

        return Optional.empty();
    }

    // endregion

    public static boolean canBeOrIsValidConnection(ConduitBundleBlockEntity blockEntity, Holder<Conduit<?>> conduit,
            Direction direction) {
        ConduitBundle bundle = blockEntity.getBundle();

        return bundle.getConnectionState(direction, conduit) instanceof DynamicConnectionState
                || canBeValidConnection(blockEntity, conduit, direction);
    }

    public static boolean canBeValidConnection(ConduitBundleBlockEntity blockEntity, Holder<Conduit<?>> conduit,
            Direction direction) {
        ConduitBundle bundle = blockEntity.getBundle();
        ConnectionState connectionState = bundle.getConnectionState(direction, conduit);
        return connectionState instanceof StaticConnectionStates state && state == StaticConnectionStates.DISABLED
                && !(blockEntity.getLevel()
                        .getBlockEntity(
                                blockEntity.getBlockPos().relative(direction)) instanceof ConduitBundleBlockEntity);
    }

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
            Optional<Block> facade = blockEntity.getBundle().facade();
            if (facade.isPresent() && FacadeHelper.areFacadesVisible()) {
                return facade.get().asItem().getDefaultInstance();
            }

            Holder<Conduit<?>> conduit = blockEntity.getShape().getConduit(pos, target);
            if (conduit == null) {
                if (blockEntity.getBundle().getConduits().isEmpty()) {
                    return ItemStack.EMPTY;
                }

                conduit = blockEntity.getBundle().getConduits().getFirst();
            }

            return ConduitBlockItem.getStackFor(conduit, 1);
        }

        return super.getCloneItemStack(state, target, level, pos, player);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> blockEntityType) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof ConduitBundleBlockEntity conduitBundleBlockEntity) {
                conduitBundleBlockEntity.everyTick();
            }
        };
    }

    // region Place and destroy logic

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        Holder<Conduit<?>> conduit = stack.get(ConduitComponents.CONDUIT);
        if (conduit == null) {
            return;
        }

        if (!(placer instanceof Player player)) {
            return;
        }

        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity blockEntity) {
            blockEntity.addType(conduit, player);
            if (level.isClientSide()) {
                blockEntity.updateClient();
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest,
            FluidState fluid) {
        HitResult hit = player.pick(player.blockInteractionRange() + 5, 1, false);
        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity blockEntity) {

            if (blockEntity.getBundle().hasFacade() && FacadeHelper.areFacadesVisible()) {
                SoundType soundtype = state.getSoundType(level, pos, player);
                level.playSound(player, pos, soundtype.getBreakSound(), SoundSource.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                if (!player.getAbilities().instabuild) {
                    blockEntity.dropFacadeItem();
                }

                int lightLevelBefore = level.getLightEmission(pos);

                blockEntity.getBundle().clearFacade();

                // Handle light update
                if (lightLevelBefore != level.getLightEmission(pos)) {
                    level.getLightEngine().checkBlock(pos);
                }
            } else {
                Holder<Conduit<?>> conduit = blockEntity.getShape()
                        .getConduit(((BlockHitResult) hit).getBlockPos(), hit);
                if (conduit == null) {
                    if (!blockEntity.getBundle().getConduits().isEmpty()) {
                        level.playSound(player, pos, SoundEvents.GENERIC_SMALL_FALL, SoundSource.BLOCKS, 1F, 1F);
                        return false;
                    }
                    return true;
                }

                SoundType soundtype = state.getSoundType(level, pos, player);
                level.playSound(player, pos, soundtype.getBreakSound(), SoundSource.BLOCKS,
                        (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                if (blockEntity.removeType(conduit, !player.getAbilities().instabuild)) {
                    return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
                }
            }

            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, state));
            return false;
        }

        // No block entity, get rid of it immediately
        return true;
    }

    // endregion

    // region Redstone

    //@formatter:off
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        if (direction == null) {
            return false;
        }

        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBundleBlockEntity && conduitBundleBlockEntity.getLevel() != null) {
            Holder<Conduit<?>> redstoneConduit = conduitBundleBlockEntity.getLevel().holderOrThrow(Conduits.REDSTONE);
            ConduitBundle conduitBundle = conduitBundleBlockEntity.getBundle();

            return conduitBundle.getConduits().contains(redstoneConduit) &&
                conduitBundle.getConnectionState(direction.getOpposite(), redstoneConduit) instanceof DynamicConnectionState;
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getSignal(BlockState pBlockState, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduitBundleBlockEntity && conduitBundleBlockEntity.getLevel() != null) {
            // TODO: Need to decouple this from the holder registry. Probably need to find conduits by their "type" instead.
            Holder<Conduit<?>> redstoneConduit = conduitBundleBlockEntity.getLevel().holderOrThrow(Conduits.REDSTONE);
            ConduitBundle conduitBundle = conduitBundleBlockEntity.getBundle();

            if (!conduitBundle.getConduits().contains(redstoneConduit)) {
                return 0;
            }

            if (!(conduitBundle.getConnectionState(direction.getOpposite(), redstoneConduit) instanceof DynamicConnectionState dyn)) {
                return 0;
            }

            if (!dyn.isInsert()) {
                return 0;
            }

            if (!conduitBundle.getNodeFor(redstoneConduit).hasData(ConduitTypes.Data.REDSTONE.get())) {
                return 0;
            }

            RedstoneConduitData data = conduitBundle.getNodeFor(redstoneConduit).getData(ConduitTypes.Data.REDSTONE.get());
            return getSignalOutput(dyn, Objects.requireNonNull(data));
        }

        return 0;
    }

    // TODO: Redstone conduit strong signals.
    @Override
    protected int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return super.getDirectSignal(pState, pLevel, pPos, pDirection);
    }

    //@formatter:on

    private int getSignalOutput(DynamicConnectionState connectionState, RedstoneConduitData data) {
        if (connectionState.filterInsert()
                .getCapability(EIOCapabilities.Filter.ITEM) instanceof RedstoneInsertFilter filter) {
            return filter.getOutputSignal(data, connectionState.insertChannel());
        }
        return data.getSignal(connectionState.insertChannel());
    }

    // endregion

    private Optional<Block> getFacadeBlock(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ConduitBundleBlockEntity conduit) {
            return conduit.getBundle().facade();
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

    private record OpenInformation(Direction direction, Holder<Conduit<?>> conduit) {
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {

    }
}
