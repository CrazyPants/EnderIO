package crazypants.enderio.machine.enchanter;

import crazypants.enderio.config.Config;
import crazypants.enderio.recipe.RecipeInput;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public class EnchanterRecipe {

  private final RecipeInput input;
  private final Enchantment enchantment;
  private final int stackSizePerLevel;
  private final double costMultiplier;

  public EnchanterRecipe(RecipeInput curInput, String enchantmentName) {
    this(curInput, Enchantment.getEnchantmentByLocation(enchantmentName), 1);    
  }

  public EnchanterRecipe(RecipeInput input, Enchantment enchantment, double costMultiplier) {
    this.input = input;
    this.enchantment = enchantment;
    stackSizePerLevel = input.getInput().getCount();
    this.costMultiplier = costMultiplier;
  }

  public boolean isInput(ItemStack stack) {
    if (stack == null || !isValid()) {
      return false;
    }
    return input.isInput(stack);
  }

  public boolean isValid() {
    return enchantment != null && input != null && input.getInput() != null;
  }

  public Enchantment getEnchantment() {
    return enchantment;
  }

  public RecipeInput getInput() {
    return input;
  }

  public int getLevelForStackSize(int size) {
    return Math.min(size / stackSizePerLevel, enchantment.getMaxLevel());
  }

  public int getItemsPerLevel() {
    return stackSizePerLevel;
  }

  public int getCostForLevel(int level) {
    level = Math.min(level, enchantment.getMaxLevel());
    int cost = getRawCostForLevel(level);
    if (level < enchantment.getMaxLevel()) {
      // min cost of half the next levels XP cause books combined in anvil
      int nextCost = getRawCostForLevel(level + 1);
      cost = Math.max(nextCost / 2, cost);

    }
    return cost;
  }

  private int getRawCostForLevel(int level) {    
     // -1 cause its the index
    double min = Math.max(1, enchantment.getMinEnchantability(level));    
    min *= costMultiplier; //per recipe scaling        
    int cost = (int) Math.round(min * Config.enchanterLevelCostFactor); //global scaling    
    cost += Config.enchanterBaseLevelCost; //add base cost
    return cost;
  }

  public int getLapizForLevel(int level) {
    int res = enchantment.getMaxLevel() == 1 ? 5 : level;
    return (int)Math.max(1, Math.round(res * Config.enchanterLapisCostFactor));
  }

  public int getLapizForStackSize(int stackSize) {    
    return getLapizForLevel(getLevelForStackSize(stackSize));
  }

}
