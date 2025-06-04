//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;


/**
 * This method triggers update on VariantSelectionScreen
 */
public class UpdateVariantScreenData
{
    /**
     * The encoding of the packet.
     * @param position The block position that is affected.
     * @return packet buffer.
     */
    public static FriendlyByteBuf encode(BlockPos position)
    {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
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
        BlockPos blockPos = friendlyByteBuf.readBlockPos();

        if (packetContext.getEnv() == EnvType.SERVER)
        {
            return;
        }

        packetContext.queue(() ->
        {
            if (packetContext.getPlayer() != Minecraft.getInstance().player)
            {
                return;
            }

            if (!(Minecraft.getInstance().screen instanceof VariantScreenSelection screenSelection))
            {
                // only if player has opened selection screen
                return;
            }

            if (!screenSelection.getPosition().equals(blockPos))
            {
                // Only if it is the same screen
                return;
            }

            // Trigger update on screen
            screenSelection.update();
        });
    }


    /**
     * The resource ID.
     */
    public static final ResourceLocation ID = new ResourceLocation(AnimalPen.MOD_ID, "update_variant_screen_data");
}
