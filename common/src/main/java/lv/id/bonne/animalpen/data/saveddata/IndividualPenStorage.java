package lv.id.bonne.animalpen.data.saveddata;


import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;


/**
 * This class manages animal pen data accessing, storing and deleting.
 */
public class IndividualPenStorage extends SavedData
{
    public IndividualPenStorage(UUID penId)
    {
        this.penId = penId;
        this.variants = new ListTag();
    }

// -------------------------------------------------------------------------
// Factory
// -------------------------------------------------------------------------


    public ListTag getVariants()
    {
        return this.variants;
    }

// -------------------------------------------------------------------------
// Public API
// -------------------------------------------------------------------------


    public void setVariants(List<CompoundTag> variants)
    {
        ListTag tag = new ListTag();
        tag.addAll(variants);

        this.variants = tag;
        this.setDirty();
    }


    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider)
    {
        tag.put("Variants", this.variants);
        return tag;
    }


// -------------------------------------------------------------------------
// WorldSavedData contract
// -------------------------------------------------------------------------


    public static String storageKey(UUID penId)
    {
        return "animal_pens/" + penId;
    }


// -------------------------------------------------------------------------
// Internals
// -------------------------------------------------------------------------


    public static IndividualPenStorage getOrCreate(ServerLevel level, UUID penId)
    {
        return level.getDataStorage().computeIfAbsent(
            factory(penId),
            storageKey(penId)
        );
    }


    private static SavedData.Factory<IndividualPenStorage> factory(UUID penId)
    {
        return new SavedData.Factory<>(
            () -> new IndividualPenStorage(penId),
            (tag, provider) -> load(tag, penId),
            DataFixTypes.LEVEL
        );
    }


    private static IndividualPenStorage load(CompoundTag tag, UUID penId)
    {
        IndividualPenStorage storage = new IndividualPenStorage(penId);
        storage.variants = tag.getList("Variants", Tag.TAG_COMPOUND);
        return storage;
    }


    public static void delete(ServerLevel level, UUID penId)
    {
        IndividualPenStorage.getOrCreate(level, penId).setVariants(Collections.emptyList());
    }


    private final UUID penId;

    private ListTag variants;
}