//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.client;


import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.item.ItemPropertiesRegistry;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.blocks.renderer.AnimalPenRenderer;
import lv.id.bonne.animalpen.blocks.renderer.AquariumRenderer;
import lv.id.bonne.animalpen.blocks.renderer.AviaryRenderer;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;


public class AnimalPenClient
{
    public static void init()
    {
        BlockEntityRendererRegistry.register(AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get(),
            context -> new AnimalPenRenderer());
        BlockEntityRendererRegistry.register(AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get(),
            context -> new AquariumRenderer());
        BlockEntityRendererRegistry.register(AnimalPenTileEntityRegistry.AVIARY_TILE_ENTITY.get(),
            context -> new AviaryRenderer());
        RenderTypeRegistry.register(RenderType.translucent(), AnimalPenBlockRegistry.AQUARIUM.get());
        RenderTypeRegistry.register(RenderType.translucent(), AnimalPenBlockRegistry.AVIARY.get());

        ItemPropertiesRegistry.registerGeneric(AnimalPen.resourceOf("filled_cage"),
            ((itemStack, clientLevel, livingEntity, i) ->
                itemStack.getTag() != null && itemStack.getTag().contains(AnimalPenCompoundTags.TAG_ANIMAL) ?
                    1.0f : 0.0f));

        ColorHandlerRegistry.registerBlockColors(new WaterTankColor(), AnimalPenBlockRegistry.AQUARIUM);

        // Implementation on block to switch screen client side only.
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, interactionHand, blockPos, direction) ->
        {
            if (Minecraft.getInstance().player != player)
            {
                // fixes local host issues.
                return EventResult.pass();
            }

            if (!(player.getLevel().getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity blockEntity))
            {
                return EventResult.pass();
            }

            if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || player.isCrouching())
            {
                return EventResult.pass();
            }

            if (blockEntity.getStoredAnimal().isEmpty())
            {
                return EventResult.pass();
            }

            if (PlayerHooks.isFake(player))
            {
                return EventResult.pass();
            }

            Minecraft.getInstance().execute(() ->
                Minecraft.getInstance().setScreen(new VariantScreenSelection(blockPos)));

            return EventResult.interruptTrue();
        });
    }
}
