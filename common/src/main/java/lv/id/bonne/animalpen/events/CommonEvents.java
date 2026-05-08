//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.events;


import java.util.List;
import java.util.Map;

import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.network.packets.AnimalInteractionSyncEndPacket;
import lv.id.bonne.animalpen.network.packets.AnimalInteractionSyncEntityPacket;
import lv.id.bonne.animalpen.network.packets.AnimalInteractionSyncStartPacket;
import lv.id.bonne.animalpen.platform.Services;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;


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
}
