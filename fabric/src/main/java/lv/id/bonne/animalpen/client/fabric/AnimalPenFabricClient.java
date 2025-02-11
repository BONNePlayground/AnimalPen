package lv.id.bonne.animalpen.client.fabric;

import lv.id.bonne.animalpen.client.AnimalPenClient;
import net.fabricmc.api.ClientModInitializer;

public final class AnimalPenFabricClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        AnimalPenClient.init();
    }
}
