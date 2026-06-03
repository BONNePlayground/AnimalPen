package lv.id.bonne.animalpen.data.saveddata;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.items.component.StoredMobVariantKey;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;


/**
 * This class manages animal pen data accessing, storing and deleting.
 */
public class IndividualPenStorage
{
    /**
     * This method returns directory for stored animal data.
     */
    private static File getStorageDirectory(ServerLevel level)
    {
        Path worldRoot = level.getServer().getWorldPath(LevelResource.ROOT);
        File dir = worldRoot.resolve("data").resolve(FOLDER_NAME).toFile();
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return dir;
    }


    /**
     * This method creates new data file with given id.
     */
    private static File getFileForPen(ServerLevel level, UUID penId)
    {
        return new File(getStorageDirectory(level), penId.toString() + ".dat");
    }


    /**
     * Loads the ListTag from the specific item's file
     */
    public static ListTag loadVariants(ServerLevel level, StoredMobVariantKey key)
    {
        File file = getFileForPen(level, key.key());
        if (!file.exists())
        {
            return new ListTag();
        }

        try
        {
            CompoundTag rootTag = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            return rootTag.getList("Variants", Tag.TAG_COMPOUND);
        }
        catch (IOException e)
        {
            AnimalPen.LOGGER.error("Failed to load animal pen data for " + key.key(), e);
            return new ListTag();
        }
    }


    public static void saveVariants(ServerLevel level, StoredMobVariantKey key, List<CompoundTag> variants)
    {
        ListTag convert = new ListTag();
        convert.addAll(variants);

        IndividualPenStorage.saveVariants(level, key, convert);
    }


    public static void saveVariants(ServerLevel level, StoredMobVariantKey key, ListTag variants)
    {
        if (key == null)
        {
            // UUID is null.
            return;
        }

        File file = getFileForPen(level, key.key());
        CompoundTag rootTag = new CompoundTag();
        rootTag.put("Variants", variants);

        try
        {
            NbtIo.writeCompressed(rootTag, file.toPath());
        }
        catch (IOException e)
        {
            AnimalPen.LOGGER.error("Failed to save animal pen data for " + key.key(), e);
        }
    }


    public static void deleteFile(ServerLevel level, StoredMobVariantKey key)
    {
        File file = getFileForPen(level, key.key());

        if (file.exists())
        {
            file.delete();
        }
    }


    private static final String FOLDER_NAME = "animal_pens";
}