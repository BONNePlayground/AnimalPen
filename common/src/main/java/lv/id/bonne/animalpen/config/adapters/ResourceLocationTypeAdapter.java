package lv.id.bonne.animalpen.config.adapters;


import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import java.lang.reflect.Type;

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
        return new ResourceLocation(json.getAsString());
    }
}