//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;


@FunctionalInterface
public interface ModEntityTypeTagsProvider
{
    SimpleTagAppender<EntityType<?>> modTag(TagKey<EntityType<?>> tag);


    default void addModTags()
    {
        this.modTag(AnimalPenTags.ANIMAL_CAGE_PICKABLE).
            add(EntityTypes.ARMADILLO).
            add(EntityTypes.CAMEL).
            add(EntityTypes.CAMEL_HUSK).
            add(EntityTypes.CAT).
            add(EntityTypes.CHICKEN).
            add(EntityTypes.COW).
            add(EntityTypes.DONKEY).
            add(EntityTypes.FROG).
            add(EntityTypes.FOX).
            add(EntityTypes.GOAT).
            add(EntityTypes.HOGLIN).
            add(EntityTypes.HORSE).
            add(EntityTypes.LLAMA).
            add(EntityTypes.MOOSHROOM).
            add(EntityTypes.MULE).
            add(EntityTypes.OCELOT).
            add(EntityTypes.PANDA).
            add(EntityTypes.PIG).
            add(EntityTypes.POLAR_BEAR).
            add(EntityTypes.RABBIT).
            add(EntityTypes.SHEEP).
            add(EntityTypes.SKELETON_HORSE).
            add(EntityTypes.SNIFFER).
            add(EntityTypes.STRIDER).
            add(EntityTypes.TRADER_LLAMA).
            add(EntityTypes.WOLF).
            add(EntityTypes.ZOMBIE_HORSE);
        this.modTag(AnimalPenTags.WATER_MOB_CONTAINER_PICKABLE).
            add(EntityTypes.AXOLOTL).
            add(EntityTypes.COD).
            add(EntityTypes.FROG).
            add(EntityTypes.PUFFERFISH).
            add(EntityTypes.SALMON).
            add(EntityTypes.TROPICAL_FISH).
            add(EntityTypes.DOLPHIN).
            add(EntityTypes.SQUID).
            add(EntityTypes.GLOW_SQUID).
            add(EntityTypes.TADPOLE).
            add(EntityTypes.NAUTILUS).
            add(EntityTypes.ZOMBIE_NAUTILUS).
            add(EntityTypes.TURTLE);
        this.modTag(AnimalPenTags.BIRD_CATCHER_PICKABLE).
            add(EntityTypes.ALLAY).
            add(EntityTypes.BEE).
            add(EntityTypes.BAT).
            add(EntityTypes.HAPPY_GHAST).
            add(EntityTypes.PARROT);
    }
}
