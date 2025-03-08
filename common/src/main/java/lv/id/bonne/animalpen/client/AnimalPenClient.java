//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.client;


import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenBlockInterface;
import lv.id.bonne.animalpen.blocks.renderer.AnimalPenRenderer;
import lv.id.bonne.animalpen.blocks.renderer.AquariumRenderer;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;


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

        // Implementation on block to switch screen client side only.
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, interactionHand, blockPos, direction) ->
        {
            if (!(player.getLevel().getBlockEntity(blockPos) instanceof AnimalPenBlockInterface<?> blockEntity))
            {
                return EventResult.pass();
            }

            if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || player.isCrouching())
            {
                return EventResult.pass();
            }

            if (blockEntity.getStoredAnimal() == null)
            {
                return EventResult.pass();
            }

            Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().setScreen(new VariantScreenSelection(blockPos)));

            return EventResult.interruptTrue();
        });
    }
}
