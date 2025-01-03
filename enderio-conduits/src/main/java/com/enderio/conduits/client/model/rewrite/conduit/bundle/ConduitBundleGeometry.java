package com.enderio.conduits.client.model.rewrite.conduit.bundle;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class ConduitBundleGeometry implements IUnbakedGeometry<ConduitBundleGeometry> {

    public ConduitBundleGeometry() {
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
        ItemOverrides overrides) {
        return new NewConduitBundleModel();
    }

    public static class Loader implements IGeometryLoader<ConduitBundleGeometry> {
        @Override
        public ConduitBundleGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
            return new ConduitBundleGeometry();
        }
    }
}
