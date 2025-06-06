//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.registries.AnimalPenFoodRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;


/**
 * This packet is used to sync food registry data from server to client.
 * @param foodRegistryMap The food registry map.
 */
public record AnimalFoodRegistryData(Map<ResourceLocation, AnimalPenFoodRegistry.AnimalFoodData> foodRegistryMap)
{
    public static void encode(AnimalFoodRegistryData packet, FriendlyByteBuf buffer)
    {
        buffer.writeInt(packet.foodRegistryMap().size());

        packet.foodRegistryMap().forEach((key, value) ->
        {
            buffer.writeResourceLocation(key);
            value.ingredient().toNetwork(buffer);
        });
    }


    public static AnimalFoodRegistryData decode(FriendlyByteBuf buffer)
    {
        int size = buffer.readInt();

        Map<ResourceLocation, AnimalPenFoodRegistry.AnimalFoodData> map = new HashMap<>();

        for (int i = 0; i < size; i++)
        {
            map.put(buffer.readResourceLocation(),
                new AnimalPenFoodRegistry.AnimalFoodData(Ingredient.fromNetwork(buffer)));
        }

        return new AnimalFoodRegistryData(map);
    }


    public void handle(Supplier<NetworkManager.PacketContext> context)
    {
        context.get().queue(() -> AnimalPenFoodRegistry.setSyncedData(this.foodRegistryMap));
    }


    public static AnimalFoodRegistryData serverData()
    {
        return new AnimalFoodRegistryData(AnimalPenFoodRegistry.getAll());
    }
}