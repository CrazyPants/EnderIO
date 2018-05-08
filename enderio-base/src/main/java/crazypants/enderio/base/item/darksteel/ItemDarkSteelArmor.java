package crazypants.enderio.base.item.darksteel;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.IAdvancedTooltipProvider;
import com.enderio.core.client.handlers.SpecialTooltipHandler;
import com.enderio.core.common.transform.EnderCoreMethods.IElytraFlyingProvider;
import com.enderio.core.common.transform.EnderCoreMethods.IOverlayRenderAware;
import com.enderio.core.common.util.ItemUtil;
import com.enderio.core.common.util.NNMap;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.util.OreDictionaryHelper;
import com.google.common.collect.Multimap;

import crazypants.enderio.api.upgrades.IDarkSteelItem;
import crazypants.enderio.api.upgrades.IDarkSteelUpgrade;
import crazypants.enderio.api.upgrades.IHasPlayerRenderer;
import crazypants.enderio.api.upgrades.IRenderUpgrade;
import crazypants.enderio.base.EnderIO;
import crazypants.enderio.base.EnderIOTab;
import crazypants.enderio.base.config.Config;
import crazypants.enderio.base.handler.darksteel.DarkSteelController;
import crazypants.enderio.base.handler.darksteel.DarkSteelRecipeManager;
import crazypants.enderio.base.handler.darksteel.PacketUpgradeState;
import crazypants.enderio.base.handler.darksteel.PacketUpgradeState.Type;
import crazypants.enderio.base.init.IModObject;
import crazypants.enderio.base.init.ModObject;
import crazypants.enderio.base.integration.thaumcraft.GogglesOfRevealingUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.elytra.ElytraUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.energy.EnergyUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.energy.EnergyUpgrade.EnergyUpgradeHolder;
import crazypants.enderio.base.item.darksteel.upgrade.energy.EnergyUpgradeManager;
import crazypants.enderio.base.item.darksteel.upgrade.glider.GliderUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.nightvision.NightVisionUpgrade;
import crazypants.enderio.base.item.darksteel.upgrade.sound.SoundDetectorUpgrade;
import crazypants.enderio.base.lang.Lang;
import crazypants.enderio.base.material.alloy.Alloy;
import crazypants.enderio.base.network.PacketHandler;
import crazypants.enderio.base.paint.PaintUtil.IWithPaintName;
import crazypants.enderio.base.recipe.MachineRecipeRegistry;
import crazypants.enderio.base.recipe.painter.HelmetPainterTemplate;
import crazypants.enderio.base.render.itemoverlay.PowerBarOverlayRenderHelper;
import crazypants.enderio.util.Prep;
import forestry.api.apiculture.IArmorApiarist;
import forestry.api.core.IArmorNaturalist;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@InterfaceList({
    // @Interface(iface = "thaumcraft.api.items.IGoggles", modid = "Thaumcraft"),
    // @Interface(iface = "thaumcraft.api.items.IVisDiscountGear", modid = "Thaumcraft"),
    // @Interface(iface = "thaumcraft.api.items.IRevealer", modid = "Thaumcraft"),
    @Interface(iface = "forestry.api.apiculture.IArmorApiarist", modid = "forestry"),
    @Interface(iface = "forestry.api.core.IArmorNaturalist", modid = "forestry") })
public class ItemDarkSteelArmor extends ItemArmor implements ISpecialArmor, IAdvancedTooltipProvider, IDarkSteelItem, IOverlayRenderAware, IHasPlayerRenderer,
    IWithPaintName, IElytraFlyingProvider, IArmorApiarist, IArmorNaturalist {
  // IGoggles, IRevealer, IVisDiscountGear {

  public static ItemDarkSteelArmor createDarkSteelBoots(@Nonnull IModObject modObject) {
    return create(modObject, EntityEquipmentSlot.FEET);
  }

  public static ItemDarkSteelArmor createDarkSteelLeggings(@Nonnull IModObject modObject) {
    return create(modObject, EntityEquipmentSlot.LEGS);
  }

  public static ItemDarkSteelArmor createDarkSteelChestplate(@Nonnull IModObject modObject) {
    return create(modObject, EntityEquipmentSlot.CHEST);
  }

  public static ItemDarkSteelArmor createDarkSteelHelmet(@Nonnull IModObject modObject) {
    final ItemDarkSteelArmor helmet = create(modObject, EntityEquipmentSlot.HEAD);
    MachineRecipeRegistry.instance.registerRecipe(MachineRecipeRegistry.PAINTER, new HelmetPainterTemplate(helmet));
    return helmet;
  }

  public static @Nonnull ItemDarkSteelArmor create(@Nonnull IModObject modObject, @Nonnull EntityEquipmentSlot armorType) {
    return new ItemDarkSteelArmor(modObject, armorType);
  }

  public static final ArmorMaterial MATERIAL = createMaterial();

  private static ArmorMaterial createMaterial() {
    Class<?>[] params = new Class<?>[] { String.class, int.class, int[].class, int.class, SoundEvent.class, float.class };
    return EnumHelper.addEnum(ArmorMaterial.class, "darkSteel", params, "darkSteel", 35, new int[] { 2, 5, 6, 2 }, 15, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 1.0f);
  }

  boolean gogglesUgradeActive = true;

  public static int getPoweredProtectionIncrease(int armorType) {
    switch (armorType) {
    case 0:
      return 1;
    case 1:
      return 2;
    case 2:
    case 3:
      return 1;
    }
    return 0;
  }

  private final int powerPerDamagePoint;

  protected ItemDarkSteelArmor(@Nonnull IModObject modObject, @Nonnull EntityEquipmentSlot armorType) {
    super(MATERIAL, 0, armorType);
    setCreativeTab(EnderIOTab.tabEnderIOItems);
    modObject.apply(this);
    powerPerDamagePoint = Config.darkSteelPowerStorageBase / MATERIAL.getDurability(armorType);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> par3List) {
    if (isInCreativeTab(tab)) {
      @Nonnull
      ItemStack is = new ItemStack(this);
      par3List.add(is);

      is = new ItemStack(this);
      EnergyUpgrade.EMPOWERED_FOUR.addToItem(is, this);
      EnergyUpgradeManager.setPowerFull(is, this);

      Iterator<IDarkSteelUpgrade> iter = DarkSteelRecipeManager.recipeIterator();
      while (iter.hasNext()) {
        IDarkSteelUpgrade upgrade = iter.next();
        if (!(upgrade instanceof EnergyUpgrade || upgrade instanceof GliderUpgrade || upgrade instanceof ElytraUpgrade) && upgrade.canAddToItem(is, this)) {
          upgrade.addToItem(is, this);
        }
      }

      if (GliderUpgrade.INSTANCE.canAddToItem(is, this)) {
        ItemStack is2 = is.copy();
        GliderUpgrade.INSTANCE.addToItem(is2, this);
        par3List.add(is2);
        if (ElytraUpgrade.INSTANCE.canAddToItem(is, this)) {
          ItemStack is3 = is.copy();
          ElytraUpgrade.INSTANCE.addToItem(is3, this);
          par3List.add(is3);
        }
        return;
      }

      par3List.add(is);
    }
  }

  @Override
  public int getIngotsRequiredForFullRepair() {
    switch (armorType) {
    case HEAD:
      return 5;
    case CHEST:
      return 8;
    case LEGS:
      return 7;
    case FEET:
    default:
      return 4;
    }
  }

  @Override
  public void addCommonEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
    DarkSteelRecipeManager.addCommonTooltipEntries(itemstack, entityplayer, list, flag);
  }

  @Override
  public void addBasicEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
    DarkSteelRecipeManager.addBasicTooltipEntries(itemstack, entityplayer, list, flag);
  }

  @Override
  public void addDetailedEntries(@Nonnull ItemStack itemstack, @Nullable EntityPlayer entityplayer, @Nonnull List<String> list, boolean flag) {
    if (!SpecialTooltipHandler.showDurability(flag)) {
      list.add(ItemUtil.getDurabilityString(itemstack));
    }
    String str = EnergyUpgradeManager.getStoredEnergyString(itemstack);
    if (str != null) {
      list.add(str);
    }
    if (EnergyUpgradeManager.itemHasAnyPowerUpgrade(itemstack)) {
      list.addAll(Lang.DARK_STEEL_POWERED.getLines(TextFormatting.WHITE));
      if (itemstack.getItem() == ModObject.itemDarkSteelBoots.getItemNN()) {
        list.addAll(Lang.DARK_BOOTS_POWERED.getLines(TextFormatting.WHITE));
      }
    }
    DarkSteelRecipeManager.addAdvancedTooltipEntries(itemstack, entityplayer, list, flag);
  }

  @Override
  public String getArmorTexture(@Nonnull ItemStack itemStack, @Nonnull Entity entity, @Nonnull EntityEquipmentSlot slot, @Nonnull String layer) {
    if (armorType == EntityEquipmentSlot.LEGS || (armorType == EntityEquipmentSlot.HEAD && !NightVisionUpgrade.INSTANCE.hasUpgrade(itemStack)
        && !SoundDetectorUpgrade.INSTANCE.hasUpgrade(itemStack))) {
      // LEGS and HELMET without faceplate
      return EnderIO.DOMAIN + ":textures/models/armor/dark_steel_layer_2.png";
    }
    // BOOTS, HELMET with faceplate, CHEST
    return EnderIO.DOMAIN + ":textures/models/armor/dark_steel_layer_1.png";
  }

  @Override
  public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, int slot) {
    if (source.isUnblockable()) {
      return new ArmorProperties(0, 0, armor.getMaxDamage() + 1 - armor.getItemDamage());
    }
    double damageRatio = damageReduceAmount + (EnergyUpgradeManager.getEnergyStored(armor) > 0 ? getPoweredProtectionIncrease(3 - slot) : 0);
    damageRatio /= 25D;
    ArmorProperties ap = new ArmorProperties(0, damageRatio, armor.getMaxDamage() + 1 - armor.getItemDamage());
    return ap;
  }

  @Override
  public int getArmorDisplay(EntityPlayer player, @Nonnull ItemStack armor, int slot) {
    int powerBonus = EnergyUpgradeManager.getEnergyStored(armor) > 0 ? getPoweredProtectionIncrease(3 - slot) : 0;
    return getArmorMaterial().getDamageReductionAmount(armorType) + powerBonus;
  }

  private static final NNMap<EntityEquipmentSlot, UUID> ARMOR_MODIFIERS = new NNMap.Brutal<>();
  static {
    ARMOR_MODIFIERS.put(EntityEquipmentSlot.FEET, UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"));
    ARMOR_MODIFIERS.put(EntityEquipmentSlot.LEGS, UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"));
    ARMOR_MODIFIERS.put(EntityEquipmentSlot.CHEST, UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"));
    ARMOR_MODIFIERS.put(EntityEquipmentSlot.HEAD, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
  }

  @Override
  public @Nonnull Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot equipmentSlot, @Nonnull ItemStack stack) {
    Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

    if (equipmentSlot == this.armorType) {
      boolean isPowered = EnergyUpgradeManager.getEnergyStored(stack) > 0;
      if (isPowered) {
        int toughnessBonus = 1;
        multimap.removeAll(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName());
        multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(),
            new AttributeModifier(ARMOR_MODIFIERS.get(equipmentSlot), "Armor toughness", toughness + toughnessBonus, 0));
        int powerBonus = getPoweredProtectionIncrease(3 - equipmentSlot.getIndex());
        multimap.removeAll(SharedMonsterAttributes.ARMOR.getName());
        multimap.put(SharedMonsterAttributes.ARMOR.getName(),
            new AttributeModifier(ARMOR_MODIFIERS.get(equipmentSlot), "Armor modifier", damageReduceAmount + powerBonus, 0));
      }
    }

    return multimap;
  }

  @Override
  public void damageArmor(EntityLivingBase entity, @Nonnull ItemStack stack, DamageSource source, int damage, int slot) {
    EnergyUpgradeHolder eu = EnergyUpgradeManager.loadFromItem(stack);
    if (eu != null && eu.getUpgrade().isAbsorbDamageWithPower() && eu.getEnergy() > 0) {
      eu.extractEnergy(damage * powerPerDamagePoint, false);
      eu.writeToItem(stack, this);
    } else {
      stack.damageItem(damage, NullHelper.notnullF(entity, "damageArmor() needs an entity"));
    }
  }

  @Override
  public boolean getIsRepairable(@Nonnull ItemStack i1, @Nonnull ItemStack i2) {
    return false;
  }

  // TODO: Mod Thaumcraft - Should we re add goggles upgrade?
  //
  // @Override
  // @Method(modid = "Thaumcraft")
  // public boolean showNodes(ItemStack stack, EntityLivingBase player) {
  // if (stack.isEmpty() || !gogglesUgradeActive) {
  // return false;
  // }
  // return GogglesOfRevealingUpgrade.INSTANCE.hasUpgrade(stack);
  //
  // }
  //
  // @Override
  // @Method(modid = "Thaumcraft")
  // public boolean showIngamePopups(ItemStack stack, EntityLivingBase player) {
  // if (stack.isEmpty() || !gogglesUgradeActive) {
  // return false;
  // }
  // return GogglesOfRevealingUpgrade.INSTANCE.hasUpgrade(stack);
  // }
  //
  // @Override
  // @Method(modid = "Thaumcraft")
  // public int getVisDiscount(ItemStack stack, EntityPlayer player) {
  // if (!stack.isEmpty() || stack.getItem() != ModObject.itemDarkSteelHelmet.getItemNN()) {
  // return 0;
  // }
  // return GogglesOfRevealingUpgrade.isUpgradeEquipped(player) ? 5 : 0;
  // }

  public boolean isGogglesUgradeActive() {
    return gogglesUgradeActive;
  }

  public void setGogglesUgradeActive(boolean gogglesUgradeActive) {
    this.gogglesUgradeActive = gogglesUgradeActive;
  }

  @Override
  public void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition) {
    PowerBarOverlayRenderHelper.instance_upgradeable.render(stack, xPosition, yPosition);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public @Nonnull IRenderUpgrade getRender() {
    return armorType == EntityEquipmentSlot.HEAD ? PaintedHelmetLayer.instance : PaintedHelmetLayer.not_an_helmet;
  }

  @SuppressWarnings("null")
  @Override
  @SideOnly(Side.CLIENT)
  public ModelBiped getArmorModel(@Nonnull EntityLivingBase entityLiving, @Nonnull ItemStack itemStack, @Nonnull EntityEquipmentSlot armorSlot,
      @Nonnull ModelBiped _default) {
    if (armorType == EntityEquipmentSlot.HEAD && Prep.isValid(itemStack) && itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("DSPAINT")) {
      // Don't render the armor model of the helmet if it is painted. The paint will be rendered by the PaintedHelmetLayer.
      return new ModelBiped() {
        @Override
        public void render(@Nonnull Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        }
      };
    }
    return null;
  }

  @Override
  public String getPaintName(@Nonnull ItemStack itemStack) {
    final NBTTagCompound subCompound = itemStack.getSubCompound("DSPAINT");
    if (subCompound != null) {
      ItemStack paintSource = new ItemStack(subCompound);
      if (Prep.isValid(paintSource)) {
        return paintSource.getDisplayName();
      }
    }
    return null;
  }

  @Override
  public boolean isElytraFlying(@Nonnull EntityLivingBase entity, @Nonnull ItemStack itemstack, boolean shouldStop) {
    if (entity instanceof EntityPlayer && DarkSteelController.isElytraUpgradeEquipped(itemstack) && DarkSteelController.isElytraActive((EntityPlayer) entity)) {
      if (shouldStop && !entity.world.isRemote) {
        DarkSteelController.setActive((EntityPlayer) entity, Type.ELYTRA, false);
        PacketHandler.INSTANCE.sendToDimension(new PacketUpgradeState(Type.ELYTRA, false, entity.getEntityId()), entity.world.provider.getDimension());
      }
      return true;
    } else {
      return false;
    }
  }

  @ObjectHolder("enderiointegrationforestry:apiarist_armor_feet")
  public static final IDarkSteelUpgrade FORESTRY_FEET = null;
  @ObjectHolder("enderiointegrationforestry:apiarist_armor_legs")
  public static final IDarkSteelUpgrade FORESTRY_LEGS = null;
  @ObjectHolder("enderiointegrationforestry:apiarist_armor_chest")
  public static final IDarkSteelUpgrade FORESTRY_CHEST = null;
  @ObjectHolder("enderiointegrationforestry:apiarist_armor_head")
  public static final IDarkSteelUpgrade FORESTRY_HEAD = null;
  @ObjectHolder("enderiointegrationforestry:naturalist_eye")
  public static final IDarkSteelUpgrade FORESTRY_EYES = null;

  @Override
  @Method(modid = "forestry")
  public boolean canSeePollination(@Nonnull EntityPlayer player, @Nonnull ItemStack armor, boolean doSee) {
    if (armor.getItem() != ModObject.itemDarkSteelHelmet.getItemNN()) {
      return false;
    }
    return FORESTRY_EYES != null && FORESTRY_EYES.hasUpgrade(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
  }

  @Override
  @Method(modid = "forestry")
  public boolean protectEntity(@Nonnull EntityLivingBase entity, @Nonnull ItemStack armor, @Nullable String cause, boolean doProtect) {
    return (FORESTRY_HEAD != null && FORESTRY_HEAD.hasUpgrade(armor)) || (FORESTRY_CHEST != null && FORESTRY_CHEST.hasUpgrade(armor))
        || (FORESTRY_FEET != null && FORESTRY_FEET.hasUpgrade(armor)) || (FORESTRY_LEGS != null && FORESTRY_LEGS.hasUpgrade(armor));
  }

  @Override
  public boolean isItemForRepair(@Nonnull ItemStack right) {
    return OreDictionaryHelper.hasName(right, Alloy.DARK_STEEL.getOreIngot());
  }

  @Override
  public boolean isForSlot(@Nonnull EntityEquipmentSlot slot) {
    return slot == armorType;
  }

  @Override
  public boolean hasUpgradeCallbacks(@Nonnull IDarkSteelUpgrade upgrade) {
    return upgrade == FORESTRY_FEET || upgrade == FORESTRY_LEGS || upgrade == FORESTRY_CHEST || upgrade == FORESTRY_HEAD || upgrade == FORESTRY_EYES
        || upgrade == ElytraUpgrade.INSTANCE || upgrade == GogglesOfRevealingUpgrade.INSTANCE;
  }

}
