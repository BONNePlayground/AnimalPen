//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;


/**
 * This method triggers update on VariantSelectionScreen
 */
public record ChangeDecorationData(BlockPos position, int index) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     *
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(ChangeDecorationData data, NetworkManager.PacketContext packetContext)
    {
        int index = data.index();
        BlockPos blockPos = data.position();

        packetContext.queue(() ->
        {
            if (packetContext.getEnv() == EnvType.CLIENT)
            {
                return;
            }

            Level level = packetContext.getPlayer().level();

            if (level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity animalPen)
            {
                if (animalPen.getOwner().isEmpty() ||
                    animalPen.getOwner().get().equals(packetContext.getPlayer().getUUID()))
                {
                    animalPen.setAndUpdateBlockState(index);
                }
            }
            else
            {
                AnimalPen.LOGGER.error("Block entity not found at the position!");
            }
        });
    }


    @Override
    @NotNull
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return ChangeDecorationData.ID;
    }


    public static final CustomPacketPayload.Type<ChangeDecorationData> ID =
        new CustomPacketPayload.Type<>(AnimalPen.resourceOf("change_decoration"));


    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeDecorationData> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC, ChangeDecorationData::position,
            ByteBufCodecs.VAR_INT, ChangeDecorationData::index,
            ChangeDecorationData::new
        );
}