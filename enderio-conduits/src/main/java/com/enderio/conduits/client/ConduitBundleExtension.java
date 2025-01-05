package com.enderio.conduits.client;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

public class ConduitBundleExtension implements IClientBlockExtensions {

    public static ConduitBundleExtension INSTANCE = new ConduitBundleExtension();

    private ConduitBundleExtension() {
    }

    @Override
    public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        // TODO: Do it based on the aimed conduit
        return true;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
        // TODO: Do it based on the aimed conduit.
        return true;
    }
}
