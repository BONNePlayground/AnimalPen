//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


/**
 * This method triggers update on VariantSelectionScreen
 */
public record UpdateVariantScreenData(BlockPos position) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(UpdateVariantScreenData data, NetworkManager.PacketContext packetContext)
    {
        BlockPos blockPos = data.position();

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


    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type()
    {
        return UpdateVariantScreenData.ID;
    }


    public static final Type<UpdateVariantScreenData> ID =
        new Type<>(new ResourceLocation(AnimalPen.MOD_ID, "update_variant_screen_data"));


    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateVariantScreenData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, UpdateVariantScreenData::position,
        UpdateVariantScreenData::new
    );
}
