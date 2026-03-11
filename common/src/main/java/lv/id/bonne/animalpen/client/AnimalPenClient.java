//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.client;


import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
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
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
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
        BlockEntityRendererRegistry.register(AnimalPenTileEntityRegistry.AVIARY_TILE_ENTITY.get(),
            context -> new AviaryRenderer());
        RenderTypeRegistry.register(ChunkSectionLayer.TRANSLUCENT, AnimalPenBlockRegistry.AQUARIUM.get());
        RenderTypeRegistry.register(ChunkSectionLayer.TRANSLUCENT, AnimalPenBlockRegistry.AVIARY.get());
        RenderTypeRegistry.register(ChunkSectionLayer.TRANSLUCENT, AnimalPenBlockRegistry.GOLD_AVIARY.get());

        AnimalPenBlockRegistry.COPPER_AVIARIES.values().forEach(
            aviary -> RenderTypeRegistry.register(ChunkSectionLayer.TRANSLUCENT,
                aviary.get()));
        AnimalPenBlockRegistry.WAXED_COPPER_AVIARIES.values().forEach(
            copperAviary -> RenderTypeRegistry.register(ChunkSectionLayer.TRANSLUCENT,
                copperAviary.get()));

        ColorHandlerRegistry.registerBlockColors(new WaterTankColor(), AnimalPenBlockRegistry.AQUARIUM);

        // Implementation on block to switch screen client side only.
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, interactionHand, blockPos, direction) ->
        {
            if (Minecraft.getInstance().player != player)
            {
                // fixes local host issues.
                return InteractionResult.PASS;
            }

            if (!(player.level().getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity blockEntity))
            {
                return InteractionResult.PASS;
            }

            if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || player.isCrouching())
            {
                return InteractionResult.PASS;
            }

            if (blockEntity.getStoredAnimal().isEmpty())
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
