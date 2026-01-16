//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import lv.id.bonne.animalpen.data.helper.SimpleItemTagAppender;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;


@FunctionalInterface
public interface ModItemTagsProvider
{
    SimpleItemTagAppender modTag(TagKey<Item> tag);


    default void addModTags()
    {
        this.modTag(AnimalPenTags.ANIMAL_PEN_ATTACK_TOOLS).
            optionalTag(ResourceLocation.tryParse("forge:knives")).
            optionalTag(ResourceLocation.tryParse("forge:tools/knives")).
            optionalTag(ResourceLocation.tryParse("c:axes")).
            optionalTag(ResourceLocation.tryParse("c:swords")).
            optionalTag(ResourceLocation.tryParse("c:knives")).
            add(Items.WOODEN_SWORD).
            add(Items.STONE_SWORD).
            add(Items.IRON_SWORD).
            add(Items.DIAMOND_SWORD).
            add(Items.NETHERITE_SWORD).
            add(Items.WOODEN_AXE).
            add(Items.STONE_AXE).
            add(Items.IRON_AXE).
            add(Items.DIAMOND_AXE).
            add(Items.NETHERITE_AXE);

        this.modTag(AnimalPenTags.AQUARIUM_ATTACK_TOOLS).
            optionalTag(ResourceLocation.tryParse("forge:knives")).
            optionalTag(ResourceLocation.tryParse("forge:tools/knives")).
            optionalTag(ResourceLocation.tryParse("c:axes")).
            optionalTag(ResourceLocation.tryParse("c:swords")).
            optionalTag(ResourceLocation.tryParse("c:knives")).
            add(Items.WOODEN_SWORD).
            add(Items.STONE_SWORD).
            add(Items.IRON_SWORD).
            add(Items.DIAMOND_SWORD).
            add(Items.NETHERITE_SWORD).
            add(Items.WOODEN_AXE).
            add(Items.STONE_AXE).
            add(Items.IRON_AXE).
            add(Items.DIAMOND_AXE).
            add(Items.NETHERITE_AXE);

        this.modTag(AnimalPenTags.AVIARY_ATTACK_TOOLS).
            optionalTag(ResourceLocation.tryParse("forge:knives")).
            optionalTag(ResourceLocation.tryParse("forge:tools/knives")).
            optionalTag(ResourceLocation.tryParse("c:axes")).
            optionalTag(ResourceLocation.tryParse("c:swords")).
            optionalTag(ResourceLocation.tryParse("c:knives")).
            add(Items.WOODEN_SWORD).
            add(Items.STONE_SWORD).
            add(Items.IRON_SWORD).
            add(Items.DIAMOND_SWORD).
            add(Items.NETHERITE_SWORD).
            add(Items.WOODEN_AXE).
            add(Items.STONE_AXE).
            add(Items.IRON_AXE).
            add(Items.DIAMOND_AXE).
            add(Items.NETHERITE_AXE);

        this.modTag(AnimalPenTags.ANIMAL_PEN_ITEMS).copy(AnimalPenTags.ANIMAL_PEN_BLOCKS);
        this.modTag(AnimalPenTags.AVIARIES_ITEMS).copy(AnimalPenTags.AVIARIES_BLOCKS);
    }
}
