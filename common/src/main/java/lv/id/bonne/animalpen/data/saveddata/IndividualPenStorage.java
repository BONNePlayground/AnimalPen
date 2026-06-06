package lv.id.bonne.animalpen.data.saveddata;


import java.io.File;
import java.util.UUID;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;


/**
 * This class manages animal pen data accessing, storing and deleting.
 */
public class IndividualPenStorage extends SavedData
{
    private IndividualPenStorage(UUID penId)
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


    public void setVariants(ListTag variants)
    {
        this.variants = variants;
        this.setDirty();
    }


    @Override
    public CompoundTag save(CompoundTag tag)
    {
        tag.put("Variants", this.variants);
        return tag;
    }


    @Override
    public void save(File file)
    {
        if (!file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }

        super.save(file);
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
            tag -> load(tag, penId),
            () -> new IndividualPenStorage(penId),
            storageKey(penId)
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
        level.getDataStorage().set(storageKey(penId), null);

        File file = level.getServer()
            .getWorldPath(LevelResource.ROOT)
            .resolve("data")
            .resolve(storageKey(penId) + ".dat")
            .toFile();

        if (file.exists() && !file.delete())
        {
            AnimalPen.LOGGER.warn("Could not delete pen data file for {}", penId);
        }
    }


    private final UUID penId;

    private ListTag variants;
}