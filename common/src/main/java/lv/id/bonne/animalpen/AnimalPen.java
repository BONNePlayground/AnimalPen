package lv.id.bonne.animalpen;


import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import lv.id.bonne.animalpen.config.ConfigurationManager;
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

        AnimalPen.CONFIG_MANAGER.readConfig();
    }


    public static final String MOD_ID = "animal_pen";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ConfigurationManager CONFIG_MANAGER = new ConfigurationManager();
}
