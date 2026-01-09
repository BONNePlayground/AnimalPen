package lv.id.bonne.animalpen.client.neoforge;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.client.AnimalPenClient;
import lv.id.bonne.animalpen.config.screen.AnimalPenConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;


@EventBusSubscriber(modid = AnimalPen.MOD_ID, value = Dist.CLIENT)
public class AnimalPenNeoForgeClient
{
    @SubscribeEvent
    public static void init(FMLClientSetupEvent event)
    {
        AnimalPenClient.init();

        if (!ModList.get().isLoaded("cloth_config"))
        {
            return;
        }

        ModLoadingContext.get().registerExtensionPoint(
            IConfigScreenFactory.class,
            () -> (arg, screen) -> AnimalPenConfigScreen.createConfigScreen(screen)
        );
    }


    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {

    }
}