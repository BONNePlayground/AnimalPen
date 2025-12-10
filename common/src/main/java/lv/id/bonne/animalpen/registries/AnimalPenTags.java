//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;


public class AnimalPenTags
{
    /**
     * Tags for animal pen blocks.
     */
    public static final TagKey<Block> ANIMAL_PEN_BLOCKS = TagKey.create(Registries.BLOCK,
        Identifier.fromNamespaceAndPath(AnimalPen.MOD_ID, "animal_pens"));

    /**
     * Tag that stores which tools can attack entity in animal pen
     */
    public static final TagKey<Item> ANIMAL_PEN_ATTACK_TOOLS = TagKey.create(Registries.ITEM,
        Identifier.fromNamespaceAndPath(AnimalPen.MOD_ID, "can_attack_pen"));

    /**
     * Tag that stores which tools can attack entity in aquarium
     */
    public static final TagKey<Item> AQUARIUM_ATTACK_TOOLS = TagKey.create(Registries.ITEM,
        Identifier.fromNamespaceAndPath(AnimalPen.MOD_ID, "can_attack_aquarium"));

    /**
     * Tag that stores common shears items.
     */
    public static final TagKey<Item> COMMON_SHEARS = TagKey.create(Registries.ITEM,
        Identifier.fromNamespaceAndPath("c", "tools/shear"));

    /**
     * Tag that stores common brushes items.
     */
    public static final TagKey<Item> COMMON_BRUSHES = TagKey.create(Registries.ITEM,
        Identifier.fromNamespaceAndPath("c", "tools/brush"));
}
