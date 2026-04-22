package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.fabricmc.api.EnvType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


/**
 * This packet clears local interaction registry and sets entity count for validation.
 *
 * @param entityCount The entity count player should receive.
 */
public record AnimalInteractionSyncStartPacket(int entityCount) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(AnimalInteractionSyncStartPacket data, NetworkManager.PacketContext packetContext)
    {
        if (packetContext.getEnv() == EnvType.SERVER) return;

        packetContext.queue(() ->
        {
            AnimalPenInteractionRegistry.clear();
            AnimalPenInteractionRegistry.setEntityCount(data.entityCount());
        });
    }


    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return AnimalInteractionSyncStartPacket.ID;
    }


    public static final CustomPacketPayload.Type<AnimalInteractionSyncStartPacket> ID =
        new CustomPacketPayload.Type<>(AnimalPen.resourceOf("start_data_sync"));


    public static final StreamCodec<RegistryFriendlyByteBuf, AnimalInteractionSyncStartPacket> STREAM_CODEC =
        StreamCodec.composite(ByteBufCodecs.INT,
            AnimalInteractionSyncStartPacket::entityCount,
            AnimalInteractionSyncStartPacket::new
    );
}