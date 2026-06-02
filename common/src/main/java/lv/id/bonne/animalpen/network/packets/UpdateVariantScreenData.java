//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;


/**
 * This method triggers update on VariantSelectionScreen
 */
public record UpdateVariantScreenData(@NotNull BlockPos position, @Nullable ListTag variants)
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
            screenSelection.update(this.variants);
        });
    }


    public static void encode(UpdateVariantScreenData packet, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(packet.position);

        CompoundTag wrapper = new CompoundTag();

        if (packet.variants != null)
        {
            wrapper.put("Payload", packet.variants);
        }

        buffer.writeNbt(wrapper);
    }


    public static UpdateVariantScreenData decode(FriendlyByteBuf buffer)
    {
        BlockPos blockPos = buffer.readBlockPos();
        CompoundTag wrapper = buffer.readNbt();

        ListTag variants = null;

        if (wrapper != null && wrapper.contains("Payload", Tag.TAG_LIST))
        {
            variants = wrapper.getList("Payload", Tag.TAG_COMPOUND);
        }

        return new UpdateVariantScreenData(blockPos, variants);
    }
}
