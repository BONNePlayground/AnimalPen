//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.client;


import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import lv.id.bonne.animalpen.blocks.renderer.AnimalPenRenderer;
import lv.id.bonne.animalpen.blocks.renderer.AquariumRenderer;
import lv.id.bonne.animalpen.platform.ClientPlatformHelper;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;


public class AnimalPenClient
{
    public static void init()
    {
        ClientPlatformHelper.registerBlockEntityRenderer(AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get(),
            context -> new AnimalPenRenderer());
        ClientPlatformHelper.registerBlockEntityRenderer(AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get(),
            context -> new AquariumRenderer());

        ClientPlatformHelper.setRenderLayer(AnimalPenBlockRegistry.AQUARIUM, RenderType.cutout());
        ColorHandlerRegistry.registerBlockColors(new WaterTankColor(), AnimalPenBlockRegistry.AQUARIUM);
    }
}
