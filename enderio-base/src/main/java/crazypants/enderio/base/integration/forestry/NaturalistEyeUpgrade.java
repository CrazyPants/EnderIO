package crazypants.enderio.base.integration.forestry;

import javax.annotation.Nonnull;

import crazypants.enderio.base.config.Config;
import crazypants.enderio.base.handler.darksteel.AbstractUpgrade;
import crazypants.enderio.base.init.ModObject;
import crazypants.enderio.util.Prep;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class NaturalistEyeUpgrade extends AbstractUpgrade {

  private static final @Nonnull String UPGRADE_NAME = "naturalistEye";

  public static final @Nonnull NaturalistEyeUpgrade INSTANCE = new NaturalistEyeUpgrade();

  public static @Nonnull ItemStack getNaturalistEye() {
    Item i = Item.REGISTRY.getObject(new ResourceLocation("Forestry", "naturalistHelmet"));
    if (i != null) {
      return new ItemStack(i);
    }
    return Prep.getEmpty();
  }

  public static boolean isUpgradeEquipped(@Nonnull EntityLivingBase player) {
    return NaturalistEyeUpgrade.INSTANCE.hasUpgrade(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
  }

  public NaturalistEyeUpgrade() {
    super(UPGRADE_NAME, "enderio.darksteel.upgrade.naturalistEye", getNaturalistEye(), Config.darkSteelApiaristArmorCost);
  }

  @Override
  public boolean canAddToItem(@Nonnull ItemStack stack) {
    if (stack.getItem() != ModObject.itemDarkSteelHelmet.getItem() || Prep.isInvalid(getUpgradeItem())) {
      return false;
    }
    return !hasUpgrade(stack);
  }

  @Override
  public @Nonnull ItemStack getUpgradeItem() {
    if (Prep.isValid(upgradeItem)) {
      return upgradeItem;
    }
    upgradeItem = getNaturalistEye();
    return upgradeItem;
  }

  @Override
  public @Nonnull String getUpgradeItemName() {
    if (Prep.isInvalid(getUpgradeItem())) {
      return "Naturalist Helmet";
    }
    return super.getUpgradeItemName();
  }

}
