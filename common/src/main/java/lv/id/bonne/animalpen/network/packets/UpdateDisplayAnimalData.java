package lv.id.bonne.animalpen.network.packets;


import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;


/**
 * This is a simple packet send from client to server to indicate that animal variant is changed.
 */
public class UpdateDisplayAnimalData
{
    /**
     * The simple packet encoding.
     *
     * @param position The block position that is affected.
     * @param index The new variant of entity.
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
            Level level = packetContext.getPlayer().getLevel();

            if (level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity animalPen)
            {
                if (animalPen.getOwner().isPresent() &&
                    !animalPen.getOwner().get().equals(packetContext.getPlayer().getUUID()))
                {
                    return;
                }

                if (index >= 0 && index < animalPen.getEntityVariants().size())
                {
                    AnimalPenCriteriaTriggersRegistry.ANIMAL_VARIANT_CHANGE_TRIGGER.trigger(
                        (ServerPlayer) packetContext.getPlayer());
                }

                animalPen.updateAnimalVariant(index);
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
    public static final ResourceLocation ID = AnimalPen.resourceOf("update_display_animal");
}
