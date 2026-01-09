package lv.id.bonne.animalpen.data.listener;


import java.util.Map;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;


/**
 * This class handles animal interaction resource data loading into registry.
 */
public class AnimalInteractionReloadListener extends SimpleJsonResourceReloadListener<AnimalInteractionEntry>
{
    /**
     * Instantiates a new Animal interaction reload listener.
     */
    public AnimalInteractionReloadListener()
    {
        super(AnimalInteractionEntry.CODEC, FileToIdConverter.json(FOLDER));
    }


    @Override
    protected void apply(Map<Identifier, AnimalInteractionEntry> objectMap,
        ResourceManager resourceManager,
        ProfilerFiller profiler)
    {
        // Clear the registry before reloading new data.
        AnimalPenInteractionRegistry.clear();

        objectMap.forEach((id, element) ->
        {
            element.entityType().ifPresent(entityType -> {
                element.interactions().forEach(animalInteraction ->
                    AnimalPenInteractionRegistry.register(entityType,
                        animalInteraction));
            });
        });

        AnimalPen.LOGGER.info(
            "Loaded " + AnimalPenInteractionRegistry.getAll().size() + " animal interaction entries.");
    }


    /**
     * The location of data folder.
     */
    private static final String FOLDER = "animal_interactions";
}