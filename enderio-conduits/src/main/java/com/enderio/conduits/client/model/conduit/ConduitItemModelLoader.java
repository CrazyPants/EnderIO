package com.enderio.conduits.client.model.conduit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.ElementsModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class ConduitItemModelLoader implements IGeometryLoader<ElementsModel> {

    @Override
    public Geometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext)
            throws JsonParseException {
        if (!jsonObject.has("elements")) {
            throw new JsonParseException("An element model must have an \"elements\" member.");
        }

        List<BlockElement> elements = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(jsonObject, "elements")) {
            elements.add(deserializationContext.deserialize(element, BlockElement.class));
        }

        return new Geometry(elements);
    }

    public static class Geometry extends ElementsModel {

        public Geometry(List<BlockElement> elements) {
            super(elements);
        }

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
            return new ConduitItemModel(super.bake(context, baker, spriteGetter, modelState, overrides));
        }
    }
}
