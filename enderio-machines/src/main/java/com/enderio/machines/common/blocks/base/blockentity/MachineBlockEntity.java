package com.enderio.machines.common.blocks.base.blockentity;

import com.enderio.base.api.UseOnly;
import com.enderio.base.api.capability.SideConfig;
import com.enderio.base.api.io.IOConfigurable;
import com.enderio.base.api.io.IOMode;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.base.common.block.EIOBlockEntity;
import com.enderio.base.common.blockentity.Wrenchable;
import com.enderio.machines.common.MachineNBTKeys;
import com.enderio.machines.common.block.LegacyMachineBlock;
import com.enderio.machines.common.blockentity.base.LegacyMachineBlockEntity;
import com.enderio.machines.common.blocks.base.block.ProgressMachineBlock;
import com.enderio.machines.common.blocks.base.inventory.MachineInventory;
import com.enderio.machines.common.blocks.base.inventory.MachineInventoryLayout;
import com.enderio.machines.common.blocks.base.state.MachineState;
import com.enderio.machines.common.init.MachineAttachments;
import com.enderio.machines.common.init.MachineDataComponents;
import com.enderio.machines.common.io.IOConfig;
import com.enderio.machines.common.io.SidedIOConfigurable;
import com.enderio.machines.common.io.TransferUtil;
import com.enderio.machines.common.network.CycleIOConfigPacket;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * Base block entity implementation for machines.
 * Implements Redstone Control and the Machine State system.
 */
public abstract class MachineBlockEntity extends EIOBlockEntity
        implements MenuProvider, Wrenchable, IOConfigurable, MachineInventoryHolder {

    public static final ICapabilityProvider<MachineBlockEntity, Direction, SideConfig> SIDE_CONFIG_PROVIDER = (be,
            side) -> side != null && be.isIOConfigMutable() ? new SidedIOConfigurable(be, side) : null;

    public static final ICapabilityProvider<MachineBlockEntity, Direction, IItemHandler> ITEM_HANDLER_PROVIDER = (be,
            side) -> be.inventory != null ? be.inventory.getForSide(side) : null;

    private static final ModelProperty<IOConfigurable> IO_CONFIG_PROPERTY = LegacyMachineBlockEntity.IO_CONFIG_PROPERTY;

    @Nullable
    private final MachineInventory inventory;

    private IOConfig ioConfig;
    private final boolean isIoConfigMutable;

    private Set<MachineState> states = new HashSet<>();

    private RedstoneControl redstoneControl = RedstoneControl.ALWAYS_ACTIVE;
    private boolean isRedstoneBlocked;

    private final boolean supportsActiveState;

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState,
            boolean isIoConfigMutable) {
        super(type, worldPosition, blockState);

        this.isIoConfigMutable = isIoConfigMutable;
        this.ioConfig = getDefaultIOConfig();

        // Create inventory if the machine has a layout
        var layout = createInventoryLayout();
        if (layout != null) {
            inventory = createMachineInventory(layout);
        } else {
            inventory = null;
        }

        this.supportsActiveState = blockState.hasProperty(ProgressMachineBlock.POWERED);
    }

    /**
     * @return True if the machine is active, this applies the POWERED block state if available.
     */
    public abstract boolean isActive();

    @Override
    public void serverTick() {
        super.serverTick();

        if (canAct(distributeResourcesInterval())) {
            distributeResources();
        }

        // Every 5 ticks, ensure active state is up-to-date.
        // Doing this every 5 ticks instead of every tick should reduce visual flicker.
        if (canAct(5)) {
            boolean isActive = isActive();
            boolean needBlockStateUpdate = supportsActiveState
                    && getBlockState().getValue(ProgressMachineBlock.POWERED) != isActive;
            boolean needStateUpdate = states.contains(MachineState.ACTIVE) != isActive;

            if (needBlockStateUpdate) {
                level.setBlockAndUpdate(worldPosition,
                        getBlockState().setValue(ProgressMachineBlock.POWERED, isActive));
            }

            if (needStateUpdate) {
                updateMachineState(MachineState.ACTIVE, isActive);
            }
        }
    }

    /**
     * Get the facing direction of the machine.
     */
    protected final Direction getBlockFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(LegacyMachineBlock.FACING)) {
            return getBlockState().getValue(LegacyMachineBlock.FACING);
        }

        return Direction.SOUTH;
    }

    // region Ticking Control

    public boolean canAct() {
        if (level == null) {
            return false;
        }

        return !isRedstoneBlocked();
    }

    public boolean canAct(int interval) {
        return level != null && canAct() && level.getGameTime() % interval == 0;
    }

    // endregion

    // region Inventory

    /**
     * Define the slot layout for the machine.
     * @return The slot layout or null for no inventory.
     */
    @Nullable
    protected MachineInventoryLayout createInventoryLayout() {
        return null;
    }

    /**
     * @apiNote inventories should call {@link MachineBlockEntity#onInventoryContentsChanged}!
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
                MachineBlockEntity.this.updateMachineState(state, add);
            }
        };
    }

    public final MachineInventory getInventory() {
        if (!hasInventory()) {
            throw new IllegalStateException("This machine does not have an inventory.");
        }

        return inventory;
    }

    public final boolean hasInventory() {
        return inventory != null;
    }

    /**
     * @apiNote Must call this on custom MachineInventory handlers!
     */
    protected void onInventoryContentsChanged(int slot) {
    }

    // endregion

    // region IO Config

    public IOConfig getDefaultIOConfig() {
        return IOConfig.empty();
    }

    public final IOConfig getIOConfig() {
        if (isIOConfigMutable()) {
            return ioConfig;
        }

        return getDefaultIOConfig();
    }

    private void setIOConfig(IOConfig ioConfig) {
        this.ioConfig = ioConfig;

        if (level != null) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // region IOConfigurable Implementation

    @Override
    public IOMode getIOMode(Direction side) {
        return getIOConfig().getMode(translateIOSide(side));
    }

    @Override
    public final boolean isIOConfigMutable() {
        return isIoConfigMutable;
    }

    @Override
    public boolean shouldRenderIOConfigOverlay() {
        return isIOConfigMutable();
    }

    @Override
    public void setIOMode(Direction side, IOMode mode) {
        if (!isIOConfigMutable()) {
            throw new IllegalStateException("Cannot edit fixed IO mode.");
        }

        if (!supportsIOMode(side, mode)) {
            throw new IllegalStateException("Cannot use this mode on this side.");
        }

        Direction localSide = translateIOSide(side);
        setIOConfig(ioConfig.withMode(localSide, mode));

        // Invalidate caps
        level.invalidateCapabilities(worldPosition);
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public ModelData getModelData() {
        if (!shouldRenderIOConfigOverlay()) {
            return ModelData.EMPTY;
        }

        return ModelData.builder().with(IO_CONFIG_PROPERTY, this).build();
    }

    @UseOnly(LogicalSide.CLIENT)
    private void clientIOConfigChanged() {
        if (shouldRenderIOConfigOverlay()) {
            requestModelDataUpdate();
        }

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public boolean supportsIOMode(Direction side, IOMode state) {
        return true;
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

    // endregion

    // endregion

    // region Resource Distribution

    // TODO: I kind of want to rewrite this without relying on getSelfCapability.

    /**
     * If you distribute resources faster than a normal machine, set the minimum interval here.
     */
    protected int distributeResourcesInterval() {
        return 5;
    }

    @UseOnly(LogicalSide.SERVER)
    protected final void distributeResources() {
        // TODO: Quick way to see if any sides are set to force.

        for (Direction side : Direction.values()) {
            IOMode mode = getIOMode(side);
            if (mode.canForce()) {
                distributeResources(side);
            }
        }
    }

    protected void distributeResources(Direction side) {
        if (canAct(20)) {
            distributeItems(side);
        }

        if (canAct(5)) {
            distributeFluids(side);
        }
    }

    private void distributeItems(Direction side) {
        IItemHandler selfHandler = getSelfCapability(Capabilities.ItemHandler.BLOCK, side);
        IItemHandler otherHandler = getNeighbouringCapability(Capabilities.ItemHandler.BLOCK, side);
        if (selfHandler == null || otherHandler == null) {
            return;
        }

        TransferUtil.distributeItems(getIOMode(side), selfHandler, otherHandler);
    }

    private void distributeFluids(Direction side) {
        IFluidHandler selfHandler = getSelfCapability(Capabilities.FluidHandler.BLOCK, side);
        IFluidHandler otherHandler = getNeighbouringCapability(Capabilities.FluidHandler.BLOCK, side);
        if (selfHandler == null || otherHandler == null) {
            return;
        }

        TransferUtil.distributeFluids(getIOMode(side), selfHandler, otherHandler);
    }

    // endregion

    // region Machine States

    public Set<MachineState> getMachineStates() {
        return states;
    }

    @UseOnly(LogicalSide.CLIENT)
    public void clientSetMachineStates(Set<MachineState> states) {
        if (level == null) {
            return;
        }

        this.states = states;
    }

    protected void updateMachineState(MachineState state, boolean predicate) {
        if (predicate) {
            pushMachineState(state);
        } else {
            popMachineState(state);
        }
    }

    @UseOnly(LogicalSide.SERVER)
    protected void pushMachineState(MachineState state) {
        if (level == null) {
            return;
        }

        states.add(state);
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @UseOnly(LogicalSide.SERVER)
    protected void popMachineState(MachineState state) {
        if (level == null) {
            return;
        }

        states.remove(state);
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    // endregion

    // region Redstone Control

    // TODO: Maybe put this into a constructor parameter instead.
    /**
     * Whether the machine can be controlled by Redstone.
     * Do not turn on/off dynamically.
     */
    public boolean supportsRedstoneControl() {
        return true;
    }

    /**
     * @return The redstone control mode for the machine
     * @throws IllegalStateException if the machine does not support redstone control.
     */
    public final RedstoneControl getRedstoneControl() {
        if (!supportsRedstoneControl()) {
            throw new IllegalStateException("This machine does not support redstone control.");
        }

        return redstoneControl;
    }

    public void setRedstoneControl(RedstoneControl control) {
        redstoneControl = control;
        setChanged();
        checkIsRedstoneBlocked();
    }

    // Final, handoff to supportsRedstoneControl now.
    @Override
    protected final boolean supportsRedstonePower() {
        return supportsRedstoneControl();
    }

    @Override
    protected void onRedstonePowerChanged() {
        checkIsRedstoneBlocked();
    }

    private void checkIsRedstoneBlocked() {
        if (supportsRedstoneControl()) {
            isRedstoneBlocked = !redstoneControl.isActive(isRedstonePowered());
            updateMachineState(MachineState.REDSTONE, isRedstoneBlocked);
        }
    }

    protected boolean isRedstoneBlocked() {
        return isRedstoneBlocked;
    }

    // endregion

    // region Wrenchable Implementation

    @Override
    public ItemInteractionResult onWrenched(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || level == null) {
            return ItemInteractionResult.SUCCESS;
        }

        // Holding shift
        if (player.isSecondaryUseActive()) {
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

            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        } else {
            if (level.isClientSide()) {
                if (isIOConfigMutable()) {
                    PacketDistributor.sendToServer(new CycleIOConfigPacket(worldPosition, context.getClickedFace()));
                }
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
    }

    // endregion

    // region Menu Provider Implementation

    @Override
    public Component getDisplayName() {
        // Default the menu title to the name of the machine's block.
        return getBlockState().getBlock().getName();
    }

    // endregion

    // region Serialization

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (supportsRedstoneControl()) {
            tag.put(MachineNBTKeys.REDSTONE_CONTROL, redstoneControl.save(registries));
        }

        if (hasInventory()) {
            tag.put(MachineNBTKeys.ITEMS, inventory.serializeNBT(registries));
        }
    }

    // Opt into network syncing
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditionalSynced(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditionalSynced(tag, registries);

        if (isIoConfigMutable && ioConfig != null) {
            tag.put(MachineNBTKeys.IO_CONFIG, ioConfig.save(registries));
        }
    }

    @SuppressWarnings("removal")
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (hasInventory() && tag.contains(MachineNBTKeys.ITEMS)) {
            inventory.deserializeNBT(registries, tag.getCompound(MachineNBTKeys.ITEMS));
        }

        // Supports old attachments.
        // We've been misusing attachments, they're intended for 3rd party data
        // additions
        // Reverting this will take some time

        // Same for IO Config
        // TODO: Ender IO 8 - remove.
        if (hasData(MachineAttachments.IO_CONFIG)) {
            ioConfig = getData(MachineAttachments.IO_CONFIG);
            removeData(MachineAttachments.IO_CONFIG);
        } else if (tag.contains(MachineNBTKeys.IO_CONFIG)) {
            ioConfig = IOConfig.parseOptional(registries, tag.getCompound(MachineNBTKeys.IO_CONFIG));

            if (level != null && level.isClientSide) {
                clientIOConfigChanged();
            }
        }

        if (supportsRedstoneControl()) {
            // TODO: Ender IO 8 - remove.
            if (hasData(MachineAttachments.REDSTONE_CONTROL)) {
                redstoneControl = getData(MachineAttachments.REDSTONE_CONTROL);
                removeData(MachineAttachments.REDSTONE_CONTROL);
            } else if (tag.contains(MachineNBTKeys.REDSTONE_CONTROL)) {
                redstoneControl = RedstoneControl.parse(registries,
                        Objects.requireNonNull(tag.get(MachineNBTKeys.REDSTONE_CONTROL)));
            }
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        if (hasInventory()) {
            this.inventory
                    .copyFromItem(componentInput.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
        }

        if (isIOConfigMutable()) {
            ioConfig = componentInput.getOrDefault(MachineDataComponents.IO_CONFIG, IOConfig.empty());
        }

        if (supportsRedstoneControl()) {
            var redstoneControl = componentInput.get(MachineDataComponents.REDSTONE_CONTROL);
            if (redstoneControl != null) {
                this.redstoneControl = redstoneControl;
            }
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);

        if (hasInventory()) {
            components.set(DataComponents.CONTAINER, this.inventory.toItemContents());
        }

        if (isIOConfigMutable()) {
            components.set(MachineDataComponents.IO_CONFIG, ioConfig);
        }

        if (supportsRedstoneControl()) {
            components.set(MachineDataComponents.REDSTONE_CONTROL, redstoneControl);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(MachineNBTKeys.ITEMS);
        tag.remove(MachineNBTKeys.IO_CONFIG);
        tag.remove(MachineNBTKeys.REDSTONE_CONTROL);
    }

    // endregion
}
