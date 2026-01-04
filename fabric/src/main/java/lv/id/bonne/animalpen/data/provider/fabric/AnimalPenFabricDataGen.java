package lv.id.bonne.animalpen.data.provider.fabric;


import lv.id.bonne.animalpen.data.provider.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;


public class AnimalPenFabricDataGen implements DataGeneratorEntrypoint
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
    {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(AnimalInteractionProvider::new);

        FabricModBlockTagProvider blockTagProvider =
            pack.addProvider(FabricModBlockTagProvider::new);
        pack.addProvider((generator, feature) ->
            new FabricModItemTagProvider(generator, feature, blockTagProvider));
        pack.addProvider(FabricModEntityTypeTagProvider::new);

        pack.addProvider(FabricModRecipeProvider::new);

        pack.addProvider(FabricBlockLootProvider::new);
    }
}