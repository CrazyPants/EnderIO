package crazypants.enderio.machine.farm.farmers;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import crazypants.enderio.config.Config;
import crazypants.enderio.machine.farm.TileFarmStation;
import crazypants.util.BlockCoord;

public class NaturaBerryFarmer extends PickableFarmer {

  public NaturaBerryFarmer(Block plantedBlock, int plantedBlockMeta, int grownBlockMeta, ItemStack seeds) {
    super(plantedBlock, plantedBlockMeta, grownBlockMeta, seeds);
  }

  @Override
  public IHarvestResult harvestBlock(TileFarmStation farm, BlockCoord bc, Block block, int meta) {
    
    if(block != getPlantedBlock()) {
      return null;
    }
    if(!farm.hasHoe()) {
      farm.setNotification(TileFarmStation.NOTIFICATION_NO_HOE);
      return null;
    }
    
    IHarvestResult res = new HarvestResult();    
    
    BlockCoord checkBlock = bc;
    for(int i=0; i < 5 && farm.hasHoe(); i++) {
      meta = farm.getBlockMeta(checkBlock);
      IHarvestResult blockRes = super.harvestBlock(farm, checkBlock, block, meta);
    
      if(blockRes != null) {       
        res.getHarvestedBlocks().add(checkBlock);
        List<EntityItem> addToDrops = blockRes.getDrops();
        for(EntityItem stack : addToDrops) {
          res.getDrops().add(stack);
        }
      }
      checkBlock = checkBlock.getLocation(ForgeDirection.UP);
    }
    
    if(res.getHarvestedBlocks().isEmpty()) {
      return null;
    }    
    
    return res;
  }

  @Override
  public boolean canHarvest(TileFarmStation farm, BlockCoord bc, Block block, int meta) {
    if(!Config.farmEssenceBerriesEnabled && "tile.ore.berries.two".equals(block.getUnlocalizedName()) && meta == grownBlockMeta) {
      return false;
    }
    
    BlockCoord checkBlock = bc;

    meta = farm.getBlockMeta(checkBlock);
    block = checkBlock.getBlock(farm.getWorldObj());
    return super.canHarvest(farm, checkBlock, block, meta);
  }
}