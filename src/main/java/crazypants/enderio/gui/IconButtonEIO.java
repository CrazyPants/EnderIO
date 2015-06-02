package crazypants.enderio.gui;

import java.awt.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import crazypants.gui.GuiToolTip;
import crazypants.gui.IGuiScreen;
import crazypants.render.RenderUtil;

public class IconButtonEIO extends GuiButton {

  public static final int DEFAULT_WIDTH = 16;
  public static final int DEFAULT_HEIGHT = 16;

  protected IconEIO icon;
  protected ResourceLocation texture;
  private int xOrigin;
  private int yOrigin;

  protected IGuiScreen gui;
  protected String[] toolTipText;

  private GuiToolTip toolTip;
  private int marginY = 0;
  private int marginX = 0;

  public IconButtonEIO(IGuiScreen gui, int id, int x, int y, IconEIO icon) {
    super(id, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, "");
    this.gui = gui;
    this.icon = icon;
    texture = IconEIO.TEXTURE;
    this.xOrigin = x;
    this.yOrigin = y;

  }
 
  public void setToolTip(String... tooltipText) {
    if(toolTip == null) {
      toolTip = new GuiToolTip(getBounds(), tooltipText);
      //gui.addToolTip(toolTip);
    } else {
      toolTip.setToolTipText(tooltipText);
    }
    this.toolTipText = tooltipText;
  }

  protected void setToolTip(GuiToolTip newToolTip) {
    if (toolTip != null) {
      gui.removeToolTip(toolTip);
    }
    toolTip = newToolTip;
    if (toolTip != null) {
      gui.addToolTip(toolTip);
    }
  }

  public final Rectangle getBounds() {
    return new Rectangle(xOrigin, yOrigin, getWidth(), getHeight());
  }
  
  public void onGuiInit() {
    gui.addButton(this);
    if(toolTip != null) {
      gui.addToolTip(toolTip);
    }
    xPosition = xOrigin + gui.getGuiLeft();
    yPosition = yOrigin + gui.getGuiTop();
  }

  public void detach() {
    gui.removeToolTip(toolTip);
    gui.removeButton(this);
  }

  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
    updateTooltipBounds();
  }

  public IconButtonEIO setPosition(int x, int y) {
    this.xOrigin = x;
    this.yOrigin = y;
    updateTooltipBounds();
    return this;
  }

  private void updateTooltipBounds() {
    if(toolTip != null) {
      toolTip.setBounds(new Rectangle(xOrigin, yOrigin, width, height));
    }
  }

  public IconButtonEIO setIconMargin(int x, int y) {
    marginX = x;
    marginY = y;
    return this;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public IconEIO getIcon() {
    return icon;
  }

  public void setIcon(IconEIO icon) {
    this.icon = icon;
  }
  
  public GuiToolTip getToolTip() {
    return toolTip;
  }

  /**
   * Override this to handle mouse clicks with other buttons than the left
   * @param mc The MC instance
   * @param x X coordinate of mouse click
   * @param y Y coordinate of mouse click
   * @param button the mouse button - only called for button >= 1
   * @return true if the mouse click is handled
   */
  public boolean mousePressedButton(Minecraft mc, int x, int y, int button) {
    return false;
  }

  protected boolean checkMousePress(Minecraft mc, int x, int y) {
    // call super here so that we only get the area check
    return super.mousePressed(mc, x, y);
  }

  /**
   * Draws this button to the screen.
   */
  @SuppressWarnings("synthetic-access")
  @Override
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if(toolTip != null) {
      toolTip.setVisible(visible);
    }    
    if(visible) {

      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + width
          && mouseY < this.yPosition + height;
      int hoverState = getHoverState(this.field_146123_n);
      mouseDragged(mc, mouseX, mouseY);

      IconEIO background = getIconForHoverState(hoverState);

      RenderUtil.bindTexture(texture);
      GL11.glColor3f(1, 1, 1);

      Tessellator tes = Tessellator.instance;
      tes.startDrawingQuads();

      int x = xPosition;
      int y = yPosition;

      GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

      background.renderIcon(x, y, width, height, 0, false);
      if(icon != null) {
        icon.renderIcon(x + marginX, y + marginY, width - (2 * marginX), height - (2 * marginY), 0, false);
      }

      tes.draw();

      GL11.glPopAttrib();

    }
  }

  protected IconEIO getIconForHoverState(int hoverState) {
    if(hoverState == 0) {
      return IconEIO.BUTTON_DISABLED;
    }
    if(hoverState == 2) {
      return IconEIO.BUTTON_HIGHLIGHT;
    }
    return IconEIO.BUTTON;
  }

}
