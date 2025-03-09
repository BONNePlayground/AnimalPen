package lv.id.bonne.animalpen.network.packets;


import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenBlockInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;


/**
 * This is a simple packet send from client to server to indicate that animal variant is changed.
 */
public class UpdateDisplayAnimalData
{
    /**
     * The simple packet encoding.
     * @param position The block position that is affected.
     * @param tag The new variant of entity.
     * @return packet buffer.
     */
    public static FriendlyByteBuf encode(BlockPos position, CompoundTag tag)
    {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        buffer.writeNbt(tag);
        buffer.writeBlockPos(position);

        return buffer;
    }


    /**
     * This method handles incoming packet on server.
     * @param friendlyByteBuf The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(FriendlyByteBuf friendlyByteBuf, NetworkManager.PacketContext packetContext)
    {
        CompoundTag animalVariant = friendlyByteBuf.readNbt();
        BlockPos blockPos = friendlyByteBuf.readBlockPos();

        packetContext.queue(() ->
        {
            Level level = packetContext.getPlayer().level();

            if (level.getBlockEntity(blockPos) instanceof AnimalPenBlockInterface<?> animalPen)
            {
                animalPen.updateAnimalVariant(animalVariant);
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
    public static final ResourceLocation ID = new ResourceLocation(AnimalPen.MOD_ID, "update_display_animal");
}
