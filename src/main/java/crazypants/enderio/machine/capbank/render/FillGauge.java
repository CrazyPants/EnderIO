package crazypants.enderio.machine.capbank.render;

import static crazypants.enderio.machine.MachineObject.blockCapBank;

import crazypants.enderio.machine.capbank.BlockCapBank;
import crazypants.enderio.machine.capbank.TileCapBank;
import net.minecraft.util.EnumFacing;

public class FillGauge implements IInfoRenderer {

  @Override
  public void render(TileCapBank te, EnumFacing dir, float partialTick) {
    FillGaugeBakery bakery = new FillGaugeBakery(te.getWorld(), te.getPos(), dir, ((BlockCapBank) blockCapBank.getBlock()).getGaugeIcon());
    if (bakery.canRender()) {
      bakery.render();
    }
  }

}
