package crazypants.enderio.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import crazypants.enderio.EnderIO;
import crazypants.enderio.conduit.ConduitDisplayMode;
import crazypants.enderio.item.darksteel.DarkSteelController;
import crazypants.enderio.item.darksteel.GogglesOfRevealingUpgrade;
import crazypants.enderio.item.darksteel.JumpUpgrade;
import crazypants.enderio.item.darksteel.PacketUpgradeState;
import crazypants.enderio.item.darksteel.SoundDetector;
import crazypants.enderio.item.darksteel.SoundDetectorUpgrade;
import crazypants.enderio.item.darksteel.SpeedUpgrade;
import crazypants.enderio.network.PacketHandler;
import crazypants.util.Lang;

public class KeyTracker {

  public static KeyTracker instance = new KeyTracker();
  
  static {
    FMLCommonHandler.instance().bus().register(instance);
  }
  
  private KeyBinding glideKey;  
  private boolean isGlideActive = false;
  
  private KeyBinding soundDetectorKey;  
  private boolean isSoundDectorActive = false;
  
  private KeyBinding nightVisionKey;  
  private boolean isNightVisionActive = false;
  
  private KeyBinding stepAssistKey;  
  private boolean isStepAssistActive = true;
  
  private KeyBinding speedKey;  
  private boolean isSpeedActive = true;
  
  
  private KeyBinding gogglesKey;  
  
  private KeyBinding yetaWrenchMode;  
  
  
  public KeyTracker() {
    glideKey = new KeyBinding("Glider Toggle", Keyboard.KEY_G, "Dark Steel Armor");
    ClientRegistry.registerKeyBinding(glideKey);
    soundDetectorKey = new KeyBinding("Sound Locator", Keyboard.KEY_L, "Dark Steel Armor");
    ClientRegistry.registerKeyBinding(soundDetectorKey);        
    nightVisionKey = new KeyBinding("Night Vision", Keyboard.KEY_P, "Dark Steel Armor");
    ClientRegistry.registerKeyBinding(nightVisionKey);
    gogglesKey = new KeyBinding("Goggles of Revealing", Keyboard.KEY_R, "Dark Steel Armor");
    ClientRegistry.registerKeyBinding(gogglesKey);
    
    stepAssistKey = new KeyBinding("Step Assist", Keyboard.KEY_NONE, "Dark Steel Armor");
    ClientRegistry.registerKeyBinding(stepAssistKey);
    
    speedKey = new KeyBinding("Speed", Keyboard.KEY_NONE, "Dark Steel Armor");
    ClientRegistry.registerKeyBinding(speedKey);
    
    yetaWrenchMode = new KeyBinding("Yeta Wrench Mode", Keyboard.KEY_Y, "Tools");
    ClientRegistry.registerKeyBinding(yetaWrenchMode);
  }
  
  @SubscribeEvent
  public void onKeyInput(KeyInputEvent event) {   
    handleGlide();
    handleSoundDetector();
    handleNightVision();
    handleYetaWrench();
    handleGoggles();
    handleStepAssist();
    handleSpeed();
  }

  private void handleSpeed() {
    if(!SpeedUpgrade.isEquipped(Minecraft.getMinecraft().thePlayer)) {
      return;
    }
    if(speedKey.getIsKeyPressed()) {      
      isSpeedActive = !isSpeedActive;
      String message;
      if(isSpeedActive) {
        message = Lang.localize("darksteel.upgrade.speed.enabled");
      } else {
        message = Lang.localize("darksteel.upgrade.speed.disabled");
      }
      Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentTranslation(message));
      DarkSteelController.instance.setSpeedActive(Minecraft.getMinecraft().thePlayer, isSpeedActive);
      PacketHandler.INSTANCE.sendToServer(new PacketUpgradeState(PacketUpgradeState.Type.SPEED, isSpeedActive));
    }
  }

  private void handleStepAssist() {
    if(!JumpUpgrade.isEquipped(Minecraft.getMinecraft().thePlayer)) {
      return;
    }
    if(stepAssistKey.getIsKeyPressed()) {      
      isStepAssistActive = !isStepAssistActive;
      String message;
      if(isStepAssistActive) {
        message = Lang.localize("darksteel.upgrade.stepAssist.enabled");
      } else {
        message = Lang.localize("darksteel.upgrade.stepAssist.disabled");
      }
      Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentTranslation(message));
      DarkSteelController.instance.setStepAssistActive(Minecraft.getMinecraft().thePlayer, isStepAssistActive);
      PacketHandler.INSTANCE.sendToServer(new PacketUpgradeState(PacketUpgradeState.Type.STEP_ASSIST, isStepAssistActive));
    }
    
  }

  private void handleGoggles() {
    EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    if(!GogglesOfRevealingUpgrade.isUpgradeEquipped(player)){
      return;
    }
    if(gogglesKey.getIsKeyPressed()) {      
      EnderIO.itemDarkSteelHelmet.setGogglesUgradeActive(!EnderIO.itemDarkSteelHelmet.isGogglesUgradeActive());
    }
    
  }

  private void handleYetaWrench() {
    if(!yetaWrenchMode.isPressed()) {
      return;
    }
    EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
    ItemStack equipped = player.getCurrentEquippedItem();
    if(equipped == null) {
      return;
    }
    if(equipped.getItem() == EnderIO.itemYetaWench) {
      ConduitDisplayMode curMode = ConduitDisplayMode.getDisplayMode(equipped);
      if(curMode == null) {
        curMode = ConduitDisplayMode.ALL;
      }
      ConduitDisplayMode newMode = curMode.next();
      ConduitDisplayMode.setDisplayMode(equipped, newMode);
      PacketHandler.INSTANCE.sendToServer(new YetaWrenchPacketProcessor(player.inventory.currentItem, newMode));
    } else if(equipped.getItem() == EnderIO.itemConduitProbe) {
      
      int newMeta = equipped.getItemDamage() == 0 ? 1 : 0;
      equipped.setItemDamage(newMeta);
      PacketHandler.INSTANCE.sendToServer(new PacketConduitProbeMode());   
      player.swingItem();
      
    }
    
        
  }

  private void handleSoundDetector() {
    if(!isSoundDetectorUpgradeEquipped(Minecraft.getMinecraft().thePlayer)) {
      SoundDetector.instance.setEnabled(false);
      return;
    }
    if(soundDetectorKey.getIsKeyPressed()) {      
      isSoundDectorActive = !isSoundDectorActive;
      String message;
      if(isSoundDectorActive) {
        message = Lang.localize("darksteel.upgrade.sound.enabled");
      } else {
        message = Lang.localize("darksteel.upgrade.sound.disabled");
      }
      Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentTranslation(message));
      SoundDetector.instance.setEnabled(isSoundDectorActive);
    }
    
  }

  private void handleGlide() {
    if(!DarkSteelController.instance.isGliderUpgradeEquipped(Minecraft.getMinecraft().thePlayer)) {
      return;
    }
    if(glideKey.getIsKeyPressed()) {      
      isGlideActive = !isGlideActive;
      String message;
      if(isGlideActive) {
        message = Lang.localize("darksteel.upgrade.glider.enabled");
      } else {
        message = Lang.localize("darksteel.upgrade.glider.disabled");
      }
      Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentTranslation(message));
      DarkSteelController.instance.setGlideActive(Minecraft.getMinecraft().thePlayer, isGlideActive);
      PacketHandler.INSTANCE.sendToServer(new PacketUpgradeState(PacketUpgradeState.Type.GLIDE, isGlideActive));
    }
  }
  
  private void handleNightVision() {
    EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    if(!DarkSteelController.instance.isNightVisionUpgradeEquipped(player)){
      isNightVisionActive = false;
      return;
    }
    if(nightVisionKey.getIsKeyPressed()) {      
      isNightVisionActive = !isNightVisionActive;
      if(isNightVisionActive) {
        player.worldObj.playSound(player.posX, player.posY, player.posZ, EnderIO.MODID + ":ds.nightvision.on", 0.1f, player.worldObj.rand.nextFloat() * 0.4f - 0.2f + 1.0f, false);
      } else {
        player.worldObj.playSound(player.posX, player.posY, player.posZ, EnderIO.MODID + ":ds.nightvision.off", 0.1f, 1.0f, false);
      }
      DarkSteelController.instance.setNightVisionActive(isNightVisionActive);      
    }
  }

  public boolean isGlideActive() {
    return isGlideActive;
  }   
    
  public boolean isSoundDetectorUpgradeEquipped(EntityClientPlayerMP player) {
    ItemStack helmet = player.getEquipmentInSlot(4);
    SoundDetectorUpgrade upgrade = SoundDetectorUpgrade.loadFromItem(helmet);
    if(upgrade == null) {
      return false;
    }
    return true;
  }
  
  public KeyBinding getYetaWrenchMode() {
    return yetaWrenchMode;
  }
}
