package crazypants.enderio.conduits.network;

import com.enderio.core.common.util.DyeColor;

import crazypants.enderio.conduits.conduit.redstone.IRedstoneConduit;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRedstoneConduitSignalColor extends AbstractConduitPacket<IRedstoneConduit> {

  private EnumFacing dir;
  private DyeColor col;

  public PacketRedstoneConduitSignalColor() {
  }

  public PacketRedstoneConduitSignalColor(IRedstoneConduit con, EnumFacing dir) {
    super(con.getBundle().getEntity(), con);
    this.dir = dir;
    col = con.getSignalColor(dir);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    if (dir == null) {
      buf.writeShort(-1);
    } else {
      buf.writeShort(dir.ordinal());
    }
    buf.writeShort(col.ordinal());
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    short ord = buf.readShort();
    if (ord < 0) {
      dir = null;
    } else {
      dir = EnumFacing.values()[ord];
    }
    col = DyeColor.values()[buf.readShort()];
  }

  public static class Handler implements IMessageHandler<PacketRedstoneConduitSignalColor, IMessage> {

    @Override
    public IMessage onMessage(PacketRedstoneConduitSignalColor message, MessageContext ctx) {
      final IRedstoneConduit conduit = message.getConduit(ctx);
      if (conduit != null) {
        conduit.setSignalColor(message.dir, message.col);
        IBlockState bs = message.getWorld(ctx).getBlockState(message.getPos());
        message.getWorld(ctx).notifyBlockUpdate(message.getPos(), bs, bs, 3);
      }
      return null;
    }
  }

}
