package lv.id.bonne.animalpen.client.fabric;

import lv.id.bonne.animalpen.client.AnimalPenClient;
import lv.id.bonne.animalpen.client.screens.renderer.pip.DecorationButtonRenderer;
import lv.id.bonne.animalpen.client.screens.renderer.pip.DecorationGUIRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;


public final class AnimalPenFabricClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        AnimalPenClient.init();
        SpecialGuiElementRegistry.register(ctx -> new DecorationButtonRenderer(ctx.vertexConsumers()));
        SpecialGuiElementRegistry.register(ctx -> new DecorationGUIRenderer(ctx.vertexConsumers()));
    }
}
