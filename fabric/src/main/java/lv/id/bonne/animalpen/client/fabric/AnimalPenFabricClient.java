package lv.id.bonne.animalpen.client.fabric;


import java.util.List;

import lv.id.bonne.animalpen.client.AnimalPenClient;
import lv.id.bonne.animalpen.client.WaterTankColor;
import lv.id.bonne.animalpen.events.CommonClientEvents;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;


public final class AnimalPenFabricClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        AnimalPenClient.init();

        BlockColorRegistry.register(List.of(new WaterTankColor()), AnimalPenBlockRegistry.AQUARIUM.get());

        UseBlockCallback.EVENT.register((player, world, hand, hit) ->
            CommonClientEvents.onRightClickBlock(player, world, hit.getBlockPos()));
    }
}
