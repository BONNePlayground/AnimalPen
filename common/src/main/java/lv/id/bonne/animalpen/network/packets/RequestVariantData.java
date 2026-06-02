package lv.id.bonne.animalpen.network.packets;


import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;


/**
 * This packet request all animal variants stored in given block pos.
 */
public class RequestVariantData
{
    public static FriendlyByteBuf encode(BlockPos blockPos)
    {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeBlockPos(blockPos);
        return buffer;
    }


    public static void handle(FriendlyByteBuf friendlyByteBuf, NetworkManager.PacketContext packetContext)
    {
       if (packetContext.getEnv() == EnvType.CLIENT)
        {
            return;
        }

        BlockPos blockPos = friendlyByteBuf.readBlockPos();

        packetContext.queue(() ->
        {
            if (!(packetContext.getPlayer() instanceof ServerPlayer player))
            {
                return;
            }

            if (player.blockPosition().distSqr(blockPos) > 25)
            {
                // Too far from player.
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(blockPos);

            if (!(blockEntity instanceof AbstractAnimalPenBlockEntity animalPenEntity))
            {
                return;
            }

            ListTag variants = animalPenEntity.getEntityVariants();

            // Reply back to the client
            AnimalPen.CHANNEL.sendToPlayer(player, new UpdateVariantScreenData(blockPos, variants));
        });
    }


    public static final ResourceLocation ID = AnimalPen.resourceOf("request_variants");
}