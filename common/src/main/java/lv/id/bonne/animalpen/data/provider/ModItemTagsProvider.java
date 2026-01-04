//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import lv.id.bonne.animalpen.data.helper.SimpleItemTagAppender;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;


@FunctionalInterface
public interface ModItemTagsProvider
{
    SimpleItemTagAppender modTag(TagKey<Item> tag);


    default void addModTags()
    {
        this.modTag(AnimalPenTags.ANIMAL_PEN_ATTACK_TOOLS).
            optionalTag(TagKey.create(Registries.ITEM, ResourceLocation.tryParse("swords")).location()).
            optionalTag(TagKey.create(Registries.ITEM, ResourceLocation.tryParse("axes")).location()).
            optionalTag(ResourceLocation.tryParse("forge:knives")).
            optionalTag(ResourceLocation.tryParse("forge:tools/knives")).
            optionalTag(ResourceLocation.tryParse("c:knives"));

        this.modTag(AnimalPenTags.AQUARIUM_ATTACK_TOOLS).
            optionalTag(TagKey.create(Registries.ITEM, ResourceLocation.tryParse("swords")).location()).
            optionalTag(TagKey.create(Registries.ITEM, ResourceLocation.tryParse("axes")).location()).
            optionalTag(ResourceLocation.tryParse("forge:knives")).
            optionalTag(ResourceLocation.tryParse("forge:tools/knives")).
            optionalTag(ResourceLocation.tryParse("c:knives"));

        this.modTag(AnimalPenTags.AVIARY_ATTACK_TOOLS).
            optionalTag(TagKey.create(Registries.ITEM, ResourceLocation.tryParse("swords")).location()).
            optionalTag(TagKey.create(Registries.ITEM, ResourceLocation.tryParse("axes")).location()).
            optionalTag(ResourceLocation.tryParse("forge:knives")).
            optionalTag(ResourceLocation.tryParse("forge:tools/knives")).
            optionalTag(ResourceLocation.tryParse("c:knives"));

        this.modTag(AnimalPenTags.ANIMAL_PEN_ITEMS).copy(AnimalPenTags.ANIMAL_PEN_BLOCKS);
    }
}
