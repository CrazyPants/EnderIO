package com.enderio.conduits.client;

import com.enderio.conduits.EnderIOConduits;
import com.enderio.conduits.client.model.conduit.facades.FacadeHelper;
import com.enderio.conduits.common.conduit.bundle.ConduitBundleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

@EventBusSubscriber(modid = EnderIOConduits.MODULE_MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ConduitHighlightEvent {

    @SubscribeEvent
    public static void highlight(RenderHighlightEvent.Block event) {
        var minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        if (minecraft.level
                .getBlockEntity(event.getTarget().getBlockPos()) instanceof ConduitBundleBlockEntity conduit) {
            // Use standard block highlights for facades.
            if (conduit.hasFacade() && FacadeHelper.areFacadesVisible()) {
                return;
            }

            event.setCanceled(true);
            BlockPos pos = event.getTarget().getBlockPos();
            Vec3 camPos = event.getCamera().getPosition();
            LevelRenderer.renderShape(event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.lines()),
                    conduit.getShape().getShapeFromHit(event.getTarget().getBlockPos(), event.getTarget()),
                    (double) pos.getX() - camPos.x, (double) pos.getY() - camPos.y, (double) pos.getZ() - camPos.z,
                    0.0F, 0.0F, 0.0F, 0.4F);
        }
    }
}
