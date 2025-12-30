package lv.id.bonne.animalpen.data.listener;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.Map;

import dev.architectury.platform.Platform;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;


/**
 * This class handles animal interaction resource data loading into registry.
 */
public class AnimalInteractionReloadListener extends SimpleJsonResourceReloadListener
{
    /**
     * Instantiates a new Animal interaction reload listener.
     */
    public AnimalInteractionReloadListener()
    {
        super(GSON, FOLDER);
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap,
        ResourceManager resourceManager,
        ProfilerFiller profiler)
    {
        // Clear the registry before reloading new data.
        AnimalPenInteractionRegistry.clear();

        jsonMap.forEach((id, element) ->
        {
            if (!element.isJsonArray())
            {
                return;
            }

            Registry.ENTITY_TYPE.getOptional(id).ifPresent(entityType ->
            {
                for (JsonElement e : element.getAsJsonArray())
                {
                    JsonObject json = e.getAsJsonObject();

                    if (json.has("required_mod"))
                    {
                        String modId = json.get("required_mod").getAsString();

                        if (!Platform.isModLoaded(modId))
                        {
                            continue;
                        }
                    }

                    AnimalInteraction.CODEC.parse(JsonOps.INSTANCE, json).
                        resultOrPartial(msg -> AnimalPen.LOGGER.error("Error in {}: {}", id, msg)).
                        ifPresent(interaction ->
                            AnimalPenInteractionRegistry.register(entityType, interaction));
                }
            });
        });

        AnimalPen.LOGGER.info(
            "Loaded " + AnimalPenInteractionRegistry.getAll().size() + " animal interaction entries.");
    }


    /**
     * This creates instance of GSON reader for food items.
     */
    private static final Gson GSON = new GsonBuilder().
        registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).
        setLenient().
        create();

    /**
     * The location of data folder.
     */
    private static final String FOLDER = "animal_interactions";
}