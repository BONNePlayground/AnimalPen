package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;
import java.util.Objects;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;


/**
 * This is validation packet to check if player received all data from it.
 */
public record AnimalInteractionSyncEndPacket() implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param player The packet context.
     */
    public static void handle(AnimalInteractionSyncEndPacket data, Player player)
    {
        if (!player.level().isClientSide())
        {
            return;
        }

        if (!AnimalPenInteractionRegistry.containsAllEntities())
        {
            AnimalPen.LOGGER.error("Entity count mismatch between server and client.");
            Objects.requireNonNull(Minecraft.getInstance().player).sendOverlayMessage(
                Component.translatable("network.animal_pen.missing_entities_form_server"));
        }
    }


    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return AnimalInteractionSyncEndPacket.ID;
    }


    public static final CustomPacketPayload.Type<AnimalInteractionSyncEndPacket> ID =
        new CustomPacketPayload.Type<>(AnimalPen.resourceOf("end_data_sync"));


    public static final StreamCodec<RegistryFriendlyByteBuf, AnimalInteractionSyncEndPacket> STREAM_CODEC =
        CustomPacketPayload.codec(
            (o1, o2) -> {},
            object -> new AnimalInteractionSyncEndPacket());
}