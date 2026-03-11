package lv.id.bonne.animalpen.data.provider.fabric;


import lv.id.bonne.animalpen.data.provider.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;


public class AnimalPenFabricDataGen implements DataGeneratorEntrypoint
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
    {
        fabricDataGenerator.addProvider(AnimalInteractionProvider::new);

        FabricModBlockTagProvider blockTagProvider =
            fabricDataGenerator.addProvider(FabricModBlockTagProvider::new);
        fabricDataGenerator.addProvider((FabricDataGenerator dataGenerator) ->
            new FabricModItemTagProvider(dataGenerator, blockTagProvider));
        fabricDataGenerator.addProvider(FabricModEntityTypeTagProvider::new);

        fabricDataGenerator.addProvider(FabricModRecipeProvider::new);

        fabricDataGenerator.addProvider(FabricBlockLootProvider::new);
        fabricDataGenerator.addProvider(FabricModAdvancementProvider::new);
    }
}