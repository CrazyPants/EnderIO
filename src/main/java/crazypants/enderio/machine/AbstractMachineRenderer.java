package crazypants.enderio.machine;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import crazypants.enderio.machine.painter.IPaintableTileEntity;
import crazypants.enderio.machine.painter.PaintedBlockRenderer;
import crazypants.enderio.machine.painter.PainterUtil;
import crazypants.render.BoundingBox;
import crazypants.render.CubeRenderer;
import crazypants.render.CustomCubeRenderer;
import crazypants.render.CustomRenderBlocks;
import crazypants.render.IRenderFace;
import crazypants.render.IconUtil;
import crazypants.render.RenderUtil;
import crazypants.vecmath.Vertex;

public class AbstractMachineRenderer implements ISimpleBlockRenderingHandler, IItemRenderer {

  private OverlayRenderer overlayRenderer = new OverlayRenderer();

  private AbstractMachineEntity curEnt;

  private CustomCubeRenderer ccr = new CustomCubeRenderer();
  
  private PaintedBlockRenderer paintedRenderer = new PaintedBlockRenderer(this.getRenderId(), null); // passthrough renderer for paintable machines

  @Override
  public boolean handleRenderType(ItemStack item, ItemRenderType type) { 
    return true;
  }

  @Override
  public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
    return true;
  }

  @Override
  public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
    Block block;
    if ((block = PainterUtil.getSourceBlock(item)) != null) {
      paintedRenderer.renderInventoryBlock(block, PainterUtil.getSourceBlockMetadata(item), 0, (RenderBlocks)data[0]);
    } else {
      renderInventoryBlock(Block.getBlockFromItem(item.getItem()), item.getItemDamage(), 0, (RenderBlocks)data[0]);
    }
  }

  @Override
  public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {

    BoundingBox bb = BoundingBox.UNIT_CUBE;

    Tessellator.instance.startDrawingQuads();

    IIcon[] textures = RenderUtil.getBlockTextures(block, metadata);

    float[] brightnessPerSide = new float[6];
    for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
      brightnessPerSide[dir.ordinal()] = Math.max(RenderUtil.getColorMultiplierForFace(dir) + 0.1f, 1f);
    }
    CubeRenderer.render(bb, textures, null, brightnessPerSide);
    Tessellator.instance.draw();
  }

  @Override
  public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {

    TileEntity te = world.getTileEntity(x, y, z);
    if(te instanceof AbstractMachineEntity) {
      curEnt = (AbstractMachineEntity)te;      
    } else {
      curEnt = null;
    }
    
    if (te instanceof IPaintableTileEntity && ((IPaintableTileEntity) te).getSourceBlock() != null) {
      ccr.setOverrideTexture(IconUtil.blankTexture);
      ccr.renderBlock(world, block, x, y, z, overlayRenderer);
      ccr.setOverrideTexture(null);
      paintedRenderer.renderWorldBlock(world, x, y, z, block, modelId, renderer);
    } else {
      ccr.renderBlock(world, block, x, y, z, overlayRenderer);
    }

    return true;
  }

  @Override
  public boolean shouldRender3DInInventory(int modelId) {
    return true;
  }

  @Override
  public int getRenderId() {
    return AbstractMachineBlock.renderId;
  }

  private class OverlayRenderer implements IRenderFace {

    @Override
    public void renderFace(CustomRenderBlocks rb, ForgeDirection face, Block par1Block, double x, double y, double z, IIcon texture, List<Vertex> refVertices,
        boolean translateToXyz) {

      ccr.getCustomRenderBlocks().doDefaultRenderFace(face,par1Block,x,y,z,texture);
      if(curEnt != null && par1Block instanceof AbstractMachineBlock) {
        IoMode mode = curEnt.getIoMode(face);
        IIcon tex = ((AbstractMachineBlock)par1Block).getOverlayIconForMode(mode);
        if(tex != null) {
          // dirty z-fighting hax, avert your eyes!
          ccr.getCustomRenderBlocks().doDefaultRenderFace(face, par1Block, x + (face.offsetX * 0.0001), y + (face.offsetY * 0.0001), z + (face.offsetZ * 0.0001), tex);
        }
      }

    }

  }

}
