//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.items.component;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


public record StoredMobVariantKey(UUID key, int amount)
{
    public static StoredMobVariantKey of()
    {
        return of(UUID.randomUUID(), 0);
    }


    public static StoredMobVariantKey of(int amount)
    {
        return of(UUID.randomUUID(), amount);
    }


    public static StoredMobVariantKey of(UUID key)
    {
        return of(key, 0);
    }

    public static StoredMobVariantKey of(UUID key, int amount)
    {
        return new StoredMobVariantKey(key, amount);
    }


    public StoredMobVariantKey withSize(int size)
    {
        return StoredMobVariantKey.of(this.key, size);
    }


    /**
     * Data serialization
     */
    public static final Codec<StoredMobVariantKey> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            UUIDUtil.CODEC.fieldOf("key").forGetter(StoredMobVariantKey::key),
            Codec.INT.fieldOf("amount").forGetter(StoredMobVariantKey::amount)
        ).apply(instance, StoredMobVariantKey::new)
    );

    /**
     * Network sync codec
     */
    public static final StreamCodec<ByteBuf, StoredMobVariantKey> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, StoredMobVariantKey::key,
            ByteBufCodecs.VAR_INT, StoredMobVariantKey::amount,
            StoredMobVariantKey::new);
}
