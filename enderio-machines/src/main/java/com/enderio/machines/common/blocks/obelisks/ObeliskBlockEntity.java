package com.enderio.machines.common.blocks.obelisks;

import com.enderio.base.api.UseOnly;
import com.enderio.base.api.capacitor.CapacitorScalable;
import com.enderio.base.api.io.IOMode;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.machines.common.MachineNBTKeys;
import com.enderio.machines.common.attachment.ActionRange;
import com.enderio.machines.common.attachment.RangedActor;
import com.enderio.machines.common.blocks.base.blockentity.PoweredMachineBlockEntity;
import com.enderio.machines.common.blocks.base.blockentity.flags.CapacitorSupport;
import com.enderio.machines.common.blocks.base.inventory.SingleSlotAccess;
import com.enderio.machines.common.blocks.base.state.MachineState;
import com.enderio.machines.common.init.MachineAttachments;
import com.enderio.machines.common.init.MachineDataComponents;
import com.enderio.machines.common.io.IOConfig;
import com.enderio.machines.common.obelisk.ObeliskAreaManager;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.LogicalSide;

public abstract class ObeliskBlockEntity<T extends ObeliskBlockEntity<T>> extends PoweredMachineBlockEntity
        implements RangedActor {

    private @Nullable AABB aabb;
    public static SingleSlotAccess FILTER = new SingleSlotAccess();

    private static final ActionRange DEFAULT_RANGE = new ActionRange(5, false);

    private ActionRange actionRange = DEFAULT_RANGE;

    public ObeliskBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState,
            boolean isIoConfigMutable, CapacitorSupport capacitorSupport, EnergyIOMode energyIOMode,
            CapacitorScalable scalableEnergyCapacity, CapacitorScalable scalableMaxEnergyUse) {
        super(type, worldPosition, blockState, isIoConfigMutable, capacitorSupport, energyIOMode,
                scalableEnergyCapacity, scalableMaxEnergyUse);
    }

    @Nullable
    protected abstract ObeliskAreaManager<T> getAreaManager(ServerLevel level);

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (level instanceof ServerLevel serverLevel) {
            var manager = getAreaManager(serverLevel);
            if (manager != null) {
                // noinspection unchecked
                manager.register((T) this);
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (level instanceof ServerLevel serverLevel) {
            var manager = getAreaManager(serverLevel);
            if (manager != null) {
                // noinspection unchecked
                manager.unregister((T) this);
            }
        }
    }

    protected void updateLocations() {
        aabb = new AABB(getBlockPos()).inflate(getRange());

        if (level instanceof ServerLevel serverLevel) {
            var manager = getAreaManager(serverLevel);
            if (manager != null) {
                // noinspection unchecked
                manager.update((T) this);
            }
        }
    }

    @Override
    public boolean isActive() {
        return canAct();
    }

    @Override
    public ActionRange getActionRange() {
        return actionRange;
    }

    @Override
    @UseOnly(LogicalSide.SERVER)
    public void setActionRange(ActionRange actionRange) {
        this.actionRange = actionRange.clamp(0, getMaxRange());
        updateLocations();
        setChanged();

        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void updateCapacitorData() {
        if(level == null || level.isClientSide()) {
            super.updateCapacitorData();
            return;
        }

        int oldMaxRange = getMaxRange();
        super.updateCapacitorData();
        int newMaxRange = getMaxRange();
        if(newMaxRange != oldMaxRange) {
            int newRange = getRange();
            if(newRange <= 0) {
                newRange = newMaxRange;
            } else {
                newRange = Math.min(getRange(), newMaxRange);
            }
            setActionRange(new ActionRange(newRange, getActionRange().isVisible()));
        }
    }

    @Override
    public void serverTick() {
        updateMachineState(MachineState.ACTIVE, isActive()); // No powered model state, so it needs to be done manually
        super.serverTick();
        if(canAct()) {
            int amount = (int)Math.ceil(getMaxEnergyUse() * ((float)getRange()/getMaxRange()));
            getEnergyStorage().consumeEnergy(amount);
        }
    }

    @Override
    public void clientTick() {
        if (level instanceof ClientLevel clientLevel) {
            getActionRange().addClientParticle(clientLevel, getBlockPos(), getColor());
        }

        super.clientTick();
    }

    public abstract String getColor();

    public @Nullable AABB getAABB() {
        return aabb;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateLocations();
    }

    @Override
    public IOConfig getDefaultIOConfig() {
        return IOConfig.of(IOMode.PULL);
    }

    @Override
    protected void saveAdditionalSynced(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditionalSynced(tag, registries);

        if (!actionRange.equals(DEFAULT_RANGE)) {
            tag.put(MachineNBTKeys.ACTION_RANGE, actionRange.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        // TODO: Ender IO 8 - remove support for old attachment loading
        if (hasData(MachineAttachments.ACTION_RANGE)) {
            actionRange = getData(MachineAttachments.ACTION_RANGE);
            removeData(MachineAttachments.ACTION_RANGE);
        } else if (tag.contains(MachineNBTKeys.ACTION_RANGE)) {
            actionRange = ActionRange.parse(registries, Objects.requireNonNull(tag.get(MachineNBTKeys.ACTION_RANGE)));
        } else {
            actionRange = DEFAULT_RANGE;
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);

        var actionRange = components.get(MachineDataComponents.ACTION_RANGE);
        if (actionRange != null) {
            this.actionRange = actionRange;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(MachineDataComponents.ACTION_RANGE, actionRange);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove(MachineNBTKeys.ACTION_RANGE);
    }
}
