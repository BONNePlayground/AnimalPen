//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.ByteBuf;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;


/**
 * This method triggers update on VariantSelectionScreen
 */
public record UpdateVariantScreenData(@NotNull BlockPos position, @Nullable ListTag variants) implements CustomPacketPayload
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
            screenSelection.update(data.variants());
        });
    }


    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type()
    {
        return UpdateVariantScreenData.ID;
    }


    public static final Type<UpdateVariantScreenData> ID =
        new Type<>(AnimalPen.resourceOf("update_variant_screen_data"));


    private static final StreamCodec<ByteBuf, @Nullable ListTag> SAFE_LIST_TAG_CODEC =
        ByteBufCodecs.OPTIONAL_COMPOUND_TAG.map(
            optionalCompound -> {
                // If the optional is empty, return null
                if (optionalCompound.isEmpty()) return null;

                CompoundTag compound = optionalCompound.get();
                if (!compound.contains("variants", Tag.TAG_LIST)) return null;

                return compound.getList("variants", Tag.TAG_COMPOUND);
            },
            listTag -> {
                if (listTag == null) return Optional.empty();

                CompoundTag compound = new CompoundTag();
                compound.put("variants", listTag);
                return Optional.of(compound);
            }
        );


    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateVariantScreenData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, UpdateVariantScreenData::position,
        SAFE_LIST_TAG_CODEC, UpdateVariantScreenData::variants,
        UpdateVariantScreenData::new
    );
}
