//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import java.util.HashMap;
import java.util.Map;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenFoodRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;


public class AnimalFoodRegistryData
{
    /**
     * The encoding of the packet.
     * @return packet buffer.
     */
    public static FriendlyByteBuf encode()
    {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        Map<ResourceLocation, AnimalPenFoodRegistry.AnimalFoodData> foodRegistry = AnimalPenFoodRegistry.getAll();

        buffer.writeInt(foodRegistry.size());

        for (Map.Entry<ResourceLocation, AnimalPenFoodRegistry.AnimalFoodData> entry : foodRegistry.entrySet())
        {
            buffer.writeResourceLocation(entry.getKey());
            entry.getValue().ingredient().toNetwork(buffer);
        }

        return buffer;
    }


    /**
     * This method handles incoming packet on server.
     * @param buffer The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(FriendlyByteBuf buffer, NetworkManager.PacketContext packetContext)
    {
        int size = buffer.readInt();

        Map<ResourceLocation, AnimalPenFoodRegistry.AnimalFoodData> map = new HashMap<>();

        for (int i = 0; i < size; i++)
        {
            map.put(buffer.readResourceLocation(),
                new AnimalPenFoodRegistry.AnimalFoodData(Ingredient.fromNetwork(buffer)));
        }

        packetContext.queue(() -> AnimalPenFoodRegistry.setSyncedData(map));
    }


    /**
     * The resource ID.
     */
    public static final ResourceLocation ID = new ResourceLocation(AnimalPen.MOD_ID, "animal_food_registry_sync");
}
