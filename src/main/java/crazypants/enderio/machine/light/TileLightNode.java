package crazypants.enderio.machine.light;

import static crazypants.enderio.machine.MachineObject.blockElectricLight;

import crazypants.enderio.TileEntityEio;
import info.loenwind.autosave.annotations.Storable;
import info.loenwind.autosave.annotations.Store;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

@Storable
public class TileLightNode extends TileEntityEio {

  @Store
  BlockPos parent;

  public TileElectricLight getParent() {
    if (world == null || parent == null) {
      return null;
    }
    TileEntity te = world.getTileEntity(parent);
    if(te instanceof TileElectricLight) {
      return (TileElectricLight) te;
    }
    return null;
  }

  public void checkParent() {
    if (hasWorld() && parent != null && world.isBlockLoaded(parent)) {
      if (world.getBlockState(parent).getBlock() != blockElectricLight.getBlock()) {
        world.setBlockToAir(pos);
      }
    }
  }

  public void onNeighbourChanged() {
    TileElectricLight p = getParent();
    if(p != null) {
      p.nodeNeighbourChanged(this);
    }
  }

  public void onBlockRemoved() {
    TileElectricLight p = getParent();
    if(p != null) {
      p.nodeRemoved(this);
    }
  }

  @Override
  public String toString() {
    return "TileLightNode [parent=" + parent + ",  pos=" + pos + ", tileEntityInvalid=" + tileEntityInvalid + "]";
  }

  public void setParentPos(BlockPos pos) {
    parent = pos.toImmutable();
  }

}
