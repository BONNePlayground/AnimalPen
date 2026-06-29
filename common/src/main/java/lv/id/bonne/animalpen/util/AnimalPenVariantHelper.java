//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.util;


import org.jetbrains.annotations.Nullable;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.items.component.StoredMobVariantKey;
import lv.id.bonne.animalpen.items.component.StoredMobVariants;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import lv.id.bonne.animalpen.data.saveddata.IndividualPenStorage;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
    public static boolean storeAnimalVariant(ItemStack itemStack, Mob animal, @Nullable Player player, Consumer<String> onError)
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

        Optional<IndividualPenStorage> animalVariants = AnimalPenVariantHelper.getAnimalVariants(itemStack, serverLevel);

        if (animalVariants.isEmpty() && !itemStack.has(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get()))
        {
            // Creates new key if it does not exist.
            itemStack.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(), StoredMobVariantKey.of());
        }

        StoredMobVariantKey variantKey =
            itemStack.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());

        if (variantKey == null)
        {
            variantKey = StoredMobVariantKey.of();
        }

        // Load variants directly from the item's individual save file
        IndividualPenStorage storage = IndividualPenStorage.getOrCreate(serverLevel, variantKey.key());

        if (storage.getVariants().size() + 1 > AnimalPen.config().getMaxStoredVariants())
        {
            if (player != null)
            {
                onError.accept(".error.too_many_variants");
            }

            return false;
        }

        CompoundTag animalTag = new CompoundTag();
        animal.save(animalTag);
        animalTag.remove("Pos");
        animalTag.remove("UUID");

        if (!storage.getVariants().contains(animalTag))
        {
            storage.getVariants().add(animalTag);
            storage.setDirty();

            itemStack.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(),
                variantKey.withSize(storage.getVariants().size()));
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
    public static Optional<IndividualPenStorage> getAnimalVariants(ItemStack itemStack, Level level)
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

            if (storedVariants == null)
            {
                return Optional.empty();
            }

            StoredMobVariantKey variantKey = StoredMobVariantKey.of(storedVariants.variants().size());
            itemStack.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(), variantKey);

            IndividualPenStorage storage = IndividualPenStorage.getOrCreate(serverLevel, variantKey.key());
            storage.setVariants(storedVariants.variants());

            return Optional.of(storage);
        }

        StoredMobVariantKey storedKey =
            itemStack.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());

        if (storedKey == null)
        {
            itemStack.remove(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());
            return Optional.empty();
        }

        return Optional.of(IndividualPenStorage.getOrCreate(serverLevel,
            storedKey.key()));
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
    public static boolean canMergeAnimalVariants(ItemStack mainItem,
        ItemStack redundantItem,
        Level level,
        @Nullable Player player,
        Consumer<String> onError)
    {
        if (AnimalPen.config().getMaxStoredVariants() <= 0)
        {
            return true;
        }

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel))
        {
            return false;
        }

        StoredMobVariantKey mainKey = mainItem.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());
        StoredMobVariantKey redundantKey = redundantItem.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());

        int mainAmount = mainKey == null ? 0 : mainKey.amount();
        int redundantAmount = redundantKey == null ? 0 : redundantKey.amount();

        if (mainAmount + redundantAmount > AnimalPen.config().getMaxStoredVariants())
        {
            if (player != null)
            {
                onError.accept(".error.too_many_variants");
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
    public static void mergeAnimalVariants(ItemStack mainItem,
        ItemStack redundantItem,
        Level level,
        @Nullable Player player,
        Consumer<String> onError)
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

        if (mainKey.key().equals(redundantKey.key()))
        {
            // This should happen only if admin commands are in use.
            return;
        }

        // Load both lists from disk
        IndividualPenStorage mainStorage = IndividualPenStorage.getOrCreate(serverLevel, mainKey.key());
        IndividualPenStorage redundantStorage = IndividualPenStorage.getOrCreate(serverLevel, redundantKey.key());

        // Safely pull elements out of redundant list and move to main
        Iterator<Tag> iterator = redundantStorage.getVariants().iterator();
        while (iterator.hasNext())
        {
            Tag currentTag = iterator.next();
            if (mainStorage.getVariants().size() + 1 > AnimalPen.config().getMaxStoredVariants())
            {
                if (player != null)
                {
                    onError.accept(".error.too_many_variants");
                }
                break;
            }

            mainStorage.getVariants().add(currentTag);
            iterator.remove();
        }

        // Update the main item's save file
        mainItem.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(),
            mainKey.withSize(mainStorage.getVariants().size()));

        mainStorage.setDirty();
        redundantStorage.setDirty();

        // Clean up the redundant item's save file
        if (redundantStorage.getVariants().isEmpty())
        {
            IndividualPenStorage.delete(serverLevel, redundantKey.key());
            redundantItem.remove(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());
        }
        else
        {
            // If the pen hit maximum limit, write back whatever remnants didn't fit
            redundantItem.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(),
                redundantKey.withSize(redundantStorage.getVariants().size()));
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
