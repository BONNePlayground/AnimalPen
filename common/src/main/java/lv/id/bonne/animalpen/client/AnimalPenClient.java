//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.client;


import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.renderer.AnimalPenRenderer;
import lv.id.bonne.animalpen.blocks.renderer.AquariumRenderer;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;


public class AnimalPenClient
{
    public static void init()
    {
        BlockEntityRendererRegistry.register(AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get(),
            context -> new AnimalPenRenderer());
        BlockEntityRendererRegistry.register(AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get(),
            context -> new AquariumRenderer());
        RenderTypeRegistry.register(RenderType.translucent(), AnimalPenBlockRegistry.AQUARIUM.get());

        ItemPropertiesRegistry.registerGeneric(new ResourceLocation(AnimalPen.MOD_ID, "filled_cage"),
            ((itemStack, clientLevel, livingEntity, i) ->
                itemStack.getTag() != null && itemStack.getTag().contains("id") ? 1.0f : 0.0f));

        ColorHandlerRegistry.registerBlockColors(new WaterTankColor(), AnimalPenBlockRegistry.AQUARIUM);
    }
}
