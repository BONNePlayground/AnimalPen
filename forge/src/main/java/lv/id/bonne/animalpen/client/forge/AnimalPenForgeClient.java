package lv.id.bonne.animalpen.client.forge;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.client.AnimalPenClient;
import lv.id.bonne.animalpen.config.screen.AnimalPenConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = AnimalPen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AnimalPenForgeClient
{
    @SubscribeEvent
    public static void init(FMLClientSetupEvent event)
    {
        AnimalPenClient.init();

        if (!ModList.get().isLoaded("cloth_config")) {
            return;
        }

        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (minecraft, screen) -> AnimalPenConfigScreen.createConfigScreen(screen))
        );
    }


    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {

    }
}