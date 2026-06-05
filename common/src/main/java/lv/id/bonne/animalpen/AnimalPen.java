package lv.id.bonne.animalpen;


import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import lv.id.bonne.animalpen.config.Configuration;
import lv.id.bonne.animalpen.config.ConfigurationManager;
import lv.id.bonne.animalpen.registries.*;
import net.minecraft.resources.Identifier;



public final class AnimalPen
{
    public static void init()
    {
        // Write common init code here.
        AnimalPenRegistry.register();
        AnimalPenDataComponentRegistry.register();
        AnimalPenBlockRegistry.register();
        AnimalPensItemRegistry.register();
        AnimalPenTileEntityRegistry.register();
        AnimalPenFunctionRegistry.register();
        AnimalPenCriteriaTriggersRegistry.register();
        AnimalPensCreativeTabRegistry.register();

        AnimalPen.CONFIG_MANAGER.readConfig();
    }


    public static Configuration config()
    {
        return CONFIG_MANAGER.getConfiguration();
    }


    public static void sendDebug(String message)
    {
        if (AnimalPen.config().isDebug())
        {
            AnimalPen.LOGGER.debug(message);
        }
    }


    public static Identifier resourceOf(String key)
    {
        return Identifier.tryBuild(AnimalPen.MOD_ID, key);
    }

    public static final String MOD_ID = "animal_pen";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ConfigurationManager CONFIG_MANAGER = new ConfigurationManager();
}
