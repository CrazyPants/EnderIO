package crazypants.enderio.machine.tank;

import java.awt.Rectangle;
import java.io.IOException;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.gui.button.CycleButton;
import com.enderio.core.client.gui.widget.GuiToolTip;
import com.enderio.core.client.render.RenderUtil;

import crazypants.enderio.EnderIO;
import crazypants.enderio.fluid.Fluids;
import crazypants.enderio.gui.IconEIO;
import crazypants.enderio.machine.gui.GuiMachineBase;
import crazypants.enderio.network.PacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class GuiTank extends GuiMachineBase<TileTank> {

  private static final Rectangle RECTANGLE_TANK = new Rectangle(80, 21, 16, 47);
  private CycleButton<VoidMode.IconHolder> voidBut;
  
  public GuiTank(InventoryPlayer par1InventoryPlayer, TileTank te) {
    super(te, new ContainerTank(par1InventoryPlayer, te), "tank");
    
    addToolTip(new GuiToolTip(RECTANGLE_TANK, "") {

      @Override
      protected void updateText() {
        text.clear();
        String heading = EnderIO.lang.localize("tank.tank");
        if(getTileEntity().tank.getFluid() != null) {
          heading += ": " + getTileEntity().tank.getFluid().getLocalizedName();
        }
        text.add(heading);
        text.add(Fluids.toCapactityString(getTileEntity().tank));
      }

    });
    
    addToolTip(new GuiToolTip(new Rectangle(14, 35, 18, 18), EnderIO.lang.localize("gui.tooltip.voidslot")) {
      @Override
      public boolean shouldDraw() {
        return super.shouldDraw() && getTileEntity().canVoidItems();
      }
    });
    
    voidBut = new CycleButton<VoidMode.IconHolder>(this, 123, 155, 43 + (showRecipeButton() ? 18 : 0), VoidMode.IconHolder.class);
  }

  @Override
  @Nullable
  public Object getIngredientUnderMouse(int mouseX, int mouseY) {
    if (RECTANGLE_TANK.contains(mouseX, mouseY)) {
      return getTileEntity().tank.getFluid();
    }
    return super.getIngredientUnderMouse(mouseX, mouseY);
  }

  @Override
  public void initGui() {
    super.initGui();
    voidBut.onGuiInit();
    voidBut.setMode(VoidMode.IconHolder.getFromMode(getTileEntity().getVoidMode()));
    ((ContainerTank) inventorySlots).createGhostSlots(getGhostSlots());
  }

  @Override
  public void updateScreen() {
    Slot slot = inventorySlots.inventorySlots.get(2);
    if (getTileEntity().canVoidItems()) {
      slot.xPos = 15;
      slot.yPos = 36;
      voidBut.onGuiInit();
    } else {
      slot.xPos = 10000;
      slot.yPos = 10000;
      voidBut.detach();
    }
  }
  
  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    super.actionPerformed(button);
    if (button.id == voidBut.id) {
      getTileEntity().setVoidMode(voidBut.getMode().getMode());
      PacketHandler.INSTANCE.sendToServer(new PacketTankVoidMode(getTileEntity()));
    }
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
    
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    bindGuiTexture();
    int sx = (width - xSize) / 2;
    int sy = (height - ySize) / 2;

    drawTexturedModalRect(sx, sy, 0, 0, xSize, ySize);
    
    if (getTileEntity().canVoidItems()) {
      Slot slot = inventorySlots.inventorySlots.get(2);
      drawTexturedModalRect(sx + slot.xPos - 1, sy + slot.yPos - 1, xSize, 0, 18, 18);
      IconEIO.map.render(IconEIO.MINUS, sx + slot.xPos, sy + slot.yPos, true);
    }

    super.drawGuiContainerBackgroundLayer(par1, par2, par3);    
    
    RenderUtil.bindBlockTexture();
    RenderUtil.renderGuiTank(getTileEntity().tank, guiLeft + 80, guiTop + 21, zLevel, 16,47);    
  }
}
