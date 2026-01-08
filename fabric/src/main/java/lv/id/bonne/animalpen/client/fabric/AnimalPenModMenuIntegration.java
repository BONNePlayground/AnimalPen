package lv.id.bonne.animalpen.client.fabric;


import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import lv.id.bonne.animalpen.config.screen.AnimalPenConfigScreen;
import net.fabricmc.loader.api.FabricLoader;


public class AnimalPenModMenuIntegration implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config"))
        {
            return null;
        }

        return AnimalPenConfigScreen::createConfigScreen;
    }
}