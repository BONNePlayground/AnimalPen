package lv.id.bonne.animalpen.datagen.forge;


import lv.id.bonne.animalpen.data.provider.AnimalInteractionProvider;
import lv.id.bonne.animalpen.data.provider.ModBlockTagsProvider;
import lv.id.bonne.animalpen.data.provider.ModEntityTypeTagsProvider;
import lv.id.bonne.animalpen.data.provider.ModItemTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
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

            ModBlockTagsProvider blockTags = new ModBlockTagsProvider(generator);
            generator.addProvider(blockTags);
            generator.addProvider(new ModItemTagsProvider(generator, blockTags));

            generator.addProvider(new ModEntityTypeTagsProvider(generator));
        }
    }
}