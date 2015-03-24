package crazypants.enderio.teleport.telepad;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.CommonProxy;
import crazypants.enderio.EnderIO;
import crazypants.enderio.GuiHandler;
import crazypants.enderio.ModObject;
import crazypants.enderio.api.teleport.ITravelAccessable;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.teleport.ContainerTravelAccessable;
import crazypants.enderio.teleport.ContainerTravelAuth;
import crazypants.enderio.teleport.GuiTravelAuth;
import crazypants.enderio.teleport.anchor.BlockTravelAnchor;

public class BlockTelePad extends BlockTravelAnchor {

  @SideOnly(Side.CLIENT)
  private IIcon unconnected;
  @SideOnly(Side.CLIENT)
  private IIcon connected;
  @SideOnly(Side.CLIENT)
  IIcon animationIcon;
  
  public static int renderId;

  public static BlockTelePad create() {
    BlockTelePad ret = new BlockTelePad();
    PacketHandler.INSTANCE.registerMessage(PacketOpenServerGui.class, PacketOpenServerGui.class, PacketHandler.nextID(), Side.SERVER);
    PacketHandler.INSTANCE.registerMessage(PacketUpdateCoords.class, PacketUpdateCoords.class, PacketHandler.nextID(), Side.SERVER);
    PacketHandler.INSTANCE.registerMessage(PacketUpdateCoords.class, PacketUpdateCoords.class, PacketHandler.nextID(), Side.CLIENT);
    PacketHandler.INSTANCE.registerMessage(PacketTeleport.class, PacketTeleport.class, PacketHandler.nextID(), Side.SERVER);
    PacketHandler.INSTANCE.registerMessage(PacketTeleport.class, PacketTeleport.class, PacketHandler.nextID(), Side.CLIENT);
    ret.init();
    return ret;
  }

  protected BlockTelePad() {
    super(ModObject.blockTelePad.unlocalisedName, TileTelePad.class);
  }

  @Override
  protected void init() {
    super.init();
    CommonProxy.guiHandler.registerGuiHandler(GuiHandler.GUI_ID_TELEPAD, this);
    CommonProxy.guiHandler.registerGuiHandler(GuiHandler.GUI_ID_TELEPAD_TRAVEL, this);
  }

  @Override
  public void registerBlockIcons(IIconRegister iIconRegister) {
    unconnected = iIconRegister.registerIcon("enderio:blockTelePad");
    connected = iIconRegister.registerIcon("enderio:telePadModel");
    animationIcon = iIconRegister.registerIcon("enderio:telePadAnimation");
  }

  @Override
  public IIcon getIcon(IBlockAccess world, int x, int y, int z, int blockSide) {
    TileTelePad te = (TileTelePad) world.getTileEntity(x, y, z);
    if(te != null && te.inNetwork()) {
      return connected;
    }
    return unconnected;
  }

  @Override
  public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
    return unconnected;
  }

  @Override
  public int getRenderType() {
    return renderId;
  }

  @Override
  public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
    AxisAlignedBB bb = super.getSelectedBoundingBoxFromPool(world, x, y, z);
    TileTelePad te = (TileTelePad) world.getTileEntity(x, y, z);
    if(!te.inNetwork()) {
      return bb;
    }
    return te.getBoundingBox();
  }

  @Override
  public void onNeighborBlockChange(World world, int x, int y, int z, Block changedTo) {
    super.onNeighborBlockChange(world, x, y, z, changedTo);
    ((TileTelePad) world.getTileEntity(x, y, z)).updateRedstoneState();
  }

  @Override
  public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
    super.onNeighborChange(world, x, y, z, tileX, tileY, tileZ);
    ((TileTelePad) world.getTileEntity(x, y, z)).updateConnectedState(true);
  }

  @Override
  public boolean openGui(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
    TileEntity te = world.getTileEntity(x, y, z);
    if(te instanceof TileTelePad) {
      TileTelePad tp = (TileTelePad) te;
      if(tp.inNetwork()) {
        if(!tp.isMaster()) {
          TileTelePad master = tp.getMaster();
          return openGui(world, master.xCoord, master.yCoord, master.zCoord, entityPlayer, side);
        }
      } else {
        return false;
      }

      // from here out we know that we are connected and are the master
      if(tp.canBlockBeAccessed(entityPlayer)) {
        entityPlayer.openGui(EnderIO.instance, GuiHandler.GUI_ID_TELEPAD, world, x, y, z);
      } else {
        sendPrivateChatMessage(entityPlayer, tp.getPlacedBy());
      }
    }
    return true;
  }

  @Override
  public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack par6ItemStack) {
    super.onBlockPlacedBy(world, x, y, z, entity, par6ItemStack);
    ((TileTelePad) world.getTileEntity(x, y, z)).updateConnectedState(true);
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(x, y, z);
    if(te instanceof TileTelePad) {
      switch (ID) {
      case GuiHandler.GUI_ID_TELEPAD:
        return new ContainerTelePad(player.inventory);
      case GuiHandler.GUI_ID_TELEPAD_TRAVEL:
        return new ContainerTravelAccessable(player.inventory, (TileTelePad) te, world);
      default:
        return new ContainerTravelAuth(player.inventory);
      }
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(x, y, z);
    if(te instanceof TileTelePad) {
      switch (ID) {
      case GuiHandler.GUI_ID_TELEPAD:
        return new GuiTelePad(player.inventory, (TileTelePad) te, world);
      case GuiHandler.GUI_ID_TELEPAD_TRAVEL:
        return new GuiAugmentedTravelAccessible(player.inventory, (TileTelePad) te, world);
      default:
        return new GuiTravelAuth(player, (ITravelAccessable) te, world);
      }
    }
    return null;
  }

}
