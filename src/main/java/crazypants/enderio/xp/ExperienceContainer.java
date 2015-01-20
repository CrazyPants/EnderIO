package crazypants.enderio.xp;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import crazypants.enderio.EnderIO;

public class ExperienceContainer {

  private int experienceLevel;
  private float experience;
  private int experienceTotal;
  private boolean xpDirty;
  private final int maxXp;
  
  public ExperienceContainer() {
    this(Integer.MAX_VALUE);
  }
  
  public ExperienceContainer(int maxStored) {
    maxXp = maxStored;
  }
  
  public int getMaximumExperiance() {    
    return maxXp;
  }

  public int getExperienceLevel() {
    return experienceLevel;
  }

  public float getExperience() {
    return experience;
  }

  public int getExperienceTotal() {
    return experienceTotal;
  }

  public boolean isDirty() {
    return xpDirty;
  }
  
  public void setDirty(boolean isDirty) {
    xpDirty = isDirty;
  }
  
  public void set(ExperienceContainer xpCon) {
    experienceTotal = xpCon.experienceTotal;
    experienceLevel = xpCon.experienceLevel;
    experience = xpCon.experience;    
  }

  public int addExperience(int xpToAdd) {
    int j = maxXp - experienceTotal;
    if(xpToAdd > j) {
      xpToAdd = j;
    }

    experience += (float) xpToAdd / (float) getXpBarCapacity();
    experienceTotal += xpToAdd;
    for (; experience >= 1.0F; experience /= getXpBarCapacity()) {
      experience = (experience - 1.0F) * getXpBarCapacity();
      experienceLevel++;
    }
    xpDirty = true;
    return xpToAdd;
  }

  private int getXpBarCapacity() {
    return XpUtil.getXpBarCapacity(experienceLevel);
  }

  public int getXpBarScaled(int scale) {
    int result = (int) (experience * scale);
    return result;

  }

  public void givePlayerXp(EntityPlayer player, int levels) {
    for (int i = 0; i < levels && experienceTotal > 0; i++) {
      givePlayerXpLevel(player);
    }
  }

  public void givePlayerXpLevel(EntityPlayer player) {
    int currentXP = XpUtil.getPlayerXP(player);
    int nextLevelXP = XpUtil.getExperienceForLevel(player.experienceLevel + 1) + 1;
    int requiredXP = nextLevelXP - currentXP;

    requiredXP = Math.min(experienceTotal, requiredXP);
    player.addExperience(requiredXP);

    int newXp = experienceTotal - requiredXP;
    experience = 0;
    experienceLevel = 0;
    experienceTotal = 0;
    addExperience(newXp);
  }
  
    
  public void drainPlayerXpToReachContainerLevel(EntityPlayer player, int level) {    
    int targetXP = XpUtil.getExperienceForLevel(level);
    int requiredXP = targetXP - experienceTotal;
    if(requiredXP <= 0) {
      return;
    }
    int drainXP = Math.min(requiredXP, XpUtil.getPlayerXP(player));
    addExperience(drainXP);
    XpUtil.addPlayerXP(player, -drainXP);    
  }
  
  public void drainPlayerXpToReachPlayerLevel(EntityPlayer player, int level) {    
    int targetXP = XpUtil.getExperienceForLevel(level);
    int drainXP = XpUtil.getPlayerXP(player) - targetXP;
    if(drainXP <= 0) {
      return;
    }    
    drainXP = addExperience(drainXP);
    if(drainXP > 0) {
      XpUtil.addPlayerXP(player, -drainXP);
    }
  }
  
  public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
    if(resource == null || !canDrain(from, resource.getFluid())) {
      return null;
    }    
    return drain(from, resource.amount, doDrain);
  }

  
  public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
    if(EnderIO.fluidXpJuice == null) {
      return null;
    }
    int available = getFluidAmount();
    int canDrain = Math.min(available, maxDrain);
    if(doDrain) {      
      int newXp = experienceTotal - XpUtil.liquidToExperiance(canDrain);
      experience = 0;
      experienceLevel = 0;
      experienceTotal = 0;
      addExperience(newXp);      
    }        
    return new FluidStack(EnderIO.fluidXpJuice, canDrain);
  }

  public boolean canFill(ForgeDirection from, Fluid fluid) {
    return fluid != null && EnderIO.fluidXpJuice != null && fluid.getID() == EnderIO.fluidXpJuice.getID();
  }
  
  public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
    if(resource == null) {
      return 0;
    }
    if(resource.amount <= 0) {
      return 0;
    }
    if(!canFill(from, resource.getFluid())) {
      return 0;
    }
    //need to do these calcs in XP instead of fluid space to avoid type overflows
    int xp = XpUtil.liquidToExperiance(resource.amount);
    int xpSpace = getMaximumExperiance() - getExperienceTotal();
    int canFillXP = Math.min(xp, xpSpace);
    if(canFillXP <= 0) {
      return 0;
    }
    if(doFill) {
      addExperience(canFillXP);
    }
    return XpUtil.experienceToLiquid(canFillXP);
  }
  
  public boolean canDrain(ForgeDirection from, Fluid fluid) {
    return fluid != null && EnderIO.fluidXpJuice != null && fluid.getID() == EnderIO.fluidXpJuice.getID();
  }
  
  public FluidTankInfo[] getTankInfo(ForgeDirection from) {
    if(EnderIO.fluidXpJuice == null) {
      return new FluidTankInfo[0];
    }
    return new FluidTankInfo[] {
      new FluidTankInfo(new FluidStack(EnderIO.fluidXpJuice, getFluidAmount()), getMaxFluidAmount())  
    };
  }

  private int getMaxFluidAmount() {    
    if(maxXp == Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    return XpUtil.experienceToLiquid(maxXp);
  }

  private int getFluidAmount() {
   return XpUtil.experienceToLiquid(experienceTotal);
  }
  
  public void readFromNBT(NBTTagCompound nbtRoot) {
    experienceLevel = nbtRoot.getInteger("experienceLevel");
    experienceTotal = nbtRoot.getInteger("experienceTotal");
    experience = nbtRoot.getFloat("experience");
  }
  
  
  public void writeToNBT(NBTTagCompound nbtRoot) {   
    nbtRoot.setInteger("experienceLevel", experienceLevel);
    nbtRoot.setInteger("experienceTotal", experienceTotal);
    nbtRoot.setFloat("experience", experience);
  }
   
  public void toBytes(ByteBuf buf) {
    buf.writeInt(experienceTotal);
    buf.writeInt(experienceLevel);
    buf.writeFloat(experience);    
  }
  
  public void fromBytes(ByteBuf buf) {
    experienceTotal = buf.readInt();
    experienceLevel = buf.readInt();
    experience = buf.readFloat();
  }

}
