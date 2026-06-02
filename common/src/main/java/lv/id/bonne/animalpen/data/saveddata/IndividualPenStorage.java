package lv.id.bonne.animalpen.data.saveddata;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

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
    public static ListTag loadVariants(ServerLevel level, UUID penId)
    {
        File file = getFileForPen(level, penId);
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
            LOGGER.error("Failed to load animal pen data for " + penId, e);
            return new ListTag();
        }
    }


    /**
     * Saves the ListTag to the specific item's file
     */
    public static void saveVariants(ServerLevel level, UUID penId, ListTag variants)
    {
        if (penId == null)
        {
            // UUID is null.
            return;
        }

        File file = getFileForPen(level, penId);
        CompoundTag rootTag = new CompoundTag();
        rootTag.put("Variants", variants);

        try
        {
            NbtIo.writeCompressed(rootTag, file.toPath());
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to save animal pen data for " + penId, e);
        }
    }


    /**
     * This method deletes stored data file.
     */
    public static void deleteFile(ServerLevel level, UUID penId)
    {
        File file = getFileForPen(level, penId);

        if (file.exists())
        {
            file.delete();
        }
    }

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String FOLDER_NAME = "animal_pens";
}