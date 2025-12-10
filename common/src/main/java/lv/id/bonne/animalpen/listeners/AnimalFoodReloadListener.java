package lv.id.bonne.animalpen.listeners;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;

import dev.architectury.platform.Platform;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenFoodRegistry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;


/**
 * This class handles animal food resource data loading into registry.
 */
public class AnimalFoodReloadListener extends SimpleJsonResourceReloadListener<AnimalFoodReloadListener.AnimalFoodEntry>
{
    public static final Codec<AnimalFoodEntry> CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("condition").forGetter(AnimalFoodEntry::condition),
            TaggableIngredient.CODEC.fieldOf("food_items").forGetter(AnimalFoodEntry::ingredient)
        ).apply(instance, AnimalFoodEntry::new));



    // Pass the Codec and folder name to the parent constructor.
    public AnimalFoodReloadListener() {
        super(CODEC, FileToIdConverter.json(FOLDER));
    }


    @Override
    protected void apply(Map<Identifier, AnimalFoodEntry> objectMap, ResourceManager resourceManager, ProfilerFiller profiler)
    {
        // Clear the registry before reloading new data.
        AnimalPenFoodRegistry.clear();

        // Iterate over each decoded file.
        objectMap.forEach((id, entry) ->
        {
            if (entry.condition().isPresent())
            {
                String condition = entry.condition().get();

                if (condition.startsWith("mod:"))
                {
                    String modId = condition.substring(4);

                    if (!modId.isBlank() && !Platform.isModLoaded(modId))
                    {
                        // Skip loading this entry if the required mod isn't loaded.
                        return;
                    }
                }
            }

            if (!entry.ingredient().isEmpty())
            {
                // Create and register your data instance.
                AnimalPenFoodRegistry.AnimalFoodData data = new AnimalPenFoodRegistry.AnimalFoodData(entry.ingredient());
                AnimalPenFoodRegistry.register(id, data);
            }
        });


        AnimalPen.LOGGER.info("Loaded " + AnimalPenFoodRegistry.getAll().size() + " animal food entries.");
    }


    /**
     * This is dummy record to detect if mod condition is present and prevent loading if mod is not loaded.
     * @param condition The optional conditional text
     * @param ingredient The Ingredient parsing.
     */
    public record AnimalFoodEntry(Optional<String> condition, TaggableIngredient ingredient)
    {
    }


    /**
     * The location of data folder.
     */
    private static final String FOLDER = "animal_foods";
}