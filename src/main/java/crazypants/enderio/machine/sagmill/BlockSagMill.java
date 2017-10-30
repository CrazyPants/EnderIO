package crazypants.enderio.machine.sagmill;

import java.util.Random;

import javax.annotation.Nonnull;

import crazypants.enderio.GuiID;
import crazypants.enderio.init.IModObject;
import crazypants.enderio.machine.MachineObject;
import crazypants.enderio.machine.base.block.AbstractMachineBlock;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.paint.IPaintable;
import crazypants.enderio.render.IBlockStateWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class BlockSagMill extends AbstractMachineBlock<TileSagMill> implements IPaintable.ISolidBlockPaintableBlock, IPaintable.IWrenchHideablePaint {

  public static BlockSagMill create(@Nonnull IModObject modObject) {
    PacketHandler.INSTANCE.registerMessage(PacketGrindingBall.class, PacketGrindingBall.class, PacketHandler.nextID(), Side.CLIENT);

    BlockSagMill res = new BlockSagMill(modObject);
    res.init();
    return res;
  }

  private BlockSagMill(@Nonnull IModObject modObject) {
    super(modObject, TileSagMill.class);
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if (te instanceof TileSagMill) {
      return new ContainerSagMill(player.inventory, (TileSagMill) te);
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if (te instanceof TileSagMill) {
      return new GuiSagMill(player.inventory, (TileSagMill) te);
    }
    return null;
  }

  @Override
  protected GuiID getGuiId() {
    return GuiID.GUI_ID_CRUSHER;
  }

  @Override
  public void randomDisplayTick(IBlockState bs, World world, BlockPos pos, Random rand) {

    TileSagMill te = (TileSagMill) world.getTileEntity(pos);
    if (te != null && te.isActive()) {
      EnumFacing front = te.facing;

      for (int i = 0; i < 3; i++) {
        double px = pos.getX() + 0.5 + front.getFrontOffsetX() * 0.51;
        double pz = pos.getZ() + 0.5 + front.getFrontOffsetZ() * 0.51;
        double py = pos.getY() + world.rand.nextFloat() * 0.8f + 0.1f;
        double v = 0.05;
        double vx = 0;
        double vz = 0;

        if (front == EnumFacing.NORTH || front == EnumFacing.SOUTH) {
          px += world.rand.nextFloat() * 0.8 - 0.4;
          vz += front == EnumFacing.NORTH ? -v : v;
        } else {
          pz += world.rand.nextFloat() * 0.8 - 0.4;
          vx += front == EnumFacing.WEST ? -v : v;
        }

        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, py, pz, vx, 0, vz);
      }
    }
  }

  @Override
  protected void setBlockStateWrapperCache(@Nonnull IBlockStateWrapper blockStateWrapper, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
      @Nonnull TileSagMill tileEntity) {
    blockStateWrapper.addCacheKey(tileEntity.getFacing()).addCacheKey(tileEntity.isActive());
  }

}
