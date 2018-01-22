package crazypants.enderio.powertools;

import com.enderio.core.common.Lang;
import crazypants.enderio.conduit.EnderIOConduits;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod(modid = EnderIOPowerTools.MODID, name = EnderIOPowerTools.MOD_NAME, version = EnderIOPowerTools.VERSION, dependencies = "after:" + EnderIOConduits.MODID)
public class EnderIOPowerTools {

  public static final @Nonnull String MODID = "enderiopowertools";
  public static final @Nonnull String DOMAIN = "enderio";
  public static final @Nonnull String MOD_NAME = "Ender IO Powertools";
  public static final @Nonnull String VERSION = "@VERSION@";

  public static final @Nonnull Lang lang = new Lang(DOMAIN);

  // TODO
//  @EventHandler
//  public void postInit(@Nonnull FMLPostInitializationEvent event) {
//    PowerToolRecipes.addRecipes();
//  }

}
