package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;


/**
 * This packet request all animal variants stored in given block pos.
 */
public record RequestVariantData(BlockPos blockPos) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(RequestVariantData data, NetworkManager.PacketContext packetContext)
    {
        BlockPos blockPos = data.blockPos();

        packetContext.queue(() ->
        {
            if (!(packetContext.getPlayer() instanceof ServerPlayer player))
            {
                return;
            }

            if (player.blockPosition().distSqr(blockPos) > 25)
            {
                // Too far from player.
                return;
            }

            if (player.level().getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity animalPen)
            {
                ListTag variants = animalPen.getEntityVariants();

                // Reply back to the client
                NetworkManager.sendToPlayer(player, new UpdateVariantScreenData(blockPos, variants));
            }
            else
            {
                AnimalPen.LOGGER.error("Block entity not found at the position!");
            }
        });
    }


    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return RequestVariantData.ID;
    }


    public static final CustomPacketPayload.Type<RequestVariantData> ID =
        new CustomPacketPayload.Type<>(AnimalPen.resourceOf("request_variants"));


    public static final StreamCodec<RegistryFriendlyByteBuf, RequestVariantData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RequestVariantData::blockPos,
        RequestVariantData::new
    );
}