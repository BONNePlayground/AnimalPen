package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;


/**
 * This is a simple packet send from client to server to indicate that animal variant is changed.
 */
public record UpdateDisplayAnimalData(BlockPos position, CompoundTag tag) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(UpdateDisplayAnimalData data, NetworkManager.PacketContext packetContext)
    {
        CompoundTag animalVariant = data.tag();
        BlockPos blockPos = data.position();

        packetContext.queue(() ->
        {
            Level level = packetContext.getPlayer().level();

            if (level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity animalPen)
            {
                animalPen.updateAnimalVariant(animalVariant);
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
        ByteBufCodecs.COMPOUND_TAG, UpdateDisplayAnimalData::tag,
        UpdateDisplayAnimalData::new
    );
}
