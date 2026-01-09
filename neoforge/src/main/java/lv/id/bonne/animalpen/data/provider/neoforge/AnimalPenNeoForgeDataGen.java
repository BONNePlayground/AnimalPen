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


@EventBusSubscriber
public class AnimalPenNeoForgeDataGen
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Server event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new AnimalInteractionProvider(output, lookupProvider));

        NeoForgeModBlockTagProvider blockTags = new NeoForgeModBlockTagProvider(output,
            lookupProvider,
            AnimalPen.MOD_ID);

        generator.addProvider(true, blockTags);
        generator.addProvider(true, new NeoForgeModItemTagProvider(output,
            lookupProvider,
            AnimalPen.MOD_ID));
        generator.addProvider(true, new NeoForgeModBlockTagCopyingItemTagProvider(output,
            lookupProvider,
            blockTags.contentsGetter(),
            AnimalPen.MOD_ID));
        generator.addProvider(true, new NeoForgeModEntityTypeTagProvider(output,
            lookupProvider,
            AnimalPen.MOD_ID));

        generator.addProvider(true, new NeoForgeModRecipeProvider.Runner(output, lookupProvider));

        generator.addProvider(true, new NeoForgeModLootTableProvider(output, lookupProvider));
    }
}