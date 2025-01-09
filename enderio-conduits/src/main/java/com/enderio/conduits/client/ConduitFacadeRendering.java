//package com.enderio.conduits.client;
//
//import com.enderio.conduits.client.model.conduit.facades.FacadeHelper;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import java.util.Map;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.client.renderer.LightTexture;
//import net.minecraft.client.renderer.Sheets;
//import net.minecraft.client.renderer.texture.OverlayTexture;
//import net.minecraft.core.BlockPos;
//import net.minecraft.util.FastColor;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.level.LightLayer;
//import net.minecraft.world.level.block.state.BlockState;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
//import net.neoforged.neoforge.client.model.data.ModelData;
//import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
//
//@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
//public class ConduitFacadeRendering {
//
//    @SubscribeEvent
//    static void renderFacade(RenderLevelStageEvent event) {
//        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS || FacadeHelper.areFacadesVisible()) {
//            return;
//        }
//
//        for (Map.Entry<BlockPos, BlockState> entry : ConduitBundleBlockEntity.FACADES.entrySet()) {
//            ClientLevel level = Minecraft.getInstance().level;
//            if (!level.isLoaded(entry.getKey())) {
//                return;
//            }
//            if (level.getBlockState(entry.getKey()).getBlock() instanceof ConduitBundleBlock) {
//                if (entry.getValue() == null) {
//                    continue;
//                }
//
//                var baseConsumer = Minecraft.getInstance()
//                        .renderBuffers()
//                        .bufferSource()
//                        .getBuffer(Sheets.translucentCullBlockSheet());
//                var wrappedConsumer = new VertexConsumerWrapper(baseConsumer) {
//                    @Override
//                    public VertexConsumer setColor(int r, int g, int b, int a) {
//                        super.setColor(r, g, b, 85);
//                        return this;
//                    }
//                };
//
//                var cameraPos = event.getCamera().getPosition();
//                event.getPoseStack().pushPose();
//                event.getPoseStack()
//                        .translate(entry.getKey().getX() - cameraPos.x, entry.getKey().getY() - cameraPos.y,
//                                entry.getKey().getZ() - cameraPos.z);
//
//                var model = Minecraft.getInstance()
//                        .getModelManager()
//                        .getBlockModelShaper()
//                        .getBlockModel(entry.getValue());
//                int color = Minecraft.getInstance().getBlockColors().getColor(entry.getValue(), level, entry.getKey());
//                for (var renderType : model.getRenderTypes(entry.getValue(), RandomSource.create(), ModelData.EMPTY)) {
//                    Minecraft.getInstance()
//                            .getBlockRenderer()
//                            .getModelRenderer()
//                            .renderModel(event.getPoseStack().last(), wrappedConsumer, entry.getValue(), model,
//                                    FastColor.ARGB32.red(color) / 255.0F, FastColor.ARGB32.green(color) / 255.0F,
//                                    FastColor.ARGB32.blue(color) / 255.0F,
//                                    LightTexture.pack(level.getBrightness(LightLayer.BLOCK, entry.getKey()),
//                                            level.getBrightness(LightLayer.SKY, entry.getKey())),
//                                    OverlayTexture.NO_OVERLAY,
//                                    model.getModelData(level, entry.getKey(), entry.getValue(), ModelData.EMPTY),
//                                    renderType);
//                }
//                Minecraft.getInstance().renderBuffers().bufferSource().endBatch(Sheets.translucentCullBlockSheet());
//                event.getPoseStack().popPose();
//            }
//        }
//    }
//}
