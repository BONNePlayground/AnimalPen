package lv.id.bonne.animalpen.network.packets;


import org.jetbrains.annotations.NotNull;
import java.util.Optional;
import java.util.UUID;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;


/**
 * This is a simple packet send from client to server to indicate that animal display size is changed.
 */
public record UpdateConfigurationData(BlockPos position, long size, long protectedAmount, Optional<UUID> uuid) implements CustomPacketPayload
{
    /**
     * This method handles incoming packet on server.
     * @param data The incoming packet.
     * @param packetContext The packet context.
     */
    public static void handle(UpdateConfigurationData data, NetworkManager.PacketContext packetContext)
    {
        BlockPos blockPos = data.position();
        long displaySize = data.size();
        long protectedAmount = data.protectedAmount();
        Optional<UUID> owner = data.uuid();

        packetContext.queue(() ->
        {
            Level level = packetContext.getPlayer().level();

            if (level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity animalPen)
            {
                if (animalPen.getOwner().isEmpty() ||
                    animalPen.getOwner().get().equals(packetContext.getPlayer().getUUID()))
                {
                    animalPen.setAnimalDisplaySize(displaySize);
                    animalPen.setProtectedAmount(protectedAmount);
                    animalPen.setOwner(owner.orElse(null));
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
    public Type<? extends CustomPacketPayload> type()
    {
        return UpdateConfigurationData.ID;
    }


    public static final Type<UpdateConfigurationData> ID =
        new Type<>(AnimalPen.resourceOf("update_configuration"));


    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateConfigurationData> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, UpdateConfigurationData::position,
        ByteBufCodecs.VAR_LONG, UpdateConfigurationData::size,
        ByteBufCodecs.VAR_LONG, UpdateConfigurationData::protectedAmount,
        ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), UpdateConfigurationData::uuid,
        UpdateConfigurationData::new
    );
}
