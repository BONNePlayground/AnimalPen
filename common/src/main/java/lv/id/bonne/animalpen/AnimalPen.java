package lv.id.bonne.animalpen;


import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
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


        EnvExecutor.runInEnv(Env.CLIENT, () -> AnimalPen::initializeClient);
        EnvExecutor.runInEnv(Env.SERVER, () -> AnimalPen::initializeServer);
    }


    private static void initializeCommon()
    {
    }


    private static void initializeClient()
    {
    }


    private static void initializeServer()
    {
    }


    public static final String MOD_ID = "animal_pen";
}
