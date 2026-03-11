package lv.id.bonne.animalpen.data.provider.neoforge;


import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.data.provider.AnimalInteractionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;


@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class AnimalPenNeoForgeDataGen
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new AnimalInteractionProvider(output, lookupProvider));

        NeoForgeModBlockTagProvider blockTags = new NeoForgeModBlockTagProvider(output,
            lookupProvider,
            AnimalPen.MOD_ID,
            event.getExistingFileHelper());

        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new NeoForgeModItemTagProvider(output,
            lookupProvider,
            blockTags,
            AnimalPen.MOD_ID,
            event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new NeoForgeModEntityTypeTagProvider(output,
            lookupProvider,
            AnimalPen.MOD_ID,
            event.getExistingFileHelper()));

        generator.addProvider(event.includeServer(), new NeoForgeModRecipeProvider.Runner(output, lookupProvider));

        generator.addProvider(event.includeServer(), new NeoForgeModLootTableProvider(output, lookupProvider));

        generator.addProvider(event.includeServer(),
            new NeoForgeModAdvancementProvider(output, lookupProvider, event.getExistingFileHelper()));
    }
}