package com.enderio.conduits.common.conduit;

import com.enderio.conduits.EnderIOConduits;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.common.init.ConduitComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import org.jetbrains.annotations.Nullable;

/**
 * A11Y tools for conduit block behaviours.
 */
@EventBusSubscriber(/*value = Dist.CLIENT, */modid = EnderIOConduits.MODULE_MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ConduitA11yManager {

    private static Holder<Conduit<?>> heldConduit;

    @Nullable
    public static Holder<Conduit<?>> getHeldConduit() {
        return heldConduit;
    }

    @SubscribeEvent
    public static void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Only does the main hand
            ItemStack mainItem = player.getMainHandItem();
            if (mainItem.has(ConduitComponents.CONDUIT)) {
                heldConduit = mainItem.get(ConduitComponents.CONDUIT);
            } else {
                heldConduit = null;
            }
        }
    }
}
