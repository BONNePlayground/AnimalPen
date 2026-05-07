package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;


/**
 * This is a simple packet send from client to server to indicate that animal variant is removed.
 */
public record RemoveDisplayAnimalData(BlockPos position, int index) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param player The packet context.
     */
    public static void handle(RemoveDisplayAnimalData data, ServerPlayer player)
    {
        BlockPos blockPos = data.position();
        int index = data.index();

        ServerLevel level = player.level();

        if (level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity animalPen)
        {
            if (animalPen.getOwner().isEmpty() ||
                animalPen.getOwner().get().equals(player.getUUID()))
            {
                animalPen.removeAnimalVariant(index);
            }
        }
        else
        {
            AnimalPen.LOGGER.error("Block entity not found at the position!");
        }
    }


    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type()
    {
        return RemoveDisplayAnimalData.ID;
    }


    public static final Type<RemoveDisplayAnimalData> ID =
        new Type<>(AnimalPen.resourceOf("remove_display_animal"));


    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveDisplayAnimalData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RemoveDisplayAnimalData::position,
        ByteBufCodecs.INT, RemoveDisplayAnimalData::index,
        RemoveDisplayAnimalData::new
    );
}
