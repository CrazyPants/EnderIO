package com.enderio.machines.client.rendering.blockentity;

import com.enderio.machines.common.blocks.obelisks.ObeliskBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ObeliskBER implements BlockEntityRenderer<ObeliskBlockEntity> {

    private final Supplier<Item> supplier;

    public ObeliskBER(Supplier<Item> itemSupplier) {
        this.supplier = itemSupplier;
    }

    public static <T extends ObeliskBlockEntity> Function<BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>> factory(
            Supplier<Item> itemSupplier) {
        return context -> new ObeliskBER(itemSupplier);
    }

    @Override
    public void render(ObeliskBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.75, 0.5);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 position = minecraft.player.position();
        float f1 = (float) (Mth.atan2(position.z - blockEntity.getBlockPos().getZ() - 0.5D,
                position.x - blockEntity.getBlockPos().getX() - 0.5D) * 180.0f / Math.PI + 90);
        poseStack.mulPose(Axis.YP.rotationDegrees(-f1 + 180));
        ItemStack stack = new ItemStack(supplier.get());
        BakedModel bakedmodel = minecraft.getItemRenderer().getModel(stack, blockEntity.getLevel(), null, 0);
        minecraft.getItemRenderer()
                .render(stack, ItemDisplayContext.GUI, true, poseStack, buffer, packedLight, packedOverlay, bakedmodel);
        poseStack.popPose();
    }
}
