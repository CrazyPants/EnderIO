package com.enderio.base.common.lang;

import com.enderio.EnderIOBase;
import com.enderio.base.api.EnderIO;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.base.common.block.glass.GlassCollisionPredicate;
import com.enderio.core.common.lang.EnumTranslationMap;
import com.enderio.regilite.Regilite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class EIOEnumLang {

    private static final Regilite REGILITE = EnderIOBase.REGILITE;

    public static final EnumTranslationMap<RedstoneControl> REDSTONE_CONTROL = builder(RedstoneControl.class,
            "redstone").addTranslation(RedstoneControl.ALWAYS_ACTIVE, "Always Active")
                    .addTranslation(RedstoneControl.ACTIVE_WITH_SIGNAL, "Active with Signal")
                    .addTranslation(RedstoneControl.ACTIVE_WITHOUT_SIGNAL, "Active without Signal")
                    .addTranslation(RedstoneControl.NEVER_ACTIVE, "Never Active")
                    .build();

    public static final EnumTranslationMap<DyeColor> DYE_COLOR = builder(DyeColor.class, "redstone")
            .addTranslation(DyeColor.WHITE, "White")
            .addTranslation(DyeColor.ORANGE, "Orange")
            .addTranslation(DyeColor.MAGENTA, "Magenta")
            .addTranslation(DyeColor.LIGHT_BLUE, "Light Blue")
            .addTranslation(DyeColor.YELLOW, "Yellow")
            .addTranslation(DyeColor.LIME, "Lime")
            .addTranslation(DyeColor.PINK, "Pink")
            .addTranslation(DyeColor.GRAY, "Gray")
            .addTranslation(DyeColor.LIGHT_GRAY, "Light Gray")
            .addTranslation(DyeColor.CYAN, "Cyan")
            .addTranslation(DyeColor.PURPLE, "Purple")
            .addTranslation(DyeColor.BLUE, "Blue")
            .addTranslation(DyeColor.BROWN, "Brown")
            .addTranslation(DyeColor.GREEN, "Green")
            .addTranslation(DyeColor.RED, "Red")
            .addTranslation(DyeColor.BLACK, "Black")
            .build();

    public static final EnumTranslationMap<GlassCollisionPredicate> GLASS_COLLISION = builder(
            GlassCollisionPredicate.class, "collision")
                    .addTranslation(GlassCollisionPredicate.PLAYERS_PASS, "Not solid to players")
                    .addTranslation(GlassCollisionPredicate.PLAYERS_BLOCK, "Only solid to players")
                    .addTranslation(GlassCollisionPredicate.MOBS_PASS, "Not solid to monsters")
                    .addTranslation(GlassCollisionPredicate.MOBS_BLOCK, "Only solid to monsters")
                    .addTranslation(GlassCollisionPredicate.ANIMALS_PASS, "Not solid to animals")
                    .addTranslation(GlassCollisionPredicate.ANIMALS_BLOCK, "Only solid to animals")
                    .build();

    private static <T extends Enum<T>> EnumTranslationMap.Builder<T> builder(Class<T> enumClass, String prefix) {
        return new EnumTranslationMap.Builder<>(EnderIO.NAMESPACE, EIOEnumLang::addTranslation, enumClass, prefix);
    }

    private static Component addTranslation(String prefix, ResourceLocation key, String english) {
        // TODO: Regilite should support a plain string key
        return REGILITE.addTranslation(prefix, key, english);
    }

    public static void register() {
    }
}
