//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.events;


import java.util.List;
import java.util.Map;

import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.network.packets.AnimalInteractionSyncEndPacket;
import lv.id.bonne.animalpen.network.packets.AnimalInteractionSyncEntityPacket;
import lv.id.bonne.animalpen.network.packets.AnimalInteractionSyncStartPacket;
import lv.id.bonne.animalpen.platform.Services;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;


public class CommonEvents
{
    public static void onPlayerJoin(Player player)
    {
        if (!(player instanceof ServerPlayer serverPlayer))
        {
            return;
        }

        Map<ResourceKey<EntityType<?>>, List<AnimalInteraction>> data = AnimalPenInteractionRegistry.getAll();

        Services.NETWORK.sendToPlayer(serverPlayer,
            new AnimalInteractionSyncStartPacket(data.size()));

        for (var entry : data.entrySet())
        {
            Services.NETWORK.sendToPlayer(serverPlayer,
                new AnimalInteractionSyncEntityPacket(entry.getKey(), entry.getValue()));
        }

        Services.NETWORK.sendToPlayer(serverPlayer, new AnimalInteractionSyncEndPacket());
    }


    public static InteractionResult onRightClickBlock(Player player, Level level, BlockPos blockPos)
    {
        if (Minecraft.getInstance().player != player)
        {
            // fixes local host issues.
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity blockEntity))
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

        if (Services.PLATFORM.isFake(player))
        {
            return InteractionResult.PASS;
        }

        Minecraft.getInstance().execute(() ->
            Minecraft.getInstance().setScreen(new VariantScreenSelection(blockPos)));

        return InteractionResult.SUCCESS;
    }
}
