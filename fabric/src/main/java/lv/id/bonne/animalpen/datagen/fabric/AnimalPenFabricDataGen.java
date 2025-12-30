package lv.id.bonne.animalpen.datagen.fabric;


import lv.id.bonne.animalpen.data.provider.AnimalInteractionProvider;
import lv.id.bonne.animalpen.data.provider.ModBlockTagsProvider;
import lv.id.bonne.animalpen.data.provider.ModEntityTypeTagsProvider;
import lv.id.bonne.animalpen.data.provider.ModItemTagsProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;


public class AnimalPenFabricDataGen implements DataGeneratorEntrypoint
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
    {
        fabricDataGenerator.addProvider(AnimalInteractionProvider::new);

        ModBlockTagsProvider blockTags =
            fabricDataGenerator.addProvider(ModBlockTagsProvider::new);
        fabricDataGenerator.addProvider((FabricDataGenerator generator) ->
            new ModItemTagsProvider(generator, blockTags));

        fabricDataGenerator.addProvider(ModEntityTypeTagsProvider::new);
    }
}