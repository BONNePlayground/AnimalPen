package lv.id.bonne.animalpen.network.packets;


import java.util.List;
import java.util.function.Supplier;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import net.fabricmc.api.EnvType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;


/**
 * This is a packet that sync given entity and its interaction with client.
 *
 * @param entityId The id of entity.
 * @param interactions The list of interactions for entity.
 */
public record AnimalInteractionSyncEntityPacket(ResourceKey<EntityType<?>> entityId, List<AnimalInteraction> interactions)
{
    public static void encode(AnimalInteractionSyncEntityPacket pkt, FriendlyByteBuf buf)
    {
        buf.writeResourceKey(pkt.entityId());

        Tag tag = AnimalInteraction.STREAM_CODEC.
            listOf().
            encodeStart(NbtOps.INSTANCE, pkt.interactions).
            getOrThrow(false, AnimalPen.LOGGER::error);

        if (tag instanceof ListTag tagList)
        {
            CompoundTag data = new CompoundTag();
            data.put("data", tagList);
            buf.writeNbt(data);
        }
        else
        {
            AnimalPen.LOGGER.error("Failed to make data packet for: " + tag.getAsString());
        }
    }


    public static AnimalInteractionSyncEntityPacket decode(FriendlyByteBuf buf)
    {
        ResourceKey<EntityType<?>> entityId = buf.readResourceKey(Registries.ENTITY_TYPE);
        CompoundTag tag = buf.readNbt();
        List<AnimalInteraction> interactions;

        if (tag == null || !tag.contains("data", Tag.TAG_LIST))
        {
            AnimalPen.LOGGER.error("Failed to parse data packet for: " + (tag == null ? "null" : tag.getAsString()));
            return null;
        }

        interactions = AnimalInteraction.STREAM_CODEC.
            listOf().
            parse(NbtOps.INSTANCE, tag.getList("data", Tag.TAG_COMPOUND)).
            getOrThrow(false, AnimalPen.LOGGER::error);

        return new AnimalInteractionSyncEntityPacket(entityId, interactions);
    }


    public static void handle(AnimalInteractionSyncEntityPacket pkt, Supplier<NetworkManager.PacketContext> ctx)
    {
        if (ctx.get().getEnv() == EnvType.SERVER) return;

        ctx.get().queue(() -> AnimalPenInteractionRegistry.register(pkt.entityId(), pkt.interactions()));
    }
}