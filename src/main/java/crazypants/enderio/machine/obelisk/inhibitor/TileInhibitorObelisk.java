package crazypants.enderio.machine.obelisk.inhibitor;

import crazypants.enderio.machine.MachineObject;
import crazypants.enderio.machine.baselegacy.SlotDefinition;
import crazypants.enderio.machine.obelisk.AbstractBlockObelisk;
import crazypants.enderio.machine.obelisk.AbstractRangedTileEntity;
import info.loenwind.autosave.annotations.Storable;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;



@Storable
public class TileInhibitorObelisk extends AbstractRangedTileEntity {

  public TileInhibitorObelisk() {
    super(new SlotDefinition(0, 0, 1), MachineObject.blockInhibitorObelisk);
  }

  @Override
  public @Nonnull String getMachineName() {
    return MachineObject.blockInhibitorObelisk.getUnlocalisedName();
  }

  @Override
  public boolean isMachineItemValidForSlot(int i, ItemStack itemstack) {
    return false;
  }

  @Override
  public boolean isActive() {
    return hasPower() && redstoneCheckPassed;
  }

  @Override
  protected boolean processTasks(boolean redstoneCheck) {
    return false;
  }

  @Override
  public float getRange() {
    //return AVERSION_RANGE.getFloat(getCapacitorData()) / 2; TODO
    return (float) AbstractBlockObelisk.DUMMY;
  }

  @Override
  public void onCapacitorDataChange() {
    super.onCapacitorDataChange();
    BlockInhibitorObelisk.instance.activeInhibitors.put(getLocation(), getBounds());
  }

  @Override
  public void validate() {
    super.validate();
    BlockInhibitorObelisk.instance.activeInhibitors.put(getLocation(), getBounds());
  }

  @Override
  public void invalidate() {
    super.invalidate();
    BlockInhibitorObelisk.instance.activeInhibitors.remove(getLocation());
  }

  @Override
  public void onChunkUnload() {
    super.onChunkUnload();
    BlockInhibitorObelisk.instance.activeInhibitors.remove(getLocation());
  }

}
