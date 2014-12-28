package crazypants.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class BlockCoord {

  public final int x;
  public final int y;
  public final int z;

  public BlockCoord(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public BlockCoord(TileEntity tile) {
    this(tile.xCoord, tile.yCoord, tile.zCoord);
  }
  
  public BlockCoord(Entity e) {
    this(MathHelper.floor_double(e.posX), MathHelper.floor_double(e.posY), MathHelper.floor_double(e.posZ));
  }

  public BlockCoord(BlockCoord bc) {
    x = bc.x;
    y = bc.y;
    z = bc.z;
  }

  public BlockCoord getLocation(ForgeDirection dir) {
    return new BlockCoord(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
  }

  public int distanceSquared(BlockCoord other) {
    int dx, dy, dz;
    dx = x - other.x;
    dy = y - other.y;
    dz = z - other.z;
    return (dx * dx + dy * dy + dz * dz);
  }
  
  public int distance(BlockCoord other) {
    double dsq = distanceSquared(other);    
    return (int)Math.ceil(Math.sqrt(dsq)); 
  }
  
  public Block getBlock(World world) {
    return world.getBlock(x, y, z);
  }

  public TileEntity getTileEntity(World world) {
    return world.getTileEntity(x, y, z);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + x;
    result = prime * result + y;
    result = prime * result + z;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(getClass() != obj.getClass()) {
      return false;
    }
    BlockCoord other = (BlockCoord) obj;
    if(x != other.x) {
      return false;
    }
    if(y != other.y) {
      return false;
    }
    if(z != other.z) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "BlockCoord [x=" + x + ", y=" + y + ", z=" + z + "]";
  }

  public boolean equals(int xCoord, int yCoord, int zCoord) {
    return x == xCoord && y == yCoord && z == zCoord;
  }

  public void writeToBuf(ByteBuf buf) {
    buf.writeInt(x);
    buf.writeInt(y);
    buf.writeInt(z);
  }

  public static BlockCoord readFromBuf(ByteBuf buf) {
    return new BlockCoord(buf.readInt(), buf.readInt(), buf.readInt());
  }
}
