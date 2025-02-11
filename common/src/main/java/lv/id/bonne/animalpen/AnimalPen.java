package lv.id.bonne.animalpen;


import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensCreativeTabRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;


public final class AnimalPen
{
    public static void init()
    {
        // Write common init code here.
        AnimalPensItemRegistry.register();
        AnimalPenBlockRegistry.register();
        AnimalPenTileEntityRegistry.register();
        AnimalPensCreativeTabRegistry.register();
    }


    public static final String MOD_ID = "animal_pen";
}
