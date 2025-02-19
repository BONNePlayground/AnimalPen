package lv.id.bonne.animalpen.config.adapters;


import com.google.gson.*;
import java.lang.reflect.Type;

import net.minecraft.resources.ResourceLocation;

public class ResourceLocationTypeAdapter implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation>
{
    @Override
    public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        return ResourceLocation.bySeparator(json.getAsString(), ':');
    }
}