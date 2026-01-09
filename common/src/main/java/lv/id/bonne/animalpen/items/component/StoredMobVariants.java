//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.items.component;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


public record StoredMobVariants(List<CompoundTag> variants)
{
    public StoredMobVariants
    {
        variants = variants.stream().
            map(CompoundTag::copy).
            collect(Collectors.toCollection(ArrayList::new));
    }


    public static StoredMobVariants of(List<CompoundTag> variantList)
    {
        return new StoredMobVariants(variantList);
    }


    /**
     * Data serialization
     */
    public static final Codec<StoredMobVariants> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            CompoundTag.CODEC.listOf().
                fieldOf("variants").
                forGetter(StoredMobVariants::variants)
        ).apply(instance, StoredMobVariants::new)
    );

    /**
     * Network sync codec
     */
    public static final StreamCodec<ByteBuf, StoredMobVariants> STREAM_CODEC =
        ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()).
            map(StoredMobVariants::new, StoredMobVariants::variants);
}
