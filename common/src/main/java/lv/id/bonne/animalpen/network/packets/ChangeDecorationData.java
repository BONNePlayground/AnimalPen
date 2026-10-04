//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;


/**
 * This method triggers update on VariantSelectionScreen
 */
public record ChangeDecorationData(BlockPos position, int index) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     *
     * @param data The incoming packet.
     */
    public static void handle(ChangeDecorationData data, ServerPlayer player)
    {
        int index = data.index();
        BlockPos blockPos = data.position();

        ServerLevel level = player.level();

        if (level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity animalPen)
        {
            if (animalPen.getOwner().isEmpty() ||
                animalPen.getOwner().get().equals(player.getUUID()))
            {
                animalPen.setAndUpdateBlockState(index);
            }
        }
        else
        {
            AnimalPen.LOGGER.error("Block entity not found at the position!");
        }
    }


    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type()
    {
        return ChangeDecorationData.ID;
    }


    public static final Type<ChangeDecorationData> ID =
        new Type<>(AnimalPen.resourceOf("change_decoration"));


    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeDecorationData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, ChangeDecorationData::position,
        ByteBufCodecs.INT, ChangeDecorationData::index,
        ChangeDecorationData::new
    );
}