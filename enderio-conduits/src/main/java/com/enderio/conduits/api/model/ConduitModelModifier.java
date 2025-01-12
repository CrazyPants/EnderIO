package com.enderio.conduits.api.model;

import com.enderio.conduits.api.Conduit;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public interface ConduitModelModifier {

    /**
     * Create additional quads to be rendered at the point of conduit connection.
     */
    default List<BakedQuad> createConnectionQuads(Holder<Conduit<?, ?>> conduit, @Nullable CompoundTag extraWorldData,
            @Nullable Direction facing, Direction connectionDirection, RandomSource rand, @Nullable RenderType type) {
        return List.of();
    }

    default List<ModelResourceLocation> getModelDependencies() {
        return List.of();
    }
}
