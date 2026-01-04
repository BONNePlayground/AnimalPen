//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
        AnimalPen.resourceOf("animal_pens"));

    /**
     * Tags for animal pen items.
     */
    public static final TagKey<Item> ANIMAL_PEN_ITEMS = TagKey.create(Registries.ITEM,
        AnimalPen.resourceOf( "animal_pens"));

    /**
     * Tag that stores which tools can attack entity in animal pen
     */
    public static final TagKey<Item> ANIMAL_PEN_ATTACK_TOOLS = TagKey.create(Registries.ITEM,
        AnimalPen.resourceOf( "can_attack_pen"));

    /**
     * Tag that stores which tools can attack entity in aquarium
     */
    public static final TagKey<Item> AQUARIUM_ATTACK_TOOLS = TagKey.create(Registries.ITEM,
        AnimalPen.resourceOf( "can_attack_aquarium"));

    /**
     * Tag that stores which tools can attack entity in bird cage
     */
    public static final TagKey<Item> AVIARY_ATTACK_TOOLS = TagKey.create(Registries.ITEM,
        AnimalPen.resourceOf( "can_attack_aviary"));

    /**
     * Tag that stores forge shears items.
     */
    public static final TagKey<Item> FORGE_SHEARS = TagKey.create(Registries.ITEM,
        new ResourceLocation("forge", "shears"));

    /**
     * Tag that stores common shears items.
     */
    public static final TagKey<Item> COMMON_SHEARS = TagKey.create(Registries.ITEM,
        new ResourceLocation("c", "shears"));

    /**
     * Tag that stores entities pickable by animal cage.
     */
    public static final TagKey<EntityType<?>> ANIMAL_CAGE_PICKABLE = TagKey.create(Registries.ENTITY_TYPE,
        AnimalPen.resourceOf( "animal_cage_pickable"));

    /**
     * Tag that stores entities pickable by water mob container.
     */
    public static final TagKey<EntityType<?>> WATER_MOB_CONTAINER_PICKABLE = TagKey.create(Registries.ENTITY_TYPE,
        AnimalPen.resourceOf( "water_mob_container_pickable"));

    /**
     * Tag that stores entities pickable by bird catcher.
     */
    public static final TagKey<EntityType<?>> BIRD_CATCHER_PICKABLE = TagKey.create(Registries.ENTITY_TYPE,
        AnimalPen.resourceOf( "bird_catcher_pickable"));
}
