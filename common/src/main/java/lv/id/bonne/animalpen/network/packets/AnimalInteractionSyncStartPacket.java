package lv.id.bonne.animalpen.network.packets;


import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.minecraft.network.FriendlyByteBuf;


/**
 * This packet clears local interaction registry and sets entity count for validation.
 *
 * @param entityCount The entity count player should receive.
 */
public record AnimalInteractionSyncStartPacket(int entityCount)
{
    public static void encode(AnimalInteractionSyncStartPacket pkt, FriendlyByteBuf buf)
    {
        buf.writeVarInt(pkt.entityCount());
    }


    public static AnimalInteractionSyncStartPacket decode(FriendlyByteBuf buf)
    {
        return new AnimalInteractionSyncStartPacket(buf.readVarInt());
    }


    public static void handle(AnimalInteractionSyncStartPacket pkt, Supplier<NetworkManager.PacketContext> ctx)
    {
        ctx.get().queue(() ->
        {
            AnimalPenInteractionRegistry.clear();
            AnimalPenInteractionRegistry.setEntityCount(pkt.entityCount());
        });
    }
}