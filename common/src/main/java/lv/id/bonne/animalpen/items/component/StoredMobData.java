package lv.id.bonne.animalpen.items.component;

//
// Created by BONNe
// Copyright - 2026
//


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


public record StoredMobData(long animalCount, Map<String, Integer> properties, Map<String, Long> cooldowns)
{
    public StoredMobData
    {
        properties = new HashMap<>(properties);
        cooldowns  = new HashMap<>(cooldowns);
    }


    public static StoredMobData of(long animalCount,
        Map<String, Integer> propertiesMap,
        Map<String, Long> cooldownMap)
    {
        return new StoredMobData(animalCount, propertiesMap, cooldownMap);
    }


    public static final StoredMobData EMPTY =
        new StoredMobData(0, new HashMap<>(0), new HashMap<>(0));


    /**
     * Serialization codec
     */
    public static final Codec<StoredMobData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.
                fieldOf("animal_count").
                forGetter(StoredMobData::animalCount),
            Codec.unboundedMap(Codec.STRING, Codec.INT).
                optionalFieldOf("properties", Map.of()).
                forGetter(StoredMobData::properties),
            Codec.unboundedMap(Codec.STRING, Codec.LONG).
                optionalFieldOf("cooldowns", Map.of()).
                forGetter(StoredMobData::cooldowns)
        ).apply(instance, StoredMobData::new)
    );

    /**
     * Network Data sync
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, StoredMobData> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            StoredMobData::animalCount,
            ByteBufCodecs.map(
                HashMap::new,
                ByteBufCodecs.STRING_UTF8,
                ByteBufCodecs.VAR_INT
            ),
            StoredMobData::properties,
            ByteBufCodecs.map(
                HashMap::new,
                ByteBufCodecs.STRING_UTF8,
                ByteBufCodecs.VAR_LONG
            ),
            StoredMobData::cooldowns,
            StoredMobData::new
        );
}
