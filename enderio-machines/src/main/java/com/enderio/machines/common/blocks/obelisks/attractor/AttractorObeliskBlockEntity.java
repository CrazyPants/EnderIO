package com.enderio.machines.common.blocks.obelisks.attractor;

import com.enderio.base.api.capacitor.CapacitorModifier;
import com.enderio.base.api.capacitor.QuadraticScalable;
import com.enderio.base.api.filter.EntityFilter;
import com.enderio.base.api.filter.ResourceFilter;
import com.enderio.base.api.io.energy.EnergyIOMode;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.machines.common.blocks.base.blockentity.flags.CapacitorSupport;
import com.enderio.machines.common.blocks.base.inventory.MachineInventoryLayout;
import com.enderio.machines.common.blocks.obelisks.ObeliskBlockEntity;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.init.MachineBlockEntities;
import com.enderio.machines.common.obelisk.ObeliskAreaManager;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AttractorObeliskBlockEntity extends ObeliskBlockEntity<AttractorObeliskBlockEntity> {

    private static final QuadraticScalable ENERGY_CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY,
            MachinesConfig.COMMON.ENERGY.ATTRACTOR_CAPACITY);
    private static final QuadraticScalable ENERGY_USAGE = new QuadraticScalable(CapacitorModifier.ENERGY_USE,
            MachinesConfig.COMMON.ENERGY.ATTRACTOR_USAGE);

    private Vec3 targetPos = new Vec3(0, 0, 0);

    public AttractorObeliskBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(MachineBlockEntities.ATTRACTOR_OBELISK.get(), worldPosition, blockState, false, CapacitorSupport.REQUIRED,
                EnergyIOMode.Input, ENERGY_CAPACITY, ENERGY_USAGE);
    }

    @Override
    protected @Nullable ObeliskAreaManager<AttractorObeliskBlockEntity> getAreaManager(ServerLevel level) {
        return null;
    }

    @Override
    public @Nullable MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder()
                .inputSlot((integer,
                        itemStack) -> itemStack.getCapability(EIOCapabilities.Filter.ITEM) instanceof EntityFilter)
                .slotAccess(FILTER)
                .capacitor()
                .build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AttractorObeliskMenu(containerId, pPlayerInventory, this);
    }

    @Override
    public int getMaxRange() {
        return 32;
    }

    @Override
    public String getColor() {
        return MachinesConfig.CLIENT.BLOCKS.ATTRACTOR_RANGE_COLOR.get();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel) {
            targetPos = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
        }
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (level == null) {
            return;
        }
        AABB bnds = getAABB();
        if (bnds == null) {
            return;
        }
        ItemStack filterStack = FILTER.getItemStack(this);
        @Nullable
        ResourceFilter cap = filterStack.getCapability(EIOCapabilities.Filter.ITEM);
        if (!(cap instanceof EntityFilter filter)) {
            return;
        }

        List<LivingEntity> filteredEntities = level.getEntities(EntityTypeTest.forClass(LivingEntity.class), bnds,
                filter);
//        List<LivingEntity> filteredEntities = level.getEntities(EntityTypeTest.forClass(LivingEntity.class), bnds, livingEntity -> true);
        for (LivingEntity ent : filteredEntities) {
            if (ent instanceof PathfinderMob mob) {
                attractMob(mob);
            } else if (!(ent instanceof Player)) {
                System.out.println("AttractorObeliskBlockEntity.serverTick: Couldn't do: " + ent);
            }
        }
    }

    public void attractMob(PathfinderMob mob) {
        float speed = 1.0F;
        mob.goalSelector.enableControlFlag(Goal.Flag.MOVE);
        Vec3 moveOffset = targetPos.subtract(mob.getX(), mob.getY(), mob.getZ());
        // keep them 1 block away
        moveOffset = moveOffset.subtract(moveOffset.normalize());
        mob.getNavigation()
                .moveTo(mob.getX() + moveOffset.x, mob.getY() + moveOffset.y, mob.getZ() + moveOffset.z, speed);
    }

    private void directPull(LivingEntity ent) {
        Vec3 entPos = ent.getPosition(0);
        Vec3 myPos = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5);
        Vec3 dir = myPos.subtract(entPos).normalize();
        dir = dir.scale(0.1);
        ent.setDeltaMovement(dir);
    }

}
