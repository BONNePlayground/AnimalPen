//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;


/**
 * This method triggers update on VariantSelectionScreen
 */
public record UpdateVariantScreenData(BlockPos position)
{
    public void handle(Supplier<NetworkManager.PacketContext> context)
    {
        NetworkManager.PacketContext packetContext = context.get();

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

            if (!screenSelection.getPosition().equals(this.position))
            {
                // Only if it is the same screen
                return;
            }

            // Trigger update on screen
            screenSelection.update();
        });
    }


    public static void encode(UpdateVariantScreenData packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.position);
    }


    public static UpdateVariantScreenData decode(FriendlyByteBuf buffer)
    {
        return new UpdateVariantScreenData(buffer.readBlockPos());
    }
}
