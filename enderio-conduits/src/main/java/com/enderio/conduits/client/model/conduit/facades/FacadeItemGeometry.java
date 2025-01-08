package com.enderio.conduits.client.model.conduit.facades;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public class FacadeItemGeometry implements IUnbakedGeometry<FacadeItemGeometry> {

    private final BlockModel facadeModel;

    public FacadeItemGeometry(BlockModel facadeModel) {
        this.facadeModel = facadeModel;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        return new FacadeItemModel(facadeModel.bake(baker, spriteGetter, modelState));
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        facadeModel.resolveParents(modelGetter);
    }

    public static class Loader implements IGeometryLoader<FacadeItemGeometry> {
        @Override
        public FacadeItemGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext)
                throws JsonParseException {
            BlockModel model = deserializationContext.deserialize(jsonObject.get("model"), BlockModel.class);
            return new FacadeItemGeometry(model);
        }
    }
}
