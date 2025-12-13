//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.Registry;
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
    public static final TagKey<Block> ANIMAL_PEN_BLOCKS = TagKey.create(Registry.BLOCK_REGISTRY,
        new ResourceLocation(AnimalPen.MOD_ID, "animal_pens"));

    /**
     * Tag that stores which tools can attack entity in animal pen
     */
    public static final TagKey<Item> ANIMAL_PEN_ATTACK_TOOLS = TagKey.create(Registry.ITEM_REGISTRY,
        new ResourceLocation(AnimalPen.MOD_ID, "can_attack_pen"));

    /**
     * Tag that stores which tools can attack entity in aquarium
     */
    public static final TagKey<Item> AQUARIUM_ATTACK_TOOLS = TagKey.create(Registry.ITEM_REGISTRY,
        new ResourceLocation(AnimalPen.MOD_ID, "can_attack_aquarium"));

    /**
     * Tag that stores forge shears items.
     */
    public static final TagKey<Item> FORGE_SHEARS = TagKey.create(Registry.ITEM_REGISTRY,
        new ResourceLocation("forge", "shears"));

    /**
     * Tag that stores common shears items.
     */
    public static final TagKey<Item> COMMON_SHEARS = TagKey.create(Registry.ITEM_REGISTRY,
        new ResourceLocation("c", "shears"));

    /**
     * Tag that stores entities pickable by animal cage.
     */
    public static final TagKey<EntityType<?>> ANIMAL_CAGE_PICKABLE = TagKey.create(Registry.ENTITY_TYPE_REGISTRY,
        new ResourceLocation(AnimalPen.MOD_ID, "animal_cage_pickable"));

    /**
     * Tag that stores entities pickable by water mob container.
     */
    public static final TagKey<EntityType<?>> WATER_MOB_CONTAINER_PICKABLE = TagKey.create(Registry.ENTITY_TYPE_REGISTRY,
        new ResourceLocation(AnimalPen.MOD_ID, "water_mob_container_pickable"));
}
