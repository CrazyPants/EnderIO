package crazypants.enderio.machines.machine.teleport.telepad;

import static crazypants.enderio.base.init.ModObject.itemLocationPrintout;

import java.util.Collections;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.fluid.SmartTank;
import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NullHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import crazypants.enderio.api.teleport.ITelePad;
import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.base.capacitor.CapacitorKeyType;
import crazypants.enderio.base.capacitor.DefaultCapacitorData;
import crazypants.enderio.base.capacitor.DefaultCapacitorKey;
import crazypants.enderio.base.capacitor.ICapacitorData;
import crazypants.enderio.base.capacitor.ICapacitorKey;
import crazypants.enderio.base.capacitor.Scaler;
import crazypants.enderio.base.config.Config;
import crazypants.enderio.base.fluid.Fluids;
import crazypants.enderio.base.item.coordselector.TelepadTarget;
import crazypants.enderio.base.machine.base.te.AbstractMachineEntity;
import crazypants.enderio.base.machine.baselegacy.PacketLegacyPowerStorage;
import crazypants.enderio.base.machine.sound.MachineSound;
import crazypants.enderio.base.power.ILegacyPowerReceiver;
import crazypants.enderio.base.teleport.TeleportUtil;
import crazypants.enderio.machines.init.MachineObject;
import crazypants.enderio.machines.lang.Lang;
import crazypants.enderio.machines.machine.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.machines.machine.teleport.telepad.packet.PacketFluidLevel;
import crazypants.enderio.machines.machine.teleport.telepad.packet.PacketSetTarget;
import crazypants.enderio.machines.machine.teleport.telepad.packet.PacketTeleport;
import crazypants.enderio.machines.machine.teleport.telepad.packet.PacketTeleportTrigger;
import crazypants.enderio.machines.machine.teleport.telepad.render.BlockType;
import crazypants.enderio.machines.network.PacketHandler;
import info.loenwind.autosave.annotations.Store;
import info.loenwind.autosave.handlers.minecraft.HandleItemStack.HandleItemStackNNList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public class TileTelePad extends TileTravelAnchor
    implements ILegacyPowerReceiver, ITelePad, IProgressTile, IItemHandlerModifiable, ITankAccess.IExtendedTankAccess {

  @Nonnull
  private ICapacitorData capacitorData = DefaultCapacitorData.BASIC_CAPACITOR;
  private final ICapacitorKey maxEnergyRecieved = new DefaultCapacitorKey(MachineObject.block_tele_pad, CapacitorKeyType.ENERGY_INTAKE, Scaler.Factory.POWER,
      Config.telepadEnergyUsePerTickRF);
  private final ICapacitorKey maxEnergyStored = new DefaultCapacitorKey(MachineObject.block_tele_pad, CapacitorKeyType.ENERGY_BUFFER, Scaler.Factory.POWER,
      Config.telepadEnergyBufferRF);
  private final ICapacitorKey maxEnergyUsed = new DefaultCapacitorKey(MachineObject.block_tele_pad, CapacitorKeyType.ENERGY_USE, Scaler.Factory.POWER,
      Config.telepadEnergyUsePerTickRF);

  @Store
  private int storedEnergyRF;

  private TileTelePad masterTile = null;

  private boolean coordsChanged = false;

  @Store
  private @Nonnull TelepadTarget target = new TelepadTarget(new BlockPos(0, 0, 0), Integer.MIN_VALUE);

  private int lastSyncPowerStored;

  private Queue<Entity> toTeleport = Queues.newArrayDeque();
  private int powerUsed;
  private int requiredPower;

  public static final @Nonnull ResourceLocation ACTIVE_RES = AbstractMachineEntity.getSoundFor("telepad.active");
  @SideOnly(Side.CLIENT)
  private MachineSound activeSound;

  @Store
  private boolean redstoneActivePrev;

  private final Fluid fluidType;

  @Store
  protected final @Nonnull SmartTank tank;

  private boolean tankDirty = false;

  // Used on non-ported TESR
  @Nonnull
  public static final String TELEPORTING_KEY = "eio:teleporting";
  @Nonnull
  public static final String PROGRESS_KEY = "teleportprogress";

  boolean wasBlocked = false;

  // Clientside rendering data
  public float[] bladeRots = new float[3];
  public float spinSpeed = 0;
  public float speedMult = 2.5f;

  @Store(handler = HandleItemStackNNList.class)
  protected NNList<ItemStack> inventory = new NNList<>(2, ItemStack.EMPTY);

  public TileTelePad() {
    Fluid fluid = null;
    if (Config.rodOfReturnFluidType != null) {
      fluid = FluidRegistry.getFluid(Config.telepadFluidType);
    }
    if (fluid == null) {
      fluid = Fluids.ENDER_DISTILLATION.getFluid();
    }
    fluidType = fluid;

    int tankCap = 0;
    if (Config.telepadFluidUse > 0) {
      tankCap = Config.telepadFluidUse * 10;
    }
    tank = new SmartTank(fluidType, tankCap);
    if (tankCap <= 0) {
      tank.setCanFill(false);
    }
    tank.setCanDrain(false);
    tank.setTileEntity(this);
  }

  public boolean isFluidEnabled() {
    return tank.getCapacity() > 0;
  }

  public boolean wasBlocked() {
    return wasBlocked;
  }

  public void setBlocked(boolean blocked) {
    wasBlocked = blocked;
  }

  @Override
  public boolean isMaster() {
    return BlockType.getType(getBlockMetadata()) == BlockType.MASTER;
  }

  @Override
  public TileTelePad getMaster() {
    if (BlockType.getType(getBlockMetadata()) == BlockType.MASTER) {
      return this;
    }
    BlockPos offset = BlockType.getType(getBlockMetadata()).getOffsetToMaster();
    if (offset == null) {
      return null;
    }
    BlockPos materPos = getPos().add(offset.getX(), offset.getY(), offset.getZ());
    if (!world.isBlockLoaded(materPos)) {
      return null;
    }
    TileEntity res = world.getTileEntity(materPos);
    if (res instanceof TileTelePad) {
      return (TileTelePad) res;
    }
    return null;
  }

  @Override
  public boolean inNetwork() {
    return getMaster() != null;
  }

  @Override
  public void doUpdate() {
    if (!isMaster()) {
      return;
    }

    if (target.getDimension() == Integer.MIN_VALUE) {
      target.setDimension(world.provider.getDimension());
    }

    if (world.isRemote) {
      updateEntityClient();
      return;
    }

    if (!inventory.get(0).isEmpty() && inventory.get(1).isEmpty()) {
      ItemStack stack = inventory.get(0);
      TelepadTarget newTarg = TelepadTarget.readFromNBT(stack);
      setTarget(newTarg);
      inventory.set(0, ItemStack.EMPTY);
      inventory.set(1, stack);
      markDirty();
    }

    if (tankDirty && shouldDoWorkThisTick(5)) {
      PacketHandler.sendToAllAround(new PacketFluidLevel(this), this);
      tankDirty = false;
    }

    if (active()) {
      if (powerUsed >= requiredPower) {
        teleport(toTeleport.poll());
        powerUsed = 0;
      } else {
        int usable = Math.min(Math.min(getUsage(), requiredPower), getEnergyStored());
        setEnergyStored(getEnergyStored() - usable);
        powerUsed += usable;
      }
      if (shouldDoWorkThisTick(5)) {
        updateQueuedEntities();
      }
    }

    boolean powerChanged = (lastSyncPowerStored != getEnergyStored() && shouldDoWorkThisTick(5));
    if (powerChanged) {
      lastSyncPowerStored = getEnergyStored();
      PacketHandler.sendToAllAround(new PacketLegacyPowerStorage(this), this);
    }
    if (coordsChanged) {
      coordsChanged = false;
      PacketHandler.sendToAllAround(new PacketSetTarget(this, target), this);
    }
  }

  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {
    updateRotations();
    if (activeSound != null) {
      activeSound.setPitch(MathHelper.clamp(0.5f + (spinSpeed / 1.5f), 0.5f, 2));
    }
    if (active()) {
      if (activeSound == null) {
        BlockPos p = getPos();
        FMLClientHandler.instance().getClient().getSoundHandler().playSound(activeSound = new MachineSound(ACTIVE_RES, p.getX(), p.getY(), p.getZ(), 0.3f, 1));
      }
      updateQueuedEntities();
    } else if (!active() && activeSound != null) {
      if (activeSound.getPitch() <= 0.5f) {
        activeSound.endPlaying();
        activeSound = null;
      }
    }
  }

  private void updateQueuedEntities() {
    if (world.isRemote) {
      if (active()) {
        getCurrentTarget().getEntityData().setFloat(PROGRESS_KEY, getProgress());
      }
    }
    List<Entity> toRemove = Lists.newArrayList();
    for (Entity e : toTeleport) {
      if (!isEntityInRange(e) || e.isDead) {
        toRemove.add(e);
      }
    }
    for (Entity e : toRemove) {
      dequeueTeleport(e, true);
    }
  }

  public void updateRedstoneState() {
    if (!inNetwork()) {
      return;
    }

    boolean redstone = isPoweredRedstone();
    if (!getMasterTile().redstoneActivePrev && redstone) {
      teleportAll();
    }
    getMasterTile().redstoneActivePrev = redstone;
  }

  public boolean isPainted() {
    return sourceBlock != null;
  }

  @Override
  public void invalidate() {
    super.invalidate();
    if (world.isRemote) {
      stopPlayingSound();
    }
  }

  @Override
  public void onChunkUnload() {
    super.onChunkUnload();
    if (world.isRemote) {
      stopPlayingSound();
    }
  }

  @SideOnly(Side.CLIENT)
  private void stopPlayingSound() {
    if (activeSound != null) {
      activeSound.endPlaying();
      activeSound = null;
    }
  }

  public int getPowerScaled(int scale) {
    return (int) ((((float) getEnergyStored()) / (getMaxEnergyStored())) * scale);
  }

  private int calculateTeleportPower() {
    if (world.provider.getDimension() == target.getDimension()) {
      int distance = (int) Math.ceil(pos.getDistance(target.getLocation().getX(), target.getLocation().getY(), target.getLocation().getZ()));
      double base = Math.log((0.005 * distance) + 1);
      requiredPower = (int) (base * Config.telepadPowerCoefficient);
    } else {
      requiredPower = Config.telepadPowerInterdimensional;
    }
    // Max out at the inter dim. value
    int res = MathHelper.clamp(requiredPower, 5000, Config.telepadPowerInterdimensional);
    return res;
  }

  public boolean active() {
    return !toTeleport.isEmpty();
  }

  public Entity getCurrentTarget() {
    return toTeleport.peek();
  }

  public @Nonnull AxisAlignedBB getBoundingBox() {
    BlockPos p = getPos();
    if (!inNetwork()) {
      return new AxisAlignedBB(p, p.offset(EnumFacing.UP).offset(EnumFacing.SOUTH).offset(EnumFacing.EAST));
    }
    p = getMaster().getLocation();
    return new AxisAlignedBB(p.getX() - 1, p.getY(), p.getZ() - 1, p.getX() + 2, p.getY() + 1, p.getZ() + 2);
  }

  @Override
  public @Nonnull AxisAlignedBB getRenderBoundingBox() {
    return getBoundingBox();
  }

  public void updateRotations() {
    if (active()) {
      spinSpeed = getProgress() * 2;
    } else {
      spinSpeed = Math.max(0, spinSpeed - 0.025f);
    }

    for (int i = 0; i < bladeRots.length; i++) {
      bladeRots[i] += spinSpeed * ((i * 2) + 20);
      bladeRots[i] %= 360;
    }
  }

  /* IProgressTile */

  @Override
  public float getProgress() {
    return ((float) powerUsed) / ((float) requiredPower);
  }

  @Override
  protected int getProgressUpdateFreq() {
    return 1;
  }

  @Override
  public void setProgress(float progress) {
    this.powerUsed = progress < 0 ? 0 : (int) ((requiredPower) * progress);
  }

  @Override
  public @Nonnull TileEntity getTileEntity() {
    return this;
  }

  @Override
  public int getX() {
    if (inNetwork()) {
      return getMasterTile().target.getX();
    }
    return target.getX();
  }

  @Override
  public int getY() {
    if (inNetwork()) {
      return getMasterTile().target.getY();
    }
    return target.getY();
  }

  @Override
  public int getZ() {
    if (inNetwork()) {
      return getMasterTile().target.getZ();
    }
    return target.getZ();
  }

  @Override
  public int getTargetDim() {
    if (inNetwork()) {
      return getMasterTile().target.getDimension();
    }
    return target.getDimension();
  }

  @Override
  public void setX(int x) {
    if (Config.telepadLockCoords) {
      return;
    }
    setTarget(getTarget().setX(x));
  }

  @Override
  public void setY(int y) {
    if (Config.telepadLockCoords) {
      return;
    }
    setTarget(getTarget().setY(y));
  }

  @Override
  public void setZ(int z) {
    if (Config.telepadLockCoords) {
      return;
    }
    setTarget(getTarget().setZ(z));
  }

  @Override
  public void setTargetDim(int dimID) {
    if (Config.telepadLockCoords) {
      return;
    }
    setTarget(getTarget().setDimension(dimID));
  }

  @Override
  public void setCoords(@Nonnull BlockPos coords) {
    if (Config.telepadLockCoords) {
      return;
    }
    setTarget(getTarget().setLocation(coords));
  }

  public void setTarget(TelepadTarget newTarget) {
    if (inNetwork() && !isMaster()) {
      getMaster().setTarget(newTarget);
      return;
    }
    if (newTarget == null) {
      newTarget = new TelepadTarget();
    }
    target = new TelepadTarget(newTarget);
    coordsChanged = true;
    markDirty();
  }

  public @Nonnull TelepadTarget getTarget() {
    if (!inNetwork() || isMaster()) {
      return target;
    }
    return getMaster().getTarget();
  }

  @Override
  public void teleportSpecific(@Nonnull Entity entity) {
    if (!inNetwork()) {
      return;
    }
    if (isMaster()) {
      if (isEntityInRange(entity)) {
        enqueueTeleport(entity, true);
      }
    } else {
      getMasterTile().teleportSpecific(entity);
    }
  }

  @Override
  public void teleportAll() {
    TileTelePad m = getMasterTile();
    if (m == null) {
      return;
    }
    if (m.world.isRemote) {
      PacketHandler.INSTANCE.sendToServer(new PacketTeleportTrigger(m));
    } else {
      for (Entity e : m.getEntitiesInRange()) {
        m.enqueueTeleport(e, true);
      }
    }
  }

  private @Nonnull List<Entity> getEntitiesInRange() {
    return world.getEntitiesWithinAABB(Entity.class, getRange());
  }

  private boolean isEntityInRange(Entity entity) {
    return getRange().isVecInside(new Vec3d(entity.posX, entity.posY, entity.posZ));
  }

  private @Nonnull AxisAlignedBB getRange() {
    BlockPos p = getPos();
    return new AxisAlignedBB(p.getX() - 1, p.getY(), p.getZ() - 1, p.getX() + 2, p.getY() + 3, p.getZ() + 2);
  }

  public void enqueueTeleport(Entity entity, boolean sendUpdate) {
    if (entity == null || toTeleport.contains(entity)) {
      return;
    }
    calculateTeleportPower();
    entity.getEntityData().setBoolean(TELEPORTING_KEY, true);
    toTeleport.add(entity);
    if (sendUpdate) {
      if (entity.world.isRemote) {
        // NOP
      } else {
        PacketHandler.INSTANCE.sendToAll(new PacketTeleport(PacketTeleport.Type.BEGIN, this, entity));
      }
    }
  }

  public void dequeueTeleport(Entity entity, boolean sendUpdate) {
    if (entity == null) {
      return;
    }
    toTeleport.remove(entity);
    entity.getEntityData().setBoolean(TELEPORTING_KEY, false);
    if (sendUpdate) {
      if (world.isRemote) {
        // NOP
      } else {
        PacketHandler.INSTANCE.sendToAll(new PacketTeleport(PacketTeleport.Type.END, this, entity));
      }
    }
    if (!active()) {
      powerUsed = 0;
    }
  }

  private boolean teleport(Entity entity) {
    if (requiredPower <= 0) {
      return false;
    }

    if (Config.telepadFluidUse > 0) {
      if (tank.getFluidAmount() < Config.telepadFluidUse) {
        tank.drain(Config.telepadFluidUse, true);
        if (entity instanceof EntityPlayer) {
          ((EntityPlayer) entity).sendMessage(Lang.GUI_TELEPAD_NOFLUID.toChatServer(new FluidStack(fluidType, 1).getLocalizedName()));
        }
        wasBlocked = true;
        return true;
      }
      tank.drainInternal(Config.telepadFluidUse, true);
    }

    entity.getEntityData().setBoolean(TELEPORTING_KEY, false);
    wasBlocked = !(entity.world.isRemote ? clientTeleport(entity) : serverTeleport(entity));
    PacketHandler.INSTANCE.sendToAll(new PacketTeleport(PacketTeleport.Type.TELEPORT, this, wasBlocked));
    if (entity instanceof EntityPlayer) {
      ((EntityPlayer) entity).closeScreen();
    }
    return !wasBlocked;
  }

  private boolean clientTeleport(@Nonnull Entity entity) {
    return TeleportUtil.checkClientTeleport(entity, target.getLocation(), target.getDimension(), TravelSource.TELEPAD);
  }

  private boolean serverTeleport(@Nonnull Entity entity) {
    dequeueTeleport(entity, true);
    return TeleportUtil.serverTeleport(entity, target.getLocation(), target.getDimension(), false, TravelSource.TELEPAD);
  }

  /* ITravelAccessable overrides */

  @Override
  public boolean canSeeBlock(@Nonnull EntityPlayer playerName) {
    return isMaster() && inNetwork();
  }

  /* IInternalPowerReceiver */

  @Override
  public int getMaxEnergyRecieved(EnumFacing dir) {
    return inNetwork() && getMasterTile() != null ? getMasterTile() == this ? maxEnergyRecieved.get(capacitorData) : getMasterTile().getMaxEnergyRecieved(dir)
        : 0;
  }

  @Override
  public int getMaxEnergyStored() {
    return inNetwork() && getMasterTile() != null ? getMasterTile() == this ? maxEnergyStored.get(capacitorData) : getMasterTile().getMaxEnergyStored() : 0;
  }

  @Override
  public boolean displayPower() {
    return inNetwork() && getMasterTile() != null;
  }

  @Override
  public int getEnergyStored() {
    return inNetwork() && getMasterTile() != null ? getMasterTile() == this ? storedEnergyRF : getMasterTile().getEnergyStored() : 0;
  }

  @Override
  public void setEnergyStored(int storedEnergy) {
    if (inNetwork() && getMasterTile() != null) {
      if (getMasterTile() == this) {
        storedEnergyRF = Math.min(getMaxEnergyStored(), storedEnergy);
      } else {
        getMasterTile().setEnergyStored(storedEnergy);
      }
    }
  }

  @Override
  public boolean canConnectEnergy(@Nonnull EnumFacing from) {
    return inNetwork() && getMasterTile() != null;
  }

  @Override
  public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
    if (!inNetwork()) {
      return 0;
    }
    int max = Math.max(0, Math.min(Math.min(getMaxEnergyRecieved(from), maxReceive), getMaxEnergyStored() - getEnergyStored()));
    if (!simulate) {
      setEnergyStored(getEnergyStored() + max);
    }
    return max;
  }

  public int getUsage() {
    return maxEnergyUsed.get(capacitorData);
  }

  private TileTelePad getMasterTile() {
    if (masterTile != null) {
      return masterTile;
    }

    masterTile = getMaster();
    return masterTile;
  }

  @Override
  public boolean shouldRenderInPass(int pass) {
    return true;
  }

  // Inventory

  @Override
  public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
    if (inNetwork() && (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)) {
      return true;
    }
    return super.hasCapability(capability, facing);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
    if (!inNetwork()) {
      return super.getCapability(capability, facing);
    }
    if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      return (T) getMaster();
    }
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      return (T) NullHelper.notnull(getMaster(), "Telepad master is null while in network!").tank;
    }
    return super.getCapability(capability, facing);
  }

  @Override
  public int getSlots() {
    return 2;
  }

  @Override
  public @Nonnull ItemStack getStackInSlot(int slot) {
    if (slot < 0 || slot >= inventory.size()) {
      return ItemStack.EMPTY;
    }
    return inventory.get(slot);
  }

  @Override
  public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
    if (slot < 0 || slot >= inventory.size()) {
      return;
    }
    inventory.set(slot, stack);
  }

  @Override
  public @Nonnull ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
    if (slot != 0 || !inventory.get(0).isEmpty() || stack.isEmpty() || stack.getItem() != itemLocationPrintout.getItem()) {
      return stack;
    }
    if (!simulate) {
      inventory.set(0, stack.copy());
      markDirty();
    }
    return ItemStack.EMPTY;
  }

  @Override
  public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (slot != 1 || amount < 1 || inventory.get(1).isEmpty()) {
      return ItemStack.EMPTY;
    }
    ItemStack res = inventory.get(1).copy();
    if (!simulate) {
      markDirty();
      inventory.set(1, ItemStack.EMPTY);
    }
    return res;
  }

  // Fluids

  @Override
  public FluidTank getInputTank(FluidStack forFluidType) {
    if (forFluidType == null || forFluidType.getFluid() != fluidType) {
      return null;
    }
    TileTelePad master = getMaster();
    if (master == null) {
      return null;
    }
    return master.tank;
  }

  @Override
  public @Nonnull FluidTank[] getOutputTanks() {
    return new FluidTank[0];
  }

  @Override
  public void setTanksDirty() {
    tankDirty = true;
    markDirty();
  }

  public int getFluidAmount() {
    return tank.getFluidAmount();
  }

  public void setFluidAmount(int level) {
    tank.setFluidAmount(level);
  }

  public @Nonnull FluidTank getTank() {
    return tank;
  }

  public Fluid getFluidType() {
    return fluidType;
  }

  @SuppressWarnings("null")
  @Override
  public @Nonnull List<ITankData> getTankDisplayData() {
    if (inNetwork()) {
      return getMaster().createDisplayData();
    }
    return Collections.emptyList();
  }

  @SuppressWarnings("null")
  private @Nonnull List<ITankData> createDisplayData() {
    ITankData data = new TankData();
    return Collections.singletonList(data);
  }

  private class TankData implements ITankData {

    @Override
    public @Nonnull EnumTankType getTankType() {
      return EnumTankType.INPUT;
    }

    @Override
    public FluidStack getContent() {
      return getTank().getFluid();
    }

    @Override
    public int getCapacity() {
      return tank.getCapacity();
    }

  }

  @Override
  public int getSlotLimit(int slot) {
    return 64;
  }

}
