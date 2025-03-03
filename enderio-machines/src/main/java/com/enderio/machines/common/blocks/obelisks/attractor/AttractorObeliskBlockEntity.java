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
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class AttractorObeliskBlockEntity extends ObeliskBlockEntity<AttractorObeliskBlockEntity> {

    private static final QuadraticScalable ENERGY_CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY,
            MachinesConfig.COMMON.ENERGY.ATTRACTOR_CAPACITY);
    private static final QuadraticScalable ENERGY_USAGE = new QuadraticScalable(CapacitorModifier.ENERGY_USE,
            MachinesConfig.COMMON.ENERGY.ATTRACTOR_USAGE);

    private Vec3 targetPos = new Vec3(0, 0, 0);
    private GameProfile fakePlayerID;

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
        if (level instanceof ServerLevel sl) {
            targetPos = new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            fakePlayerID = new GameProfile(UUID.randomUUID(), "attractor" + targetPos);
        }
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if(canAct()) {
            doAttract();
        }
    }

    private void doAttract() {
        if (level == null) {
            return;
        }
        AABB aabb = getAABB();
        if (aabb == null) {
            return;
        }
        @Nullable
        ResourceFilter cap = FILTER.getItemStack(this).getCapability(EIOCapabilities.Filter.ITEM);
        if (!(cap instanceof EntityFilter filter)) {
            return;
        }
        float speed = 1.0F;
        List<Mob> filteredEntities = level.getEntities(EntityTypeTest.forClass(Mob.class), aabb, filter);
//        List<Mob> filteredEntities = level.getEntities(EntityTypeTest.forClass(Mob.class), aabb, ent -> true);
        for (Mob mob : filteredEntities) {
            if (mob instanceof WitherBoss) {
                mob.goalSelector.disableControlFlag(Goal.Flag.TARGET);
                mob.goalSelector.disableControlFlag(Goal.Flag.LOOK);
                mob.setTarget(null);
                directPull(mob, 2.25f);
            } else if(mob instanceof PathfinderMob) {
                attractMob(mob, speed);
            } else if (useTarget(mob)) {
                setTarget(mob);
            } else if (mob instanceof Ghast) {
                directPull(mob, speed);
            }
        }
    }

    private boolean useTarget(Mob mob) {
        return mob instanceof Slime || mob instanceof Phantom;
    }

    private void setTarget(Mob mob) {
        assert level != null;
        FakePlayer fakePlayer = FakePlayerFactory.get((ServerLevel) level, fakePlayerID);
        fakePlayer.setPos(targetPos.x, targetPos.y, targetPos.z);
        mob.setTarget(fakePlayer);
    }

    private void attractMob(Mob mob, float speed) {
        mob.goalSelector.enableControlFlag(Goal.Flag.MOVE);
        Vec3 moveOffset = targetPos.subtract(mob.getX(), mob.getY(), mob.getZ());
        // keep them 1 block away
        moveOffset = moveOffset.subtract(moveOffset.normalize());
        mob.getNavigation()
                .moveTo(mob.getX() + moveOffset.x, mob.getY() + moveOffset.y, mob.getZ() + moveOffset.z, speed);
    }

    private void directPull(LivingEntity mob, float speed) {
        Vec3 dir = targetPos.subtract( new Vec3(mob.xo, mob.yo, mob.zo)).normalize();
        dir = dir.scale(speed * 0.1);
        AABB aabb = mob.getBoundingBox();
        aabb.move(dir);
        if(level != null && level.noCollision(mob, aabb)) {
            mob.setDeltaMovement(dir);
        }
    }

}
