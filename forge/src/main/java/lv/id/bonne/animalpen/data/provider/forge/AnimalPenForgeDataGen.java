package lv.id.bonne.animalpen.data.provider.forge;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.data.provider.AnimalInteractionProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AnimalPenForgeDataGen
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(event.includeServer(), new AnimalInteractionProvider(generator));

        ForgeModBlockTagProvider blockTags = new ForgeModBlockTagProvider(generator,
            AnimalPen.MOD_ID,
            event.getExistingFileHelper());

        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ForgeModItemTagProvider(generator,
            blockTags,
            AnimalPen.MOD_ID,
            event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new ForgeModEntityTypeTagProvider(generator,
            AnimalPen.MOD_ID,
            event.getExistingFileHelper()));

        generator.addProvider(event.includeServer(), new ForgeModRecipeProvider(generator));

        generator.addProvider(event.includeServer(), new ForgeModLootTableProvider(generator));
    }
}