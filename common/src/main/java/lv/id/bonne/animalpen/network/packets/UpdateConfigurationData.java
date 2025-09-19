package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.Nullable;
import java.util.UUID;

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
public class UpdateConfigurationData
{
    /**
     * The encoding of the packet.
     * @param position The block position that is affected.
     * @param displaySize The new displaySize of entity.
     * @return packet buffer.
     */
    public static FriendlyByteBuf encode(BlockPos position, long displaySize, long protectedAmount, @Nullable UUID owner)
    {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        buffer.writeBlockPos(position);
        buffer.writeLong(displaySize);
        buffer.writeLong(protectedAmount);

        if (owner != null)
        {
            buffer.writeBoolean(true);
            buffer.writeUUID(owner);
        }
        else
        {
            buffer.writeBoolean(false);
        }

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
        long displaySize = friendlyByteBuf.readLong();
        long protectedAmount = friendlyByteBuf.readLong();
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


    /**
     * The resource ID.
     */
    public static final ResourceLocation ID = new ResourceLocation(AnimalPen.MOD_ID, "update_configuration");
}
