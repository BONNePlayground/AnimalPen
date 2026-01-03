//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import java.util.function.Supplier;

import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;


@FunctionalInterface
public interface ModBlockTagsProvider
{
    SimpleTagAppender<Block> modTag(TagKey<Block> tag);


    default void addModTags()
    {
        this.modTag(AnimalPenTags.ANIMAL_PEN_BLOCKS).add(
            AnimalPenBlockRegistry.ANIMAL_PENS.values().
                stream().
                map(Supplier::get).
                toArray(Block[]::new)
        );
    }
}