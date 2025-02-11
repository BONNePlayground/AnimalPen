//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.client;


import lv.id.bonne.animalpen.blocks.renderer.AnimalPenRenderer;
import lv.id.bonne.animalpen.platform.ClientPlatformHelper;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;


public class AnimalPenClient
{
    public static void init()
    {
        ClientPlatformHelper.registerBlockEntityRenderer(AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get(),
            context -> new AnimalPenRenderer());
    }
}
