//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.client;


import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.architectury.networking.NetworkManager;
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
import lv.id.bonne.animalpen.network.packets.AnimalInteractionSyncEndPacket;
import lv.id.bonne.animalpen.network.packets.AnimalInteractionSyncEntityPacket;
import lv.id.bonne.animalpen.network.packets.AnimalInteractionSyncStartPacket;
import lv.id.bonne.animalpen.network.packets.UpdateVariantScreenData;
import lv.id.bonne.animalpen.registries.AnimalPenMobAnimationsRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
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
        RenderTypeRegistry.register(RenderType.translucent(), AnimalPenBlockRegistry.GOLD_AVIARY.get());

        AnimalPenBlockRegistry.COPPER_AVIARIES.values().forEach(
            aviary -> RenderTypeRegistry.register(RenderType.translucent(),
                aviary.get()));
        AnimalPenBlockRegistry.WAXED_COPPER_AVIARIES.values().forEach(
            copperAviary -> RenderTypeRegistry.register(RenderType.translucent(),
                copperAviary.get()));

        ItemPropertiesRegistry.registerGeneric(AnimalPen.resourceOf("filled_cage"),
            ((itemStack, clientLevel, livingEntity, i) ->
                itemStack.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()) ? 1.0f : 0.0f));

        ColorHandlerRegistry.registerBlockColors(new WaterTankColor(), AnimalPenBlockRegistry.AQUARIUM);

        // Implementation on block to switch screen client side only.
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, interactionHand, blockPos, direction) ->
        {
            if (Minecraft.getInstance().player != player)
            {
                // fixes local host issues.
                return EventResult.pass();
            }

            if (!(player.level().getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity blockEntity))
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

        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
            AnimalInteractionSyncStartPacket.ID,
            AnimalInteractionSyncStartPacket.STREAM_CODEC,
            AnimalInteractionSyncStartPacket::handle);

        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
            AnimalInteractionSyncEntityPacket.ID,
            AnimalInteractionSyncEntityPacket.STREAM_CODEC,
            AnimalInteractionSyncEntityPacket::handle);

        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
            AnimalInteractionSyncEndPacket.ID,
            AnimalInteractionSyncEndPacket.STREAM_CODEC,
            AnimalInteractionSyncEndPacket::handle);

        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
            UpdateVariantScreenData.ID,
            UpdateVariantScreenData.STREAM_CODEC,
            UpdateVariantScreenData::handle);

        // Register custom animations for each entity.
        AnimalPenMobAnimationsRegistry.init();
    }
}
