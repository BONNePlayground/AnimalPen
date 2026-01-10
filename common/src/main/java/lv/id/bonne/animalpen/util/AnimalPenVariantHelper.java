//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.util;


import org.jetbrains.annotations.Nullable;
import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


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

        CompoundTag itemTag = itemStack.getOrCreateTag();

        if (!itemTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
        {
            return false;
        }

        ListTag variantList = itemTag.getList(AnimalPenCompoundTags.TAG_VARIANTS, Tag.TAG_COMPOUND);

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
            // small optimization?
            variantList.add(variant);
            itemTag.put(AnimalPenCompoundTags.TAG_VARIANTS, variantList);
            itemStack.setTag(itemTag);
        }

        return true;
    }


    /**
     * This method returns Optional list-tag or animal variants in given item-stack
     *
     * @param itemStack The item stack that need to be checked.
     * @return Optional list of tags for animal variants.
     */
    public static Optional<ListTag> getAnimalVariants(ItemStack itemStack)
    {
        if (itemStack.getOrCreateTag().contains(AnimalPenCompoundTags.TAG_VARIANTS))
        {
            return Optional.of(itemStack.getOrCreateTag().getList(AnimalPenCompoundTags.TAG_VARIANTS,
                Tag.TAG_COMPOUND));
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

        CompoundTag itemTag = mainItem.getOrCreateTag();
        CompoundTag redundantTag = redundantItem.getOrCreateTag();

        if (!itemTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(AnimalPenCompoundTags.TAG_ENTITY_ID) ||
            !redundantTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
        {
            return false;
        }

        ListTag variantList = itemTag.getList(AnimalPenCompoundTags.TAG_VARIANTS, Tag.TAG_COMPOUND);
        ListTag redundantList = redundantTag.getList(AnimalPenCompoundTags.TAG_VARIANTS, Tag.TAG_COMPOUND);

        if (variantList.size() + redundantList.size() > AnimalPen.config().getMaxStoredVariants())
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

        CompoundTag itemTag = mainItem.getOrCreateTag();
        CompoundTag redundantTag = redundantItem.getOrCreateTag();

        if (!itemTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(AnimalPenCompoundTags.TAG_ENTITY_ID) ||
            !redundantTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL).contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
        {
            return;
        }

        ListTag variantList = itemTag.getList(AnimalPenCompoundTags.TAG_VARIANTS, Tag.TAG_COMPOUND);
        ListTag redundantList = redundantTag.getList(AnimalPenCompoundTags.TAG_VARIANTS, Tag.TAG_COMPOUND);

        for (Tag tag : redundantList)
        {
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

            variantList.add(tag);
        }

        itemTag.put(AnimalPenCompoundTags.TAG_VARIANTS, variantList);
        mainItem.setTag(itemTag);
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
