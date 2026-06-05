package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;


/**
 * This is a simple packet send from client to server to indicate that animal variant is changed.
 */
public record UpdateDisplayAnimalData(BlockPos position, int index) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(UpdateDisplayAnimalData data, NetworkManager.PacketContext packetContext)
    {
        int index = data.index();
        BlockPos blockPos = data.position();

        packetContext.queue(() ->
        {
            if (packetContext.getPlayer() instanceof ServerPlayer serverPlayer &&
                serverPlayer.level().getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity animalPen)
            {
                if (animalPen.getOwner().isPresent() &&
                    !animalPen.getOwner().get().equals(serverPlayer.getUUID()))
                {
                    return;
                }

                animalPen.updateAnimalVariant(serverPlayer, index);
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
        return UpdateDisplayAnimalData.ID;
    }


    public static final Type<UpdateDisplayAnimalData> ID =
        new Type<>(AnimalPen.resourceOf("update_display_animal"));


    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDisplayAnimalData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, UpdateDisplayAnimalData::position,
        ByteBufCodecs.VAR_INT, UpdateDisplayAnimalData::index,
        UpdateDisplayAnimalData::new
    );
}
