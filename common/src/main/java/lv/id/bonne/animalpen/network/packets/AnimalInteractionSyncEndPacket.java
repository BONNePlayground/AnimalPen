package lv.id.bonne.animalpen.network.packets;


import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;


/**
 * This is validation packet to check if player received all data from it.
 */
public record AnimalInteractionSyncEndPacket()
{
    public static void encode(AnimalInteractionSyncEndPacket pkt, FriendlyByteBuf buf)
    {
    }


    public static AnimalInteractionSyncEndPacket decode(FriendlyByteBuf buf)
    {
        return new AnimalInteractionSyncEndPacket();
    }


    public static void handle(AnimalInteractionSyncEndPacket pkt, Supplier<NetworkManager.PacketContext> ctx)
    {
        ctx.get().queue(() ->
        {
            if (!AnimalPenInteractionRegistry.containsAllEntities())
            {
                AnimalPen.LOGGER.error("Entity count mismatch between server and client.");
                Objects.requireNonNull(Minecraft.getInstance().player).sendMessage(
                    new TranslatableComponent("network.animal_pen.missing_entities_form_server"),
                    UUID.randomUUID());
            }
        });
    }
}