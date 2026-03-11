package lv.id.bonne.animalpen.data.provider.forge;


import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.data.provider.AnimalInteractionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
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
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new AnimalInteractionProvider(output, lookupProvider));

        ForgeModBlockTagProvider blockTags = new ForgeModBlockTagProvider(output,
            lookupProvider,
            AnimalPen.MOD_ID,
            event.getExistingFileHelper());

        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ForgeModItemTagProvider(output,
            lookupProvider,
            blockTags,
            AnimalPen.MOD_ID,
            event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new ForgeModEntityTypeTagProvider(output,
            lookupProvider,
            AnimalPen.MOD_ID,
            event.getExistingFileHelper()));

        generator.addProvider(event.includeServer(), new ForgeModRecipeProvider(output));

        generator.addProvider(event.includeServer(), new ForgeModLootTableProvider(output));

        generator.addProvider(event.includeServer(),
            new ForgeModAdvancementProvider(output, event.getExistingFileHelper()));
    }
}