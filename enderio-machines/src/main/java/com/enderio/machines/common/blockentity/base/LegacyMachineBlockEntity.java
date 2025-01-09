package com.enderio.machines.common.blockentity.base;

import com.enderio.base.api.capability.SideConfig;
import com.enderio.base.api.io.IOConfigurable;
import com.enderio.base.api.io.IOMode;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.base.common.blockentity.Wrenchable;
import com.enderio.core.common.blockentity.EnderBlockEntity;
import com.enderio.core.common.network.NetworkDataSlot;
import com.enderio.machines.common.MachineNBTKeys;
import com.enderio.machines.common.block.LegacyMachineBlock;
import com.enderio.machines.common.blocks.base.blockentity.MachineInventoryHolder;
import com.enderio.machines.common.blocks.base.inventory.MachineInventory;
import com.enderio.machines.common.blocks.base.inventory.MachineInventoryLayout;
import com.enderio.machines.common.blocks.base.state.MachineState;
import com.enderio.machines.common.init.MachineAttachments;
import com.enderio.machines.common.init.MachineDataComponents;
import com.enderio.machines.common.io.IOConfig;
import com.enderio.machines.common.io.SidedIOConfigurable;
import com.enderio.machines.common.io.TransferUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import me.liliandev.ensure.ensures.EnsureSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true, since = "7.1")
public abstract class LegacyMachineBlockEntity extends EnderBlockEntity
        implements MenuProvider, Wrenchable, IOConfigurable, MachineInventoryHolder {

    public static final ICapabilityProvider<LegacyMachineBlockEntity, Direction, SideConfig> SIDE_CONFIG_PROVIDER = SidedIOConfigurable::new;

    public static final ICapabilityProvider<LegacyMachineBlockEntity, Direction, IItemHandler> ITEM_HANDLER_PROVIDER = (
            be, side) -> be.inventory != null ? be.inventory.getForSide(side) : null;

    // region IO Configuration

    private final IOConfig defaultIOConfig;

    public static final ModelProperty<IOConfigurable> IO_CONFIG_PROPERTY = new ModelProperty<>();

    private ModelData modelData = ModelData.EMPTY;

    // endregion

    // region Items

    @Nullable
    private final MachineInventory inventory;
    @Nullable
    private final MachineInventoryLayout inventoryLayout;

    // endregion

    private boolean isRedstoneBlocked;

    // region Common Dataslots

    public static final NetworkDataSlot.CodecType<RedstoneControl> REDSTONE_CONTROL_DATA_SLOT_TYPE = new NetworkDataSlot.CodecType<>(
            RedstoneControl.CODEC, RedstoneControl.STREAM_CODEC.cast());

    private final NetworkDataSlot<RedstoneControl> redstoneControlDataSlot;
    private final @Nullable NetworkDataSlot<IOConfig> ioConfigDataSlot;

    // endregion

    private Set<MachineState> states = new HashSet<>();

    public LegacyMachineBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);

        // Create IO Config.
        this.defaultIOConfig = getDefaultIOConfig();

        if (!this.hasData(MachineAttachments.IO_CONFIG)) {
            this.setData(MachineAttachments.IO_CONFIG, defaultIOConfig);
        }

        // If the machine declares an inventory layout, use it to create a handler
        inventoryLayout = createInventoryLayout();
        if (inventoryLayout != null) {
            inventory = createMachineInventory(inventoryLayout);
        } else {
            inventory = null;
        }

        if (supportsRedstoneControl()) {
            redstoneControlDataSlot = addDataSlot(
                    REDSTONE_CONTROL_DATA_SLOT_TYPE.create(this::getRedstoneControl, this::internalSetRedstoneControl));
        } else {
            redstoneControlDataSlot = null;
        }

        // Register sync slot for ioConfig and setup model data.
        if (isIOConfigMutable()) {
            ioConfigDataSlot = addDataSlot(IOConfig.DATA_SLOT_TYPE.create(this::getIOConfig, v -> {
                setIOConfig(v);

                if (level != null && level.isClientSide()) {
                    onIOConfigChanged();
                }

                level.invalidateCapabilities(getBlockPos());
            }));
        } else {
            ioConfigDataSlot = null;
        }

        addDataSlot(MachineState.DATA_SLOT_TYPE.create(this::getMachineStates, l -> states = l));
    }

    // region New IO Config

    public IOConfig getDefaultIOConfig() {
        return IOConfig.empty();
    }

    private IOConfig getIOConfig() {
        if (isIOConfigMutable()) {
            return getData(MachineAttachments.IO_CONFIG);
        }

        return defaultIOConfig;
    }

    /**
     * Avoid state-based conditionals in here as this is called once in the constructor to determine whether to sync.
     * @return Whether the player can edit the IO Config.
     */
    public boolean isIOConfigMutable() {
        return true;
    }

    /**
     * @return Whether the block model should show the IO config states.
     */
    public boolean shouldRenderIOConfigOverlay() {
        return isIOConfigMutable();
    }

    public final IOMode getIOMode(Direction side) {
        return getIOConfig().getMode(translateIOSide(side));
    }

    /**
     * Override to declare custom constraints on IOMode's for sides of blocks.
     */
    @SuppressWarnings("unused")
    public boolean supportsIOMode(Direction side, IOMode mode) {
        return true;
    }

    public final void setIOMode(Direction side, IOMode mode) {
        if (!isIOConfigMutable()) {
            throw new IllegalStateException("Cannot edit fixed IO mode.");
        }

        if (!supportsIOMode(side, mode)) {
            throw new IllegalStateException("Cannot use this mode on this side.");
        }

        Direction localSide = translateIOSide(side);

        var ioConfig = getIOConfig();
        var oldMode = ioConfig.getMode(localSide);
        var newIOConfig = ioConfig.withMode(localSide, mode);
        setIOConfig(newIOConfig);

        // Fire change event
        onIOConfigChanged(side, oldMode, mode);
    }

    private void setIOConfig(IOConfig config) {
        if (!isIOConfigMutable()) {
            throw new IllegalStateException("Cannot set IO config when isIOConfigMutable is false.");
        }

        setData(MachineAttachments.IO_CONFIG, config);

        if (level == null) {
            return;
        }

        // Mark entity as changed.
        setChanged();

        // Invalidate capabilities
        level.invalidateCapabilities(getBlockPos());

        // Notify neighbors of update
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    protected void onIOConfigChanged(Direction side, IOMode oldMode, IOMode newMode) {
        if (level != null && level.isClientSide() && isIOConfigMutable()) {
            clientUpdateSlot(ioConfigDataSlot, getIOConfig());
        }
    }

    private Direction translateIOSide(Direction side) {
        // The block faces with its southern face. So the back of the machine.
        Direction south = getBlockFacing();
        return switch (side) {
        case NORTH -> south.getOpposite();
        case SOUTH -> south;
        case WEST -> south.getCounterClockWise();
        case EAST -> south.getClockWise();
        default -> side;
        };
    }

    protected Direction getBlockFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(LegacyMachineBlock.FACING)) {
            return getBlockState().getValue(LegacyMachineBlock.FACING);
        }

        return Direction.SOUTH;
    }

    @Override
    public ModelData getModelData() {
        return shouldRenderIOConfigOverlay() ? modelData : ModelData.EMPTY;
    }

    private void onIOConfigChanged() {
        if (this.level == null) {
            return;
        }

        if (shouldRenderIOConfigOverlay()) {
            modelData = modelData.derive().with(IO_CONFIG_PROPERTY, this).build();
            requestModelDataUpdate();
        }

        this.level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    // endregion

    // region Redstone Control

    /**
     * Whether this block entity supports redstone control
     */
    public boolean supportsRedstoneControl() {
        return true;
    }

    public boolean isRedstoneBlocked() {
        return isRedstoneBlocked;
    }

    public RedstoneControl getRedstoneControl() {
        if (!supportsRedstoneControl()) {
            throw new IllegalStateException("This machine does not support redstone control.");
        }

        return getData(MachineAttachments.REDSTONE_CONTROL);
    }

    public void setRedstoneControl(RedstoneControl redstoneControl) {
        if (!supportsRedstoneControl()) {
            throw new IllegalStateException("This machine does not support redstone control.");
        }

        if (level != null && level.isClientSide()) {
            clientUpdateSlot(redstoneControlDataSlot, redstoneControl);
        } else {
            internalSetRedstoneControl(redstoneControl);
        }
    }

    private void internalSetRedstoneControl(RedstoneControl redstoneControl) {
        setData(MachineAttachments.REDSTONE_CONTROL, redstoneControl);
        setChanged();
        updateRedstone();
    }

    // endregion

    // region Inventory

    /**
     * Create the block entity's inventory slot layout.
     */
    @Nullable
    public MachineInventoryLayout createInventoryLayout() {
        return null;
    }

    /**
     * Get the block entity's inventory slot layout.
     */
    @Nullable
    public MachineInventoryLayout getInventoryLayout() {
        return inventoryLayout;
    }

    @Nullable
    public final MachineInventory getInventory() {
        return inventory;
    }

    @Override
    public boolean hasInventory() {
        return inventory != null;
    }

    /**
     * Only call this if you're sure your machine has an inventory.
     */
    protected final MachineInventory getInventoryNN() {
        return Objects.requireNonNull(inventory);
    }

    /**
     * Called to create an item handler if a slot layout is provided.
     */
    protected MachineInventory createMachineInventory(MachineInventoryLayout layout) {
        return new MachineInventory(this, layout) {
            @Override
            protected void onContentsChanged(int slot) {
                onInventoryContentsChanged(slot);
                setChanged();
            }

            @Override
            public void updateMachineState(MachineState state, boolean add) {
                LegacyMachineBlockEntity.this.updateMachineState(state, add);
            }
        };
    }

    /**
     * @apiNote Must call this on custom MachineInventory handlers!
     */
    protected void onInventoryContentsChanged(int slot) {
    }

    // endregion

    // region Block Entity ticking

    @Override
    public void serverTick() {
        if (canActSlow()) {
            forceResources();
        }

        super.serverTick();
    }

    public boolean canAct() {
        if (this.level == null) {
            return false;
        }

        return !isRedstoneBlocked;
    }

    public boolean canActSlow() {
        if (this.level == null) {
            return false;
        }

        return canAct() && this.level.getGameTime() % 5 == 0;
    }

    // endregion

    // region Resource movement

    /**
     * Push and pull resources to/from other blocks.
     */
    private void forceResources() {
        for (Direction direction : Direction.values()) {
            if (getIOMode(direction).canForce()) {
                // TODO: Maybe some kind of resource distributor so that items are transmitted
                // evenly around? rather than taking the order of Direction.values()
                moveItems(direction);
                moveFluids(direction);
            }
        }
    }

    /**
     * Move items to and from via the given side.
     */
    private void moveItems(Direction side) {
        IItemHandler selfHandler = getSelfCapability(Capabilities.ItemHandler.BLOCK, side);
        IItemHandler otherHandler = getNeighbouringCapability(Capabilities.ItemHandler.BLOCK, side);
        if (selfHandler == null || otherHandler == null) {
            return;
        }

        TransferUtil.distributeItems(getIOMode(side), selfHandler, otherHandler);
    }

    /**
     * Move fluids to and from via the given side.
     */
    private void moveFluids(Direction side) {
        IFluidHandler selfHandler = getSelfCapability(Capabilities.FluidHandler.BLOCK, side);
        IFluidHandler otherHandler = getNeighbouringCapability(Capabilities.FluidHandler.BLOCK, side);
        if (selfHandler == null || otherHandler == null) {
            return;
        }

        TransferUtil.distributeFluids(getIOMode(side), selfHandler, otherHandler);
    }

    // endregion

    // region Serialization

    @Override
    public void onLoad() {
        super.onLoad();
        updateRedstone();
    }

    @Override
    public void saveAdditional(CompoundTag pTag, HolderLookup.Provider lookupProvider) {
        super.saveAdditional(pTag, lookupProvider);

        if (this.inventory != null) {
            pTag.put(MachineNBTKeys.ITEMS, inventory.serializeNBT(lookupProvider));
        }
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider lookupProvider) {
        if (this.inventory != null) {
            inventory.deserializeNBT(lookupProvider, pTag.getCompound(MachineNBTKeys.ITEMS));
        }

        // For rendering io overlays after placed by an nbt filled block item
        if (this.level != null) {
            onIOConfigChanged();
        }

        super.loadAdditional(pTag, lookupProvider);
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);

        if (this.inventory != null) {
            this.inventory.copyFromItem(components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
        }

        if (isIOConfigMutable()) {
            setData(MachineAttachments.IO_CONFIG,
                    components.getOrDefault(MachineDataComponents.IO_CONFIG, IOConfig.empty()));
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        if (this.inventory != null) {
            components.set(DataComponents.CONTAINER, this.inventory.toItemContents());
        }

        if (isIOConfigMutable()) {
            components.set(MachineDataComponents.IO_CONFIG, getData(MachineAttachments.IO_CONFIG));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(MachineNBTKeys.ITEMS);
        removeData(MachineAttachments.IO_CONFIG);
    }

    // endregion

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    // TODO: Rename to onBlockEntityItemUsed?
    // called when a player uses the block entity, before menu is may open.
    public ItemInteractionResult onBlockEntityUsed(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public boolean stillValid(Player pPlayer) {
        if (this.level == null) {
            return false;
        }

        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }

        return pPlayer.canInteractWithBlock(this.worldPosition, 1.5);
    }

    @EnsureSide(EnsureSide.Side.CLIENT)
    @Override
    public ItemInteractionResult onWrenched(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || level == null) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player.isSecondaryUseActive()) {// aka break block
            BlockPos pos = getBlockPos();
            BlockState state = getBlockState();
            Block block = state.getBlock();

            if (level instanceof ServerLevel serverLevel) {
                List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, serverLevel.getBlockEntity(pos));
                Inventory inventory = player.getInventory();
                for (ItemStack item : drops) {
                    inventory.placeItemBackInInventory(item);
                }
            }

            block.playerWillDestroy(level, pos, state, player);
            level.removeBlock(pos, false);
            block.destroy(level, pos, state);

            // TODO: custom sound when sound manager is up and running??
        } else {
            // Cycle side config
            if (level.isClientSide()) {
                if (isIOConfigMutable()) {
                    cycleIOMode(context.getClickedFace());
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    public boolean canOpenMenu() {
        return true;
    }

    public int getLightEmission() {
        return getBlockState().getLightEmission();
    }

    public Set<MachineState> getMachineStates() {
        return states;
    }

    public void updateMachineState(MachineState state, boolean add) {
        if (level != null && level.isClientSide) {
            return;
        }
        if (add) {
            states.add(state);
        } else {
            states.remove(state);
        }
    }

    public void neighborChanged(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        updateRedstone();
    }

    private void updateRedstone() {
        if (supportsRedstoneControl()) {
            boolean active = getRedstoneControl().isActive(this.level.hasNeighborSignal(worldPosition));
            updateMachineState(MachineState.REDSTONE, !active);
            this.isRedstoneBlocked = !active;
        }
    }
}
