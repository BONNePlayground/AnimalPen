package lv.id.bonne.animalpen.data.saveddata;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;


/**
 * This class manages animal pen data accessing, storing and deleting.
 */
public class IndividualPenStorage extends SavedData
{
    private final UUID penId;
    private ListTag variants;

    public IndividualPenStorage(UUID penId)
    {
        this.penId = penId;
        this.variants = new ListTag();
    }

    private IndividualPenStorage(UUID penId, ListTag variants)
    {
        this.penId = penId;
        this.variants = variants;
    }

    public ListTag getVariants()
    {
        return this.variants;
    }


    public void setVariants(List<CompoundTag> variants)
    {
        ListTag tag = new ListTag();
        tag.addAll(variants);

        this.variants = tag;
        this.setDirty();
    }


    public static IndividualPenStorage getOrCreate(ServerLevel level, UUID penId)
    {
        return level.getDataStorage().computeIfAbsent(type(penId));
    }


    public static void delete(ServerLevel level, UUID penId)
    {
        IndividualPenStorage data = getOrCreate(level, penId);
        data.variants.clear();
        data.setDirty();
    }

    private static SavedDataType<IndividualPenStorage> type(UUID penId)
    {
        return new SavedDataType<>(
            AnimalPen.resourceOf( penId.toString()),
            () -> new IndividualPenStorage(penId),
            CODEC(penId),
            DataFixTypes.LEVEL
        );
    }

    static List<CompoundTag> toList(ListTag listTag)
    {
        List<CompoundTag> converted = new ArrayList<>(listTag.size());
        listTag.forEach(tag -> converted.add((CompoundTag) tag));
        return converted;
    }


    private static Codec<IndividualPenStorage> CODEC(UUID penId)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
            CompoundTag.CODEC.listOf()
                .fieldOf("Variants")
                .forGetter(storage -> toList(storage.getVariants()))
        ).apply(instance, variants -> {
            ListTag listTag = new ListTag();
            listTag.addAll(variants);

            return new IndividualPenStorage(penId, listTag);
        }));
    }
}