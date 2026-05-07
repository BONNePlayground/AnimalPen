//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.client;


import lv.id.bonne.animalpen.blocks.renderer.AnimalPenRenderer;
import lv.id.bonne.animalpen.blocks.renderer.AquariumRenderer;
import lv.id.bonne.animalpen.blocks.renderer.AviaryRenderer;
import lv.id.bonne.animalpen.registries.AnimalPenMobAnimationsRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;


public class AnimalPenClient
{
    public static void init()
    {
        BlockEntityRenderers.register(AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get(),
            AnimalPenRenderer::new);
        BlockEntityRenderers.register(AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get(),
            AquariumRenderer::new);
        BlockEntityRenderers.register(AnimalPenTileEntityRegistry.AVIARY_TILE_ENTITY.get(),
            AviaryRenderer::new);

        // Register custom animations for each entity.
        AnimalPenMobAnimationsRegistry.init();
    }
}
