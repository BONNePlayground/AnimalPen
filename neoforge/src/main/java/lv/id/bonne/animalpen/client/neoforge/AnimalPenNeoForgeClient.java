package lv.id.bonne.animalpen.client.neoforge;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.client.AnimalPenClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;


@EventBusSubscriber(modid = AnimalPen.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AnimalPenNeoForgeClient
{
    @SubscribeEvent
    public static void init(FMLClientSetupEvent event)
    {
        AnimalPenClient.init();
    }


    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {

    }
}