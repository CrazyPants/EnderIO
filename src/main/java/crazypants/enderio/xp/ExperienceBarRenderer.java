package crazypants.enderio.xp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import org.lwjgl.opengl.GL11;

import crazypants.render.ColorUtil;
import crazypants.render.RenderUtil;

public class ExperienceBarRenderer {

  public static void render(Gui gui, int x, int y, int length, ExperienceContainer xpCont) {
    render(gui, x, y, length, xpCont, -1);
  }
  
  public static void render(Gui gui, int x, int y, int length, ExperienceContainer xpCont, int required) {
    
    String text = xpCont.getExperienceLevel() + "";
    int color = 8453920;
    boolean shadow = true;
    if(required > 0) {
      text += "/" + required;
      if(required > xpCont.getExperienceLevel()) {
        color = ColorUtil.getRGB(1f,0,0.1f);
        shadow = false;
      }
    }
    FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
    int strX  = x + length/2 - fr.getStringWidth(text) / 2;
    fr.drawString(text, strX, y-11, color, shadow);
    
    RenderUtil.bindTexture("enderio:textures/gui/widgets.png");
    GL11.glColor3f(1, 1, 1);
    int xpScaled = xpCont.getXpBarScaled(length -2);    
    
    // x, y, u, v, width, height
    //start of 'slot'
    gui.drawTexturedModalRect(x,y,80,141,1,5);    
    gui.drawTexturedModalRect(x + 1,y,81,141,length-2,5);    
    gui.drawTexturedModalRect(x + length - 1,y,205,141,1,5);
    
    RenderUtil.renderQuad2D(x + 1, y + 1, 0, xpScaled, 3, ColorUtil.getRGB(0, 127, 14));
    
    
    
  }
  
}
