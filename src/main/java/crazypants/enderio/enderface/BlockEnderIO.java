package crazypants.enderio.enderface;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraftforge.common.UsernameCache;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.BlockEio;
import crazypants.enderio.GuiHandler;
import crazypants.enderio.CommonProxy;
import crazypants.enderio.EnderIO;
import crazypants.enderio.ISidedGuiHandler;
import crazypants.enderio.Log;
import crazypants.enderio.ModObject;
import crazypants.enderio.api.teleport.ITravelAccessable;
import crazypants.enderio.enderface.te.MeProxy;
import crazypants.enderio.gui.IResourceTooltipProvider;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.teleport.anchor.BlockTravelAnchor;

public class BlockEnderIO extends BlockEio implements IResourceTooltipProvider {

  public static BlockEnderIO create() {

    CommonProxy.guiHandler.registerGuiHandler(GuiHandler.GUI_ID_ME_ACCESS_TERMINAL, new ISidedGuiHandler() {

      @Override
      public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        try {
          return MeProxy.createMeTerminalContainer(player, x, y, z, false);
        } catch (Exception e) {
          Log.warn("BlockEnderIO: Error occured creating the server gui element for an ME Terminal " + e);
        }
        return null;
      }

      @Override
      public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return MeProxy.instance.createTerminalGui(player, x, y, z);
      }

    });

    PacketHandler.INSTANCE.registerMessage(PacketOpenRemoteUi.class, PacketOpenRemoteUi.class, PacketHandler.nextID(), Side.SERVER);

    BlockEnderIO result = new BlockEnderIO();
    result.init();
    return result;
  }

  IIcon frameIcon;
  IIcon selectedOverlayIcon;
  IIcon highlightOverlayIcon;

  static int pass;

  private BlockEnderIO() {
    super(ModObject.blockEnderIo.unlocalisedName, TileEnderIO.class);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int getRenderBlockPass() {
    return 1;
  }

  @Override
  public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack item) {
    TileEntity te = world.getTileEntity(x, y, z);
    if(te instanceof TileEnderIO) {
      TileEnderIO eio = (TileEnderIO) te;
      eio.initUiPitch = -player.rotationPitch;
      eio.initUiYaw = -player.rotationYaw + 180;
      eio.lastUiPitch = eio.initUiPitch;
      eio.lastUiYaw = eio.initUiYaw;
      if(player instanceof EntityPlayer) {
        eio.setPlacedBy((EntityPlayer) player);
      }
      world.markBlockForUpdate(x, y, z);
    }
  }

  @Override
  public boolean openGui(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
    TileEntity te = world.getTileEntity(x, y, z);
    if(te instanceof ITravelAccessable) {
      ITravelAccessable ta = (ITravelAccessable) te;
      if(ta.canUiBeAccessed(entityPlayer)) {
        entityPlayer.openGui(EnderIO.instance, GuiHandler.GUI_ID_TRAVEL_ACCESSABLE, world, x, y, z);
      } else {
        BlockTravelAnchor.sendPrivateChatMessage(entityPlayer, ta.getPlacedBy());
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public int getRenderType() {
    return -1;
  }

  @Override
  public int getLightValue(IBlockAccess world, int x, int y, int z) {
    return 13;
  }

  @Override
  public int getLightOpacity() {
    return 100;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(IIconRegister iIconRegister) {
    super.registerBlockIcons(iIconRegister);
    frameIcon = iIconRegister.registerIcon("enderio:enderIOFrame");
    highlightOverlayIcon = iIconRegister.registerIcon("enderio:enderIOHighlight");
    selectedOverlayIcon = iIconRegister.registerIcon("enderio:enderIOSelected");
  }

  @Override
  public String getUnlocalizedNameForTooltip(ItemStack stack) {
    return getUnlocalizedName();
  }
}
