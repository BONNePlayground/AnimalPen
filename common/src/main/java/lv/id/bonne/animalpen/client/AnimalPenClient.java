//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.client;


import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenBlockInterface;
import lv.id.bonne.animalpen.blocks.renderer.AnimalPenRenderer;
import lv.id.bonne.animalpen.blocks.renderer.AquariumRenderer;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;


public class AnimalPenClient
{
    public static void init()
    {
        BlockEntityRendererRegistry.register(AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get(),
            context -> new AnimalPenRenderer());
        BlockEntityRendererRegistry.register(AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get(),
            context -> new AquariumRenderer());
        RenderTypeRegistry.register(RenderType.translucent(), AnimalPenBlockRegistry.AQUARIUM.get());

        ColorHandlerRegistry.registerBlockColors(new WaterTankColor(), AnimalPenBlockRegistry.AQUARIUM);

        // Implementation on block to switch screen client side only.
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, interactionHand, blockPos, direction) ->
        {
            if (!(player.level().getBlockEntity(blockPos) instanceof AnimalPenBlockInterface<?> blockEntity))
            {
                return InteractionResult.PASS;
            }

            if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || player.isCrouching())
            {
                return InteractionResult.PASS;
            }

            if (blockEntity.getStoredAnimal() == null)
            {
                return InteractionResult.PASS;
            }

            if (PlayerHooks.isFake(player))
            {
                return InteractionResult.PASS;
            }

            Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().setScreen(new VariantScreenSelection(blockPos)));

            return InteractionResult.SUCCESS;
        });
    }
}
