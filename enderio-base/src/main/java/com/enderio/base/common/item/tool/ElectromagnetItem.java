package com.enderio.base.common.item.tool;

import com.enderio.base.common.config.BaseConfig;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.base.common.util.AttractionUtil;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ElectromagnetItem extends PoweredToggledItem {

    private static final double COLLISION_DISTANCE_SQ = 1.25 * 1.25;
    private static final double SPEED = 0.035;
    private static final double SPEED_4 = SPEED * 4;

    public ElectromagnetItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected int getEnergyUse() {
        return BaseConfig.COMMON.ITEMS.ELECTROMAGNET_ENERGY_USE.get();
    }

    @Override
    public int getMaxEnergy() {
        return BaseConfig.COMMON.ITEMS.ELECTROMAGNET_MAX_ENERGY.get();
    }

    private int getRange() {
        return BaseConfig.COMMON.ITEMS.ELECTROMAGNET_RANGE.get();
    }

    private int getMaxItems() {
        return BaseConfig.COMMON.ITEMS.ELECTROMAGNET_MAX_ITEMS.get();
    }

    private boolean isBlacklisted(ItemEntity entity) {
        return entity.getItem().is(EIOTags.Items.ELECTROMAGNET_BLACKLIST);
    }

    private boolean isMagnetable(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            return !isBlacklisted(itemEntity);
        }
        return entity instanceof ExperienceOrb;
    }

    @Override
    protected void onTickWhenActive(Player player, ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId,
            boolean pIsSelected) {
        if (player.isSpectator()) {
            return;
        }

        int range = getRange();
        AABB bounds = new AABB(player.getX() - range, player.getY() - range, player.getZ() - range,
                player.getX() + range, player.getY() + range, player.getZ() + range);

        List<Entity> toMove = pLevel.getEntities(player, bounds, this::isMagnetable);

        int itemsRemaining = getMaxItems();
        if (itemsRemaining <= 0) {
            itemsRemaining = Integer.MAX_VALUE;
        }

        for (Entity entity : toMove) {
            if (AttractionUtil.moveToPos(pEntity, player.getX(), player.getY() + player.getEyeHeight() * .75f,
                    player.getZ(), SPEED, SPEED_4, COLLISION_DISTANCE_SQ)) {
                entity.playerTouch(player);

            }

            if (itemsRemaining-- <= 0) {
                return;
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem();
    }
}
