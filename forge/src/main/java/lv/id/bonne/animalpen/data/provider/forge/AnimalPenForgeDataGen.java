package lv.id.bonne.animalpen.data.provider.forge;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.data.provider.AnimalInteractionProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AnimalPenForgeDataGen
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();

        if (event.includeServer())
        {
            generator.addProvider(new AnimalInteractionProvider(generator));

            ForgeModBlockTagProvider blockTags = new ForgeModBlockTagProvider(generator,
                AnimalPen.MOD_ID,
                event.getExistingFileHelper());

            generator.addProvider(blockTags);
            generator.addProvider(new ForgeModItemTagProvider(generator,
                blockTags,
                AnimalPen.MOD_ID,
                event.getExistingFileHelper()));
            generator.addProvider(new ForgeModEntityTypeTagProvider(generator,
                AnimalPen.MOD_ID,
                event.getExistingFileHelper()));

            generator.addProvider(new ForgeModRecipeProvider(generator));

            generator.addProvider(new ForgeModLootTableProvider(generator));
        }
    }
}