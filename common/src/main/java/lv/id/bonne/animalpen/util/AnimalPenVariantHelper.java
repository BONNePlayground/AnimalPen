//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.util;


import org.jetbrains.annotations.Nullable;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.data.saveddata.IndividualPenStorage;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


/**
 * Some useful methods I did not know where to put.
 */
public class AnimalPenVariantHelper
{
    /**
     * This method stores given animal as a variant in given item stack data file.
     *
     * @param itemStack The storage place.
     * @param animal The animal that need to be stored
     * @param player Player that should receive message is it fails to add variant.
     * @return {@code true} if variant was added, {@code false} otherwise
     */
    public static boolean storeAnimalVariant(ItemStack itemStack, Mob animal, @Nullable Player player)
    {
        if (AnimalPen.config().getMaxStoredVariants() <= 0)
        {
            return false;
        }

        // File handling requires access to the Server's thread and level
        if (animal.level.isClientSide() || !(animal.level instanceof ServerLevel serverLevel))
        {
            return false;
        }

        CompoundTag itemTag = itemStack.getOrCreateTag();

        if (!itemTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
        {
            return false;
        }

        // Get or assign this item's unique data file pointer
        UUID penId;

        if (itemTag.hasUUID(AnimalPenCompoundTags.TAG_STORAGE_ID))
        {
            penId = itemTag.getUUID(AnimalPenCompoundTags.TAG_STORAGE_ID);
        }
        else
        {
            penId = UUID.randomUUID();
            itemTag.putUUID(AnimalPenCompoundTags.TAG_STORAGE_ID, penId);
        }

        // Load variants directly from the item's individual save file
        ListTag variantList = IndividualPenStorage.loadVariants(serverLevel, penId);

        if (variantList.size() + 1 > AnimalPen.config().getMaxStoredVariants())
        {
            if (player != null)
            {
                player.displayClientMessage(
                    Component.translatable("item.animal_pen.animal_cage.error.too_many_variants").
                        withStyle(ChatFormatting.DARK_RED), true);
            }

            return false;
        }

        CompoundTag variant = new CompoundTag();
        animal.save(variant);

        variant.remove("UUID");
        variant.remove("Pos");

        if (!variantList.contains(variant))
        {
            variantList.add(variant);
            // Save data back out to disk immediately
            IndividualPenStorage.saveVariants(serverLevel, penId, variantList);
        }

        return true;
    }


    /**
     * This method returns Optional list-tag of animal variants from the item's data file.
     * Note: Requires Level context to locate the server's data folder.
     *
     * @param itemStack The item stack that need to be checked.
     * @param level The level context (must be server-side).
     * @return Optional list of tags for animal variants.
     */
    public static Optional<ListTag> getAnimalVariants(ItemStack itemStack, Level level)
    {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel))
        {
            return Optional.empty();
        }

        CompoundTag itemTag = itemStack.getOrCreateTag();

        // New migration
        if (itemTag.contains(AnimalPenCompoundTags.TAG_VARIANTS))
        {
            ListTag variants = itemTag.getList(AnimalPenCompoundTags.TAG_VARIANTS, Tag.TAG_COMPOUND);
            UUID uuid = UUID.randomUUID();
            itemTag.putUUID(AnimalPenCompoundTags.TAG_STORAGE_ID, uuid);
            IndividualPenStorage.saveVariants(serverLevel, uuid, variants);
            itemTag.remove(AnimalPenCompoundTags.TAG_VARIANTS);
        }

        if (itemTag.hasUUID(AnimalPenCompoundTags.TAG_STORAGE_ID))
        {
            UUID penId = itemTag.getUUID(AnimalPenCompoundTags.TAG_STORAGE_ID);
            ListTag variantList = IndividualPenStorage.loadVariants(serverLevel, penId);
            return Optional.of(variantList);
        }

        return Optional.empty();
    }


    /**
     * This method returns if animal variants can be merged into main item variants.
     *
     * @param mainItem The item stack that should contain all variants
     * @param redundantItem The item stack that donates their variants
     * @param level The level context (must be server-side).
     * @param player A player instance
     * @return {@code true} if all variants can be added, {@code false} otherwise.
     */
    public static boolean canMergeAnimalVariants(ItemStack mainItem, ItemStack redundantItem, Level level, @Nullable Player player)
    {
        if (AnimalPen.config().getMaxStoredVariants() <= 0)
        {
            return true;
        }

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel))
        {
            return false;
        }

        CompoundTag itemTag = mainItem.getOrCreateTag();
        CompoundTag redundantTag = redundantItem.getOrCreateTag();

        if (!itemTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(AnimalPenCompoundTags.TAG_ENTITY_ID) ||
            !redundantTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
        {
            return false;
        }

        // Read sizes from files instead of item NBT
        int mainSize = 0;

        if (itemTag.hasUUID(AnimalPenCompoundTags.TAG_STORAGE_ID))
        {
            mainSize = IndividualPenStorage.loadVariants(serverLevel, itemTag.getUUID(AnimalPenCompoundTags.TAG_STORAGE_ID)).size();
        }

        int redundantSize = 0;

        if (redundantTag.hasUUID(AnimalPenCompoundTags.TAG_STORAGE_ID))
        {
            redundantSize = IndividualPenStorage.loadVariants(serverLevel, redundantTag.getUUID(AnimalPenCompoundTags.TAG_STORAGE_ID)).size();
        }

        if (mainSize + redundantSize > AnimalPen.config().getMaxStoredVariants())
        {
            if (player != null)
            {
                player.displayClientMessage(
                    Component.translatable("item.animal_pen.animal_cage.error.too_many_variants").
                        withStyle(ChatFormatting.DARK_RED), true);
            }

            return false;
        }

        return true;
    }


    /**
     * This method merges redundant item entity variants into main item stack file.
     *
     * @param mainItem The item stack that should contain all variants
     * @param redundantItem The item stack that donates their variants
     * @param level The level context (must be server-side).
     * @param player A player instance
     */
    public static void mergeAnimalVariants(ItemStack mainItem, ItemStack redundantItem, Level level, @Nullable Player player)
    {
        if (AnimalPen.config().getMaxStoredVariants() <= 0)
        {
            return;
        }

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel))
        {
            return;
        }

        CompoundTag itemTag = mainItem.getOrCreateTag();
        CompoundTag redundantTag = redundantItem.getOrCreateTag();

        if (!itemTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(AnimalPenCompoundTags.TAG_ENTITY_ID) ||
            !redundantTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
        {
            return;
        }

        UUID mainId;

        if (itemTag.hasUUID(AnimalPenCompoundTags.TAG_STORAGE_ID))
        {
            mainId = itemTag.getUUID(AnimalPenCompoundTags.TAG_STORAGE_ID);
        }
        else
        {
            mainId = UUID.randomUUID();
            itemTag.putUUID(AnimalPenCompoundTags.TAG_STORAGE_ID, mainId);
        }

        if (!redundantTag.hasUUID(AnimalPenCompoundTags.TAG_STORAGE_ID))
        {
            return;
        }

        UUID redundantId = redundantTag.getUUID(AnimalPenCompoundTags.TAG_STORAGE_ID);

        // Load both lists from disk
        ListTag variantList = IndividualPenStorage.loadVariants(serverLevel, mainId);
        ListTag redundantList = IndividualPenStorage.loadVariants(serverLevel, redundantId);

        // Safely pull elements out of redundant list and move to main
        Iterator<Tag> iterator = redundantList.iterator();
        while (iterator.hasNext())
        {
            Tag currentTag = iterator.next();
            if (variantList.size() + 1 > AnimalPen.config().getMaxStoredVariants())
            {
                if (player != null)
                {
                    player.displayClientMessage(
                        Component.translatable("item.animal_pen.animal_cage.error.too_many_variants").
                            withStyle(ChatFormatting.DARK_RED), true);
                }
                break;
            }

            variantList.add(currentTag.copy());
            iterator.remove(); // Safely remove element from redundant list tracking
        }

        // Update the main item's save file
        IndividualPenStorage.saveVariants(serverLevel, mainId, variantList);

        // Clean up the redundant item's save file
        if (redundantList.isEmpty())
        {
            IndividualPenStorage.deleteFile(serverLevel, redundantId);
            redundantTag.remove(AnimalPenCompoundTags.TAG_STORAGE_ID);
        }
        else
        {
            // If the pen hit maximum limit, write back whatever remnants didn't fit
            IndividualPenStorage.saveVariants(serverLevel, redundantId, redundantList);
        }
    }


    public static boolean customInteraction(EntityType<?> entityType, ItemStack itemStack)
    {
        return itemStack.is(AnimalPensItemRegistry.ANIMAL_CAGE.get()) &&
            entityType.is(AnimalPenTags.ANIMAL_CAGE_PICKABLE) ||
            itemStack.is(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()) &&
                entityType.is(AnimalPenTags.WATER_MOB_CONTAINER_PICKABLE) ||
            itemStack.is(AnimalPensItemRegistry.BIRD_CATCHER.get()) &&
                entityType.is(AnimalPenTags.BIRD_CATCHER_PICKABLE);
    }
}
