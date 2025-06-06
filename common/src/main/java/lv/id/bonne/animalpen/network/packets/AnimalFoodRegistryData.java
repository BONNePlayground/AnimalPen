//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenFoodRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record AnimalFoodRegistryData(Map<ResourceLocation, AnimalPenFoodRegistry.AnimalFoodData> data) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(AnimalFoodRegistryData data, NetworkManager.PacketContext packetContext)
    {
        packetContext.queue(() -> AnimalPenFoodRegistry.setSyncedData(data.data()));
    }


    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return AnimalFoodRegistryData.ID;
    }


    public static final CustomPacketPayload.Type<AnimalFoodRegistryData> ID =
        new CustomPacketPayload.Type<>(new ResourceLocation(AnimalPen.MOD_ID, "animal_food_registry_sync"));


    public static final StreamCodec<RegistryFriendlyByteBuf, AnimalFoodRegistryData> STREAM_CODEC =
        StreamCodec.of(
            (buf, registryData) -> {
                Map<ResourceLocation, AnimalPenFoodRegistry.AnimalFoodData> map = registryData.data();
                buf.writeVarInt(map.size());
                for (Map.Entry<ResourceLocation, AnimalPenFoodRegistry.AnimalFoodData> entry : map.entrySet()) {
                    ResourceLocation.STREAM_CODEC.encode(buf, entry.getKey());
                    AnimalPenFoodRegistry.AnimalFoodData.STREAM_CODEC.encode(buf, entry.getValue());
                }
            },
            buf -> {
                int size = buf.readVarInt();
                Map<ResourceLocation, AnimalPenFoodRegistry.AnimalFoodData> map = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    ResourceLocation key = ResourceLocation.STREAM_CODEC.decode(buf);
                    AnimalPenFoodRegistry.AnimalFoodData value = AnimalPenFoodRegistry.AnimalFoodData.STREAM_CODEC.decode(buf);
                    map.put(key, value);
                }
                return new AnimalFoodRegistryData(map);
            }
        );
}