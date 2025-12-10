package lv.id.bonne.animalpen.config.adapters;


import com.google.gson.*;
import java.lang.reflect.Type;

import net.minecraft.resources.Identifier;

public class IdentifierTypeAdapter implements JsonSerializer<Identifier>, JsonDeserializer<Identifier>
{
    @Override
    public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Identifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        return Identifier.bySeparator(json.getAsString(), ':');
    }
}