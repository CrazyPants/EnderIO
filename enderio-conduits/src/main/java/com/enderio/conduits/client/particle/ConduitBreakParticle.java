package com.enderio.conduits.client.particle;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.common.conduit.bundle.NewConduitShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ConduitBreakParticle extends TextureSheetParticle {
    private final BlockPos pos;
    private final float uo;
    private final float vo;

    public ConduitBreakParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockPos pos, ResourceLocation texture) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.pos = pos;
        this.setSprite(Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(texture));
        this.gravity = 1.0F;
        this.rCol = 0.6F;
        this.gCol = 0.6F;
        this.bCol = 0.6F;

        this.quadSize /= 2.0F;
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0F) / 4.0F);
    }

    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0F);
    }

    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0F);
    }

    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0F) / 4.0F);
    }

    public int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        return i == 0 && this.level.hasChunkAt(this.pos) ? LevelRenderer.getLightColor(this.level, this.pos) : i;
    }

    public static void addDestroyEffects(BlockPos pos, Conduit<?, ?> conduit) {
        Level level = Minecraft.getInstance().level;
        ParticleEngine engine = Minecraft.getInstance().particleEngine;
        List<AABB> boxes = NewConduitShape.CONNECTION.toAabbs();
        double countMult = 1D / boxes.size();
        boxes.forEach(aabb -> {
            double sizeX = Math.min(1D, aabb.maxX - aabb.minX);
            double sizeY = Math.min(1D, aabb.maxY - aabb.minY);
            double sizeZ = Math.min(1D, aabb.maxZ - aabb.minZ);
            int xCount = Math.max(2, Mth.ceil(sizeX / 0.25D * countMult));
            int yCount = Math.max(2, Mth.ceil(sizeY / 0.25D * countMult));
            int zCount = Math.max(2, Mth.ceil(sizeZ / 0.25D * countMult));

            for (int iX = 0; iX < xCount; ++iX) {
                for (int iY = 0; iY < yCount; ++iY) {
                    for (int iZ = 0; iZ < zCount; ++iZ) {
                        double offX = ((double) iX + 0.5D) / (double) xCount;
                        double offY = ((double) iY + 0.5D) / (double) yCount;
                        double offZ = ((double) iZ + 0.5D) / (double) zCount;
                        double x = pos.getX() + offX * sizeX + aabb.minX;
                        double y = pos.getY() + offY * sizeY + aabb.minY;
                        double z = pos.getZ() + offZ * sizeZ + aabb.minZ;
                        engine.add(new ConduitBreakParticle((ClientLevel) level, x, y, z, offX - 0.5D, offY - 0.5D, offZ - 0.5D, pos, conduit.texture()));
                    }
                }
            }
        });
    }
}
