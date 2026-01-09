//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.util;


import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.FlowerBlock;


public class AnimalPenItemHelper
{
    /**
     * This method returns flower effect if given item stack has one.
     */
    public static Optional<SuspiciousStewEffects> getEffectFromItemStack(ItemStack itemStack)
    {
        Item item = itemStack.getItem();

        if (!(item instanceof BlockItem blockItem) ||
            !(blockItem.getBlock() instanceof FlowerBlock flowerBlock))
        {
            return Optional.empty();
        }

        return Optional.of(flowerBlock.getSuspiciousEffects());
    }


    /**
     * This method replaces given type of bucket with a proper outcome bucket.
     */
    public static ItemStack replacement(ItemStack itemStack)
    {
        if (itemStack.is(Items.AXOLOTL_BUCKET) ||
            itemStack.is(Items.COD_BUCKET) ||
            itemStack.is(Items.SALMON_BUCKET) ||
            itemStack.is(Items.TROPICAL_FISH_BUCKET) ||
            itemStack.is(Items.PUFFERFISH_BUCKET))
        {
            return new ItemStack(Items.WATER_BUCKET);
        }
        else if (itemStack.is(Items.MILK_BUCKET) ||
            itemStack.is(Items.LAVA_BUCKET) ||
            itemStack.is(Items.WATER_BUCKET) ||
            itemStack.is(Items.POWDER_SNOW_BUCKET))
        {
            return new ItemStack(Items.BUCKET);
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }


    /**
     * Item tag accessor from resource location value used in DataGen.
     */
    public static TagKey<Item> itemTag(Identifier value)
    {
        return TagKey.create(Registries.ITEM, value);
    }


    /**
     * Item tag accessor from string value used in DataGen.
     */
    public static TagKey<Item> itemTag(String value)
    {
        return itemTag(Identifier.tryParse(value));
    }


    /**
     * This method returns stored cooldowns from given data component holder.
     * @param componentHolder - the data component holder.
     * @return Map of stored cooldowns.
     */
    public static Map<String, Integer> getCooldowns(DataComponentHolder componentHolder)
    {
        return componentHolder.has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()) ?
            componentHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()).cooldowns() :
            Collections.emptyMap();
    }


    public static long getMobCount(DataComponentHolder componentHolder)
    {
        return componentHolder.has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()) ?
            componentHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()).animalCount() :
            0;
    }


    /**
     * This map stores dye color link to a proper wool item.
     */
    public static final Map<DyeColor, ItemLike> ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), (enumMap) ->
    {
        enumMap.put(DyeColor.WHITE, Items.WHITE_WOOL);
        enumMap.put(DyeColor.ORANGE, Items.ORANGE_WOOL);
        enumMap.put(DyeColor.MAGENTA, Items.MAGENTA_WOOL);
        enumMap.put(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_WOOL);
        enumMap.put(DyeColor.YELLOW, Items.YELLOW_WOOL);
        enumMap.put(DyeColor.LIME, Items.LIME_WOOL);
        enumMap.put(DyeColor.PINK, Items.PINK_WOOL);
        enumMap.put(DyeColor.GRAY, Items.GRAY_WOOL);
        enumMap.put(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_WOOL);
        enumMap.put(DyeColor.CYAN, Items.CYAN_WOOL);
        enumMap.put(DyeColor.PURPLE, Items.PURPLE_WOOL);
        enumMap.put(DyeColor.BLUE, Items.BLUE_WOOL);
        enumMap.put(DyeColor.BROWN, Items.BROWN_WOOL);
        enumMap.put(DyeColor.GREEN, Items.GREEN_WOOL);
        enumMap.put(DyeColor.RED, Items.RED_WOOL);
        enumMap.put(DyeColor.BLACK, Items.BLACK_WOOL);
    });
}
