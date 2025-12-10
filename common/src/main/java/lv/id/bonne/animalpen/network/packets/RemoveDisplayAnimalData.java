package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenBlockInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;


/**
 * This is a simple packet send from client to server to indicate that animal variant is removed.
 */
public record RemoveDisplayAnimalData(BlockPos position, int index) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(RemoveDisplayAnimalData data, NetworkManager.PacketContext packetContext)
    {
        BlockPos blockPos = data.position();
        int index = data.index();

        packetContext.queue(() ->
        {
            Level level = packetContext.getPlayer().level();

            if (level.getBlockEntity(blockPos) instanceof AnimalPenBlockInterface<?> animalPen)
            {
                animalPen.removeAnimalVariant(index);
            }
            else
            {
                AnimalPen.LOGGER.error("Block entity not found at the position!");
            }
        });
    }


    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type()
    {
        return RemoveDisplayAnimalData.ID;
    }


    public static final Type<RemoveDisplayAnimalData> ID =
        new Type<>(Identifier.fromNamespaceAndPath(AnimalPen.MOD_ID, "remove_display_animal"));


    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveDisplayAnimalData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RemoveDisplayAnimalData::position,
        ByteBufCodecs.INT, RemoveDisplayAnimalData::index,
        RemoveDisplayAnimalData::new
    );
}
