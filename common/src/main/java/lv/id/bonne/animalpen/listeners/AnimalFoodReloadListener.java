package lv.id.bonne.animalpen.listeners;


import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.util.Map;

import dev.architectury.platform.Platform;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenFoodRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Ingredient;


/**
 * This class handles animal food resource data loading into registry.
 */
public class AnimalFoodReloadListener extends SimpleJsonResourceReloadListener
{
    /**
     * Instantiates a new Animal food reload listener.
     */
    public AnimalFoodReloadListener()
    {
        super(GSON, FOLDER);
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler)
    {
        // Clear the registry before reloading new data.
        AnimalPenFoodRegistry.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet())
        {
            ResourceLocation id = entry.getKey();

            try
            {
                JsonElement element = entry.getValue();

                if (!element.isJsonObject())
                {
                    AnimalPen.LOGGER.error("Invalid JSON format for " + id + ": Expected a JSON object.");
                    continue;
                }

                JsonObject json = element.getAsJsonObject();

                if (json.has("condition") && json.get("condition").isJsonPrimitive())
                {
                    JsonPrimitive primitive = json.getAsJsonPrimitive("condition");

                    if (primitive.isString() && primitive.getAsString().startsWith("mod:"))
                    {
                        String modId = primitive.getAsString().substring(4);

                        if (!modId.isBlank() && !Platform.isModLoaded(modId))
                        {
                            // Do not load entities that needs mod to be enabled. Do not need error message.
                            continue;
                        }
                    }
                }


                // New JSON structure: direct array of ingredients.
                JsonArray ingredientsArray = json.has("food_items") && json.get("food_items").isJsonArray()
                    ? json.getAsJsonArray("food_items")
                    : new JsonArray();

                Ingredient ingredient;

                try
                {
                    ingredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, ingredientsArray).
                        getOrThrow(text ->
                        {
                            AnimalPen.LOGGER.error("Error parsing ingredient in " + id + ": " + text);
                            return new IllegalArgumentException(text);
                        });
                }
                catch (Exception e)
                {
                    continue;
                }

                // Create data instance and register it.
                AnimalPenFoodRegistry.AnimalFoodData data = new AnimalPenFoodRegistry.AnimalFoodData(ingredient);
                AnimalPenFoodRegistry.register(id, data);
            }
            catch (JsonSyntaxException e)
            {
                AnimalPen.LOGGER.error("JSON syntax error in " + id + ": " + e.getMessage());
            }
            catch (Exception e)
            {
                AnimalPen.LOGGER.error("Error processing file " + id + ": " + e.getMessage());
            }
        }

        AnimalPen.LOGGER.info("Loaded " + AnimalPenFoodRegistry.getAll().size() + " animal food entries.");
    }


    /**
     * This creates instance of GSON reader for food items.
     */
    private static final Gson GSON = new GsonBuilder().
        registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).
        create();

    /**
     * The location of data folder.
     */
    private static final String FOLDER = "animal_foods";
}