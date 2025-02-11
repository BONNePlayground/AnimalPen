package lv.id.bonne.animalpen.forge;

import lv.id.bonne.animalpen.AnimalPen;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AnimalPen.MOD_ID)
public final class AnimalPenForge
{
    public AnimalPenForge()
    {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(AnimalPen.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        AnimalPen.init();
    }
}
