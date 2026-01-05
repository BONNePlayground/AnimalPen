package lv.id.bonne.animalpen.network.packets;


import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;


/**
 * This is a simple packet send from client to server to indicate that animal variant is removed.
 */
public class RemoveDisplayAnimalData
{
    /**
     * The encoding of the packet.
     *
     * @param position The block position that is affected.
     * @param index The removed variant index.
     * @return packet buffer.
     */
    public static FriendlyByteBuf encode(BlockPos position, int index)
    {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        buffer.writeBlockPos(position);
        buffer.writeInt(index);

        return buffer;
    }


    /**
     * This method handles incoming packet on server.
     *
     * @param friendlyByteBuf The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(FriendlyByteBuf friendlyByteBuf, NetworkManager.PacketContext packetContext)
    {
        BlockPos blockPos = friendlyByteBuf.readBlockPos();
        int index = friendlyByteBuf.readInt();

        packetContext.queue(() ->
        {
            Level level = packetContext.getPlayer().level();

            if (level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity animalPen)
            {
                animalPen.removeAnimalVariant(index);
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
    public static final ResourceLocation ID = AnimalPen.resourceOf("remove_display_animal");
}
