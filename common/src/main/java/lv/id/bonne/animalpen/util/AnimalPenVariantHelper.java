//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.util;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.items.component.StoredMobVariants;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;


/**
 * Some useful methods I did not know where to put.
 */
public class AnimalPenVariantHelper
{
    /**
     * This method stores given animal as a variant in given item stack.
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

        StoredMobVariants storedMobVariants =
            itemStack.get(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get());

        if (storedMobVariants != null &&
            storedMobVariants.variants().size() + 1 > AnimalPen.config().getMaxStoredVariants())
        {
            if (player != null)
            {
                player.displayClientMessage(
                    Component.translatable("item.animal_pen.animal_cage.error.too_many_variants").
                        withStyle(ChatFormatting.DARK_RED), true);
            }

            return false;
        }

        itemStack.update(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get(),
            StoredMobVariants.of(new ArrayList<>(AnimalPen.config().getMaxStoredVariants())),
            data ->
            {
                List<CompoundTag> variants = data.variants();

                CompoundTag animalTag = AnimalPenVariantHelper.saveMob(animal);
                animalTag.remove("Pos");
                animalTag.remove("UUID");
                variants.add(animalTag);

                return StoredMobVariants.of(variants);
            }
        );

        return true;
    }


    /**
     * This method returns Optional list-tag or animal variants in given item-stack
     *
     * @param itemStack The item stack that need to be checked.
     * @return Optional list of tags for animal variants.
     */
    public static Optional<List<CompoundTag>> getAnimalVariants(ItemStack itemStack)
    {
        if (itemStack.has(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get()))
        {
            return Optional.of(itemStack.get(
                AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get()).variants());
        }
        else
        {
            return Optional.empty();
        }
    }


    /**
     * This method returns if animal variants can be merged into main item variants.
     *
     * @param mainItem The item stack that should contain all variants
     * @param redundantItem The item stack that donates their variants
     * @param player A player instance
     * @return {@code true} if all variants can be added, {@code false} otherwise.
     */
    public static boolean canMergeAnimalVariants(ItemStack mainItem, ItemStack redundantItem, @Nullable Player player)
    {
        if (AnimalPen.config().getMaxStoredVariants() <= 0)
        {
            return true;
        }

        if (!mainItem.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()) ||
            !redundantItem.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
        {
            return false;
        }

        StoredMobVariants mainVariants = mainItem.getOrDefault(
            AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get(),
            StoredMobVariants.of(new ArrayList<>()));

        StoredMobVariants redundantVariants = redundantItem.getOrDefault(
            AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get(),
            StoredMobVariants.of(new ArrayList<>()));

        if (mainVariants.variants().size() + redundantVariants.variants().size() >
            AnimalPen.config().getMaxStoredVariants())
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
     * This method merges redundant item entity variants into main item stack.
     *
     * @param mainItem The item stack that should contain all variants
     * @param redundantItem The item stack that donates their variants
     * @param player A player instance
     */
    public static void mergeAnimalVariants(ItemStack mainItem, ItemStack redundantItem, @Nullable Player player)
    {
        if (AnimalPen.config().getMaxStoredVariants() <= 0)
        {
            return;
        }

        StoredMobVariants mainVariants = mainItem.getOrDefault(
            AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get(),
            StoredMobVariants.of(new ArrayList<>()));

        StoredMobVariants redundantVariants = redundantItem.getOrDefault(
            AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get(),
            StoredMobVariants.of(new ArrayList<>()));

        List<CompoundTag> mergedList = new ArrayList<>(AnimalPen.config().getMaxStoredVariants());
        mergedList.addAll(mainVariants.variants());

        int remaining = AnimalPen.config().getMaxStoredVariants() - mergedList.size();

        if (remaining > 0)
        {
            mergedList.addAll(redundantVariants.variants().subList(0,
                Math.min(remaining, redundantVariants.variants().size())));
        }

        mainItem.set(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get(),
            StoredMobVariants.of(mergedList));

        redundantItem.remove(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get());
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


    @NotNull
    public static CompoundTag saveMob(@NotNull Mob mob)
    {
        try (ProblemReporter.ScopedCollector scopedCollector =
                 new ProblemReporter.ScopedCollector(mob.problemPath(), AnimalPen.LOGGER))
        {
            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, mob.registryAccess());
            mob.saveWithoutId(tagValueOutput);

            return tagValueOutput.buildResult();
        }
    }


    public static void loadMob(@NotNull Mob mob, CompoundTag tag)
    {
        try (ProblemReporter.ScopedCollector scopedCollector =
                 new ProblemReporter.ScopedCollector(mob.problemPath(), AnimalPen.LOGGER))
        {
            ValueInput valueInput = TagValueInput.create(scopedCollector, mob.registryAccess(), tag);
            mob.load(valueInput);
        }
    }
}
