package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;


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
     * @param player The packet context.
     */
    public static void handle(AnimalInteractionSyncStartPacket data, Player player)
    {
        if (!player.level().isClientSide())
        {
            return;
        }

        AnimalPenInteractionRegistry.clear();
        AnimalPenInteractionRegistry.setEntityCount(data.entityCount());
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