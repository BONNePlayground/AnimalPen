//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.util;


import org.jetbrains.annotations.Nullable;
import java.util.Iterator;
import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.items.component.StoredMobVariantKey;
import lv.id.bonne.animalpen.items.component.StoredMobVariants;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
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
        if (animal.level().isClientSide() || !(animal.level() instanceof ServerLevel serverLevel))
        {
            return false;
        }

        Optional<ListTag> animalVariants = AnimalPenVariantHelper.getAnimalVariants(itemStack, serverLevel);

        if (animalVariants.isEmpty() && !itemStack.has(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get()))
        {
            // Creates new key if it does not exist.
            itemStack.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(), StoredMobVariantKey.of());
        }

        // Load variants directly from the item's individual save file
        ListTag variantList = animalVariants.orElse(new ListTag());

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

        CompoundTag animalTag = new CompoundTag();
        animal.save(animalTag);
        animalTag.remove("Pos");
        animalTag.remove("UUID");

        if (!variantList.contains(animalTag))
        {
            variantList.add(animalTag);
            // Save data back out to disk immediately
            StoredMobVariantKey variantKey =
                itemStack.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());

            IndividualPenStorage.saveVariants(serverLevel, variantKey, variantList);

            itemStack.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(),
                variantKey.withSize(variantList.size()));
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

        if (itemStack.has(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get()))
        {
            // Migration
            StoredMobVariants storedVariants =
                itemStack.remove(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get());
            StoredMobVariantKey variantKey = StoredMobVariantKey.of(storedVariants.variants().size());
            itemStack.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(), variantKey);
            IndividualPenStorage.saveVariants(serverLevel, variantKey, storedVariants.variants());
        }

        if (itemStack.has(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get()))
        {
            StoredMobVariantKey storedKey =
                itemStack.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());
            ListTag variantList = IndividualPenStorage.loadVariants(serverLevel, storedKey);
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

        if (!mainItem.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()) ||
            !redundantItem.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
        {
            return false;
        }

        StoredMobVariantKey mainKey = mainItem.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());
        StoredMobVariantKey redundantKey = redundantItem.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());

        if (mainKey.amount() + redundantKey.amount() > AnimalPen.config().getMaxStoredVariants())
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

        StoredMobVariantKey mainKey = mainItem.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());
        StoredMobVariantKey redundantKey = redundantItem.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());

        if (mainKey == null)
        {
            // Set new key
            mainKey = StoredMobVariantKey.of();
        }

        if (redundantKey == null)
        {
            // Nothing to merge
            return;
        }

        // Load both lists from disk
        ListTag variantList = IndividualPenStorage.loadVariants(serverLevel, mainKey);
        ListTag redundantList = IndividualPenStorage.loadVariants(serverLevel, redundantKey);

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

            variantList.add(currentTag);
            iterator.remove();
        }

        // Update the main item's save file
        IndividualPenStorage.saveVariants(serverLevel, mainKey, variantList);
        mainItem.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(),
            mainKey.withSize(variantList.size()));

        // Clean up the redundant item's save file
        if (redundantList.isEmpty())
        {
            IndividualPenStorage.deleteFile(serverLevel, redundantKey);
            redundantItem.remove(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());
        }
        else
        {
            // If the pen hit maximum limit, write back whatever remnants didn't fit
            IndividualPenStorage.saveVariants(serverLevel, redundantKey, redundantList);
            redundantItem.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(),
                redundantKey.withSize(redundantList.size()));

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
