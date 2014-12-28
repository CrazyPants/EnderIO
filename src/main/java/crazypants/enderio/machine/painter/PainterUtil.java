package crazypants.enderio.machine.painter;

import com.google.common.base.Strings;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.registry.GameData;
import crazypants.util.Lang;

public final class PainterUtil {

  private PainterUtil() {
  }

  public static boolean isMetadataEquivelent(ItemStack one, ItemStack two) {
    if(one == null || two == null) {
      return false;
    }
    return PainterUtil.getSourceBlock(one) == PainterUtil.getSourceBlock(two)
        && PainterUtil.getSourceBlockMetadata(one) == PainterUtil.getSourceBlockMetadata(two);
  }

  public static Block getSourceBlock(ItemStack item) {
    NBTTagCompound tag = item.getTagCompound();
    return getSourceBlock(tag);
  }

  public static Block getSourceBlock(NBTTagCompound tag) {
    if(tag != null) {
      String blockId = tag.getString(BlockPainter.KEY_SOURCE_BLOCK_ID);
      if(!Strings.isNullOrEmpty(blockId)) {
        Block res = (Block) Block.blockRegistry.getObject(blockId);
        return res;
      }
    }
    return null;
  }

  public static int getSourceBlockMetadata(ItemStack item) {
    NBTTagCompound tag = item.getTagCompound();
    return getSourceBlockMetadata(tag);
  }

  public static int getSourceBlockMetadata(NBTTagCompound tag) {
    if(tag != null) {
      return tag.getInteger(BlockPainter.KEY_SOURCE_BLOCK_META);
    }
    return 0;
  }

  public static String getTooltTipText(ItemStack item) {
    String sourceName = "";
    Block sourceId = PainterUtil.getSourceBlock(item);
    int meta = PainterUtil.getSourceBlockMetadata(item);    
    if(sourceId != null) {
      if(sourceId != null) {
        ItemStack is = new ItemStack(Item.getItemFromBlock(sourceId), 1, meta);
        sourceName = is.getDisplayName();
      }
    }
    return Lang.localize("blockPainter.paintedWith") + " " + sourceName;
  }

  public static void setSourceBlock(ItemStack item, Block source, int meta) {
    NBTTagCompound tag = item.getTagCompound();
    if(tag == null) {
      tag = new NBTTagCompound();
      item.setTagCompound(tag);
    }
    setSourceBlock(item.getTagCompound(), source, meta);
  }
  
  public static void setSourceBlock(NBTTagCompound tag, Block source, int meta) {
    if (tag == null) {
      return;
    }
    String name = Block.blockRegistry.getNameForObject(source);
    if(name != null && !name.trim().isEmpty()) {
      tag.setString(BlockPainter.KEY_SOURCE_BLOCK_ID, name);
      tag.setInteger(BlockPainter.KEY_SOURCE_BLOCK_META, meta);
    }
  }
  
  public static ItemStack applyDefaultPaintedState(ItemStack stack) {
    setSourceBlock(stack, Blocks.stone, 0);
    return stack;
  }
}
