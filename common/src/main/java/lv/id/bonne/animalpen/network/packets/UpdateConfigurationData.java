package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenBlockInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;


/**
 * This is a simple packet send from client to server to indicate that animal display size is changed.
 */
public record UpdateAnimalSizeData(BlockPos position, long size, long protectedAmount, booleand hasUUID, UUID uuid) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(UpdateAnimalSizeData data, NetworkManager.PacketContext packetContext)
    {
        BlockPos blockPos = data.position();
        long displaySize = friendlyByteBuf.readLong();
        long protectedAmount = data.size();
        boolean hasUUID = friendlyByteBuf.readBoolean();
        UUID owner = hasUUID ? friendlyByteBuf.readUUID() : null;

        packetContext.queue(() ->
        {
            Level level = packetContext.getPlayer().level();

            if (level.getBlockEntity(blockPos) instanceof AnimalPenBlockInterface<?> animalPen)
            {
                animalPen.setAnimalDisplaySize(displaySize);
                animalPen.setProtectedAmount(protectedAmount);
                animalPen.setOwner(owner);
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
        return UpdateAnimalSizeData.ID;
    }


    public static final Type<UpdateAnimalSizeData> ID =
        new Type<>(new ResourceLocation(AnimalPen.MOD_ID, "update_configuration"));


    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateAnimalSizeData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, UpdateAnimalSizeData::position,
        ByteBufCodecs.VAR_LONG, UpdateAnimalSizeData::size,
        UpdateAnimalSizeData::new
    );
}
