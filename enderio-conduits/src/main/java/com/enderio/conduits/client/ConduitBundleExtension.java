package com.enderio.conduits.client;

import com.enderio.conduits.api.bundle.ConduitBundleReader;
import com.enderio.conduits.client.model.conduit.facades.FacadeHelper;
import com.enderio.conduits.client.particle.ConduitBreakParticle;
import com.enderio.conduits.common.conduit.bundle.ConduitBundleBlock;
import com.enderio.conduits.common.conduit.bundle.ConduitBundleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

public class ConduitBundleExtension implements IClientBlockExtensions {

    public static ConduitBundleExtension INSTANCE = new ConduitBundleExtension();

    private ConduitBundleExtension() {
    }

    @Override
    public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        if (!(target instanceof BlockHitResult blockHitResult)) {
            return false;
        }

        if (level.getBlockEntity(blockHitResult.getBlockPos()) instanceof ConduitBundleBlockEntity conduitBundle) {
            // TODO: Get the conduit texture and add it to the particle manager.

            if (conduitBundle.hasFacade() && FacadeHelper.areFacadesVisible()) {
                return false;
            }

            var conduit = conduitBundle.getShape().getConduit(blockHitResult.getBlockPos(), target);
            if (conduit != null) {
                ConduitBreakParticle.addCrackEffects(blockHitResult.getBlockPos(), state, conduit.value(), blockHitResult.getDirection());
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        // Use vanilla particles if we have a visible facade.
        // Conduit break particles are handled by the BE.
        if (level.getBlockEntity(pos) instanceof ConduitBundleReader conduitBundle) {
            return !(conduitBundle.hasFacade() && FacadeHelper.areFacadesVisible());
        }

        // Not a bundle
        return false;
    }
}
