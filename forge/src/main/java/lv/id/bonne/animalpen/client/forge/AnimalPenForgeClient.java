package lv.id.bonne.animalpen.client.forge;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.client.AnimalPenClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = AnimalPen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AnimalPenForgeClient
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