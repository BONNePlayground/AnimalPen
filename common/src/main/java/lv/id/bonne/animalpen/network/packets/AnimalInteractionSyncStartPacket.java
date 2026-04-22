package lv.id.bonne.animalpen.network.packets;


import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.fabricmc.api.EnvType;
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
        if (ctx.get().getEnv() == EnvType.SERVER) return;

        ctx.get().queue(() ->
        {
            AnimalPenInteractionRegistry.clear();
            AnimalPenInteractionRegistry.setEntityCount(pkt.entityCount());
        });
    }
}