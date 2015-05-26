package crazypants.enderio.machine;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ObjectArrays;

import crazypants.render.CustomCubeRenderer;
import crazypants.render.TechneModelRenderer;
import crazypants.render.TechneUtil;
import crazypants.render.VertexRotationFacing;
import crazypants.render.VertexTransform;
import crazypants.render.VertexTransformComposite;

public class TechneMachineRenderer<T extends AbstractMachineEntity> extends TechneModelRenderer {

  private CustomCubeRenderer ccr = new CustomCubeRenderer();
  private OverlayRenderer overlay = new OverlayRenderer();

  public TechneMachineRenderer(AbstractMachineBlock<T> block, String modelPath) {
    super(modelPath, block.getRenderType(), new VertexTransformComposite(new VertexRotationFacing(ForgeDirection.NORTH)));
  }

  public TechneMachineRenderer(int renderId, String modelPath) {
    super(TechneUtil.getModel(modelPath), renderId, new VertexTransformComposite(new VertexRotationFacing(ForgeDirection.NORTH)));
  }

  public TechneMachineRenderer<T> addTransform(VertexTransform vt) {
    this.vt = new VertexTransformComposite(ObjectArrays.concat(vt, ((VertexTransformComposite) this.vt).xforms));
    return this;
  }

  @Override
  public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
    setFacingDir(ForgeDirection.SOUTH);
    super.renderInventoryBlock(block, metadata, modelId, renderer);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
    T te = (T) world.getTileEntity(x, y, z);
    if(te != null) {
      setFacingDir(te.getFacingDir());
      overlay.setTile(te);
    }

    super.renderWorldBlock(world, x, y, z, block, modelId, renderer);

    if(renderer.overrideBlockTexture == null) {
      ccr.renderBlock(world, block, x, y, z, overlay);
    }

    return true;
  }

  private void setFacingDir(ForgeDirection dir) {
    VertexRotationFacing rot = (VertexRotationFacing) ((VertexTransformComposite) vt).xforms[0];
    rot.setRotation(dir);
  }
}
