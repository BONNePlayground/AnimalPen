package lv.id.bonne.animalpen.client.neoforge;


import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.client.AnimalPenClient;
import lv.id.bonne.animalpen.client.WaterTankColor;
import lv.id.bonne.animalpen.config.screen.AnimalPenConfigScreen;
import lv.id.bonne.animalpen.events.CommonEvents;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;


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
    public static void onRightClick(PlayerInteractEvent.RightClickBlock e)
    {
        CommonEvents.onRightClickBlock(e.getEntity(), e.getLevel(), e.getPos());
    }


    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event)
    {
        event.register(
            List.of(new WaterTankColor()),
            AnimalPenBlockRegistry.AQUARIUM.get()
        );
    }
}