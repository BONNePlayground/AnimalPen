package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;
import java.util.List;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;


/**
 * This is a packet that sync given entity and its interaction with client.
 *
 * @param entityId The id of entity.
 * @param interactions The list of interactions for entity.
 */
public record AnimalInteractionSyncEntityPacket(ResourceKey<EntityType<?>> entityId,
                                                List<AnimalInteraction> interactions) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(AnimalInteractionSyncEntityPacket data, NetworkManager.PacketContext packetContext)
    {
        packetContext.queue(() ->
            AnimalPenInteractionRegistry.register(data.entityId(), data.interactions()));
    }


    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return AnimalInteractionSyncStartPacket.ID;
    }


    public static final CustomPacketPayload.Type<AnimalInteractionSyncEntityPacket> ID =
        new CustomPacketPayload.Type<>(AnimalPen.resourceOf("perform_data_sync"));


    public static final StreamCodec<FriendlyByteBuf, AnimalInteractionSyncEntityPacket> STREAM_CODEC =
        StreamCodec.composite(
            ResourceKey.streamCodec(Registries.ENTITY_TYPE),
            AnimalInteractionSyncEntityPacket::entityId,
            ByteBufCodecs.fromCodec(AnimalInteraction.CODEC.listOf()),
            AnimalInteractionSyncEntityPacket::interactions,
            AnimalInteractionSyncEntityPacket::new
        );
}