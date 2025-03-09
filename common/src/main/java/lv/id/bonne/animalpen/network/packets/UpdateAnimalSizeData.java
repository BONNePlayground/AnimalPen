package lv.id.bonne.animalpen.network.packets;


import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenBlockInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;


/**
 * This is a simple packet send from client to server to indicate that animal display size is changed.
 */
public class UpdateAnimalSizeData
{
    /**
     * The encoding of the packet.
     * @param position The block position that is affected.
     * @param size The new size of entity.
     * @return packet buffer.
     */
    public static FriendlyByteBuf encode(BlockPos position, long size)
    {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        buffer.writeBlockPos(position);
        buffer.writeLong(size);

        return buffer;
    }


    /**
     * This method handles incoming packet on server.
     * @param friendlyByteBuf The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(FriendlyByteBuf friendlyByteBuf, NetworkManager.PacketContext packetContext)
    {
        BlockPos blockPos = friendlyByteBuf.readBlockPos();
        long size = friendlyByteBuf.readLong();

        packetContext.queue(() ->
        {
            Level level = packetContext.getPlayer().getLevel();

            if (level.getBlockEntity(blockPos) instanceof AnimalPenBlockInterface<?> animalPen)
            {
                animalPen.setAnimalDisplaySize(size);
            }
            else
            {
                AnimalPen.LOGGER.error("Block entity not found at the position!");
            }
        });
    }


    /**
     * The resource ID.
     */
    public static final ResourceLocation ID = new ResourceLocation(AnimalPen.MOD_ID, "update_animal_size");
}
