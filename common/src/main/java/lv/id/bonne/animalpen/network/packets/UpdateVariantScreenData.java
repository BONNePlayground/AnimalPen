//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;


/**
 * This method triggers update on VariantSelectionScreen
 */
public record UpdateVariantScreenData(BlockPos position) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param player The packet context.
     */
    public static void handle(UpdateVariantScreenData data, Player player)
    {
        if (!player.level().isClientSide())
        {
            return;
        }

        BlockPos blockPos = data.position();

        if (player != Minecraft.getInstance().player)
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
    }


    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type()
    {
        return UpdateVariantScreenData.ID;
    }


    public static final Type<UpdateVariantScreenData> ID =
        new Type<>(AnimalPen.resourceOf("update_variant_screen_data"));


    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateVariantScreenData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, UpdateVariantScreenData::position,
        UpdateVariantScreenData::new
    );
}
