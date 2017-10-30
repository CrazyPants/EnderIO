package crazypants.enderio.machine.enchanter;

import static crazypants.enderio.machine.MachineObject.blockEnchanter;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IResourceTooltipProvider;

import crazypants.enderio.GuiID;
import crazypants.enderio.init.IModObject;
import crazypants.enderio.machine.MachineObject;
import crazypants.enderio.machine.base.block.AbstractMachineBlock;
import crazypants.enderio.machine.render.RenderMappers;
import crazypants.enderio.render.IBlockStateWrapper;
import crazypants.enderio.render.IHaveTESR;
import crazypants.enderio.render.IRenderMapper;
import crazypants.enderio.render.ITESRItemBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockEnchanter extends AbstractMachineBlock<TileEnchanter> implements IGuiHandler, IResourceTooltipProvider, ITESRItemBlock, IHaveTESR {

  public static BlockEnchanter create(@Nonnull IModObject modObject) {
    BlockEnchanter res = new BlockEnchanter();
    res.init();
    return res;
  }

  protected BlockEnchanter() {
    super(MachineObject.blockEnchanter, TileEnchanter.class);
    setLightOpacity(0);
  }

  @Override
  protected GuiID getGuiId() {
    return GuiID.GUI_ID_ENCHANTER;
  }

  @Override
  public boolean isOpaqueCube(IBlockState bs) {
    return false;
  }

  @Override
  public boolean isFullCube(IBlockState bs) {
    return false;
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEnchanter tileEntity = getTileEntity(world, new BlockPos(x, y, z));
    if (tileEntity != null) {
      return new ContainerEnchanter(player, player.inventory, tileEntity);
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEnchanter tileEntity = getTileEntity(world, new BlockPos(x, y, z));
    if (tileEntity != null) {
      return new GuiEnchanter(player, player.inventory, tileEntity);
    }
    return null;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void bindTileEntitySpecialRenderer() {
    ClientRegistry.bindTileEntitySpecialRenderer(TileEnchanter.class, new EnchanterModelRenderer());
    ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(blockEnchanter.getBlock()), 0, TileEnchanter.class);
  }

  @Override
  protected void setBlockStateWrapperCache(@Nonnull IBlockStateWrapper blockStateWrapper, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
      @Nonnull TileEnchanter tileEntity) {
    blockStateWrapper.addCacheKey(tileEntity.getFacing());
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IRenderMapper.IItemRenderMapper getItemRenderMapper() {
    return RenderMappers.FRONT_MAPPER;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IRenderMapper.IBlockRenderMapper getBlockRenderMapper() {
    return RenderMappers.FRONT_MAPPER;
  }

}
