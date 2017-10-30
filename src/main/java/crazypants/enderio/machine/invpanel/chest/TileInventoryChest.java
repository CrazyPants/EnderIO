package crazypants.enderio.machine.invpanel.chest;

import com.enderio.core.common.NBTAction;
import com.enderio.core.common.inventory.EnderInventory;
import com.enderio.core.common.inventory.InventorySlot;
import crazypants.enderio.machine.MachineObject;
import crazypants.enderio.capacitor.CapacitorKeyType;
import crazypants.enderio.capacitor.DefaultCapacitorKey;
import crazypants.enderio.capacitor.Scaler;
import crazypants.enderio.paint.IPaintable;
import crazypants.util.Prep;
import info.loenwind.autosave.annotations.Storable;
import info.loenwind.autosave.annotations.Store;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;

@Storable
public abstract class TileInventoryChest extends AbstractCapabilityPoweredMachineEntity implements IPaintable.IPaintableTileEntity {

  private static final Map<EnumChestSize, Class<? extends TileInventoryChest>> CLASSES = new EnumMap<EnumChestSize, Class<? extends TileInventoryChest>>(
      EnumChestSize.class);

  @Storable
  public static class Meta0 extends TileInventoryChest {
    public Meta0() {
      super(EnumChestSize.TINY);
    }
  }

  @Storable
  public static class Meta1 extends TileInventoryChest {
    public Meta1() {
      super(EnumChestSize.SMALL);
    }
  }

  @Storable
  public static class Meta2 extends TileInventoryChest {
    public Meta2() {
      super(EnumChestSize.MEDIUM);
    }
  }

  @Storable
  public static class Meta3 extends TileInventoryChest {
    public Meta3() {
      super(EnumChestSize.BIG);
    }
  }

  @Storable
  public static class Meta4 extends TileInventoryChest {
    public Meta4() {
      super(EnumChestSize.LARGE);
    }
  }

  @Storable
  public static class Meta5 extends TileInventoryChest {
    public Meta5() {
      super(EnumChestSize.HUGE);
    }
  }

  @Storable
  public static class Meta6 extends TileInventoryChest {
    public Meta6() {
      super(EnumChestSize.ENORMOUS);
    }
  }

  @Storable
  public static class Meta7 extends TileInventoryChest {
    public Meta7() {
      super(EnumChestSize.WAREHOUSE);
    }
  }

  @Storable
  public static class Meta8 extends TileInventoryChest {
    public Meta8() {
      super(EnumChestSize.WAREHOUSE13);
    }
  }

  public static void create() {
    CLASSES.put(EnumChestSize.TINY, Meta0.class);
    CLASSES.put(EnumChestSize.SMALL, Meta1.class);
    CLASSES.put(EnumChestSize.MEDIUM, Meta2.class);
    CLASSES.put(EnumChestSize.BIG, Meta3.class);
    CLASSES.put(EnumChestSize.LARGE, Meta4.class);
    CLASSES.put(EnumChestSize.HUGE, Meta5.class);
    CLASSES.put(EnumChestSize.ENORMOUS, Meta6.class);
    CLASSES.put(EnumChestSize.WAREHOUSE, Meta7.class);
    CLASSES.put(EnumChestSize.WAREHOUSE13, Meta8.class);

    for (EnumChestSize size : EnumChestSize.values()) {
      GameRegistry.registerTileEntity(CLASSES.get(size), MachineObject.blockInventoryChest.getUnlocalisedName() + size.getName() + "TileEntity");
    }
  }

  public static TileInventoryChest create(EnumChestSize size) {
    try {
      return CLASSES.get(size).newInstance();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private final EnumChestSize size;

  @Store({ NBTAction.SAVE, NBTAction.ITEM })
  private final EnderInventory chestInventory;

  // called by our block
  private TileInventoryChest(EnumChestSize size) {
    super(new EnderInventory(),
        new DefaultCapacitorKey(MachineObject.blockInventoryChest, CapacitorKeyType.ENERGY_INTAKE, Scaler.Factory.POWER, 10),
        new DefaultCapacitorKey(MachineObject.blockInventoryChest, CapacitorKeyType.ENERGY_BUFFER, Scaler.Factory.POWER, 100000),
        new DefaultCapacitorKey(MachineObject.blockInventoryChest, CapacitorKeyType.ENERGY_USE, Scaler.Factory.POWER, 1)
    );
    chestInventory = getInventory();
    this.size = size;
    for (int i = 0; i < size.getSlots(); i++) {
      getInventory().add(EnderInventory.Type.INOUT, "slot" + i, new InventorySlot());
    }
  }

  @Override
  public @Nonnull String getMachineName() {
    return MachineObject.blockInventoryChest.getUnlocalisedName();
  }

  @Override
  public boolean isActive() {
    return hasPower();
  }

  private boolean lastState = false;

  @Override
  protected boolean processTasks(boolean redstoneCheck) {
    getEnergy().useEnergy();
    if (lastState != hasPower()) {
      lastState = hasPower();
      return true;
    }
    return false;
  }

  public int getComparatorInputOverride() {
    if (size == null) {
      return 0;
    }
    int count = 0;
    for (InventorySlot slot : getInventory().getView(EnderInventory.Type.INOUT)) {
      if (Prep.isValid(slot.getStackInSlot(0))) {
        count++;
      }
    }
    return count == 0 ? 0 : (14 * count / size.getSlots() + 1);
  }

  @Override
  public boolean hasCapability(Capability<?> capability, EnumFacing facingIn) {
    if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !hasPower()) {
      return false;
    }
    return super.hasCapability(capability, facingIn);
  }

  @Override
  public <T> T getCapability(Capability<T> capability, EnumFacing facingIn) {
    if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !hasPower()) {
      return null;
    }
    return super.getCapability(capability, facingIn);
  }

}
