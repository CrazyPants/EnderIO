package crazypants.enderio.conduit.gui.item;

import crazypants.enderio.base.filter.IItemFilter;
import crazypants.enderio.base.filter.gui.IItemFilterContainer;
import crazypants.enderio.base.network.PacketHandler;
import crazypants.enderio.conduit.item.IItemConduit;
import crazypants.enderio.conduit.packet.PacketItemConduitFilter;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class ItemConduitFilterContainer implements IItemFilterContainer {

  private IItemConduit itemConduit;
  private EnumFacing dir;
  private boolean isInput;

  public ItemConduitFilterContainer(@Nonnull IItemConduit itemConduit, @Nonnull EnumFacing dir, boolean isInput) {
    this.itemConduit = itemConduit;
    this.dir = dir;
    this.isInput = isInput;
  }

  @Override
  public IItemFilter getItemFilter() {
    if(isInput) {
      return itemConduit.getInputFilter(dir);
    } else {
      return itemConduit.getOutputFilter(dir);
    }
  }

  // TODO Abstract filter logic to work for each different kind of filter
  @Override
  public void onFilterChanged() {
    PacketHandler.INSTANCE.sendToServer(new PacketItemConduitFilter(itemConduit, dir));
  }

}
