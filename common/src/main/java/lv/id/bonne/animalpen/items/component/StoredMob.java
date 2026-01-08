package lv.id.bonne.animalpen.items.component;

//
// Created by BONNe
// Copyright - 2026
//


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;


public record StoredMob(EntityType<?> entityType, CompoundTag tag)
{
    public StoredMob
    {
        tag = tag.copy();
    }


    public static StoredMob of(EntityType<?> entityType, CompoundTag tag)
    {
        return new StoredMob(entityType, tag);
    }


    /**
     * Serialization codec
     */
    public static final Codec<StoredMob> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().
                fieldOf("entity_type").
                forGetter(StoredMob::entityType),
            CompoundTag.CODEC.
                fieldOf("tag").
                forGetter(StoredMob::tag)
        ).apply(instance, StoredMob::new)
    );

    /**
     * Network Data sync
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, StoredMob> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ENTITY_TYPE),
            StoredMob::entityType,
            ByteBufCodecs.COMPOUND_TAG,
            StoredMob::tag,
            StoredMob::new
        );
}
