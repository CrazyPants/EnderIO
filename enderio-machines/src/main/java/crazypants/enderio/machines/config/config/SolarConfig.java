package crazypants.enderio.machines.config.config;

import crazypants.enderio.base.config.factory.IValue;
import crazypants.enderio.base.config.factory.IValueFactory;
import crazypants.enderio.machines.config.Config;

public final class SolarConfig {

  public static final IValueFactory F = Config.F.section("generator.solar");

  public static final IValue<Integer> solarPanelOneOutput = F.make("solarPanelOneOutput", 10, //
      "Maximum output in RF/t of the Simple Photovoltaic Panels.").setMin(1).sync();
  public static final IValue<Integer> solarPanelTwoOutput = F.make("solarPanelTwoOutput", 40, //
      "Maximum output in RF/t of the Photovoltaic Panels.").setMin(1).sync();
  public static final IValue<Integer> solarPanelThreeOutput = F.make("solarPanelThreeOutput", 80, //
      "Maximum output in RF/t of the Advanced Photovoltaic Panels.").setMin(1).sync();
  public static final IValue<Integer> solarPanelFourOutput = F.make("solarPanelFourOutput", 160, //
      "Maximum output in RF/t of the Vibrant Photovoltaic Panels.").setMin(1).sync();

  public static final IValue<Boolean> canSolarTypesJoin = F.make("canSolarTypesJoin", false, //
      "When enabled Photovoltaic Panels of different kinds can join together as a multi-block").setMin(1).sync();
  public static final IValue<Integer> solarRecalcSunTick = F.make("solarRecalcSunTick", 5 * 20, //
      "How often (in ticks) the Photovoltaic Panels should check the sun's angle.").setMin(1).sync();

}
