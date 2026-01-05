//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import lv.id.bonne.animalpen.data.helper.SimpleTagAppender;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;


@FunctionalInterface
public interface ModEntityTypeTagsProvider
{
    SimpleTagAppender<EntityType<?>> modTag(TagKey<EntityType<?>> tag);


    default void addModTags()
    {
        this.modTag(AnimalPenTags.ANIMAL_CAGE_PICKABLE).
            add(EntityType.CAMEL).
            add(EntityType.CAT).
            add(EntityType.CHICKEN).
            add(EntityType.COW).
            add(EntityType.DONKEY).
            add(EntityType.FROG).
            add(EntityType.FOX).
            add(EntityType.GOAT).
            add(EntityType.HOGLIN).
            add(EntityType.HORSE).
            add(EntityType.LLAMA).
            add(EntityType.MOOSHROOM).
            add(EntityType.MULE).
            add(EntityType.OCELOT).
            add(EntityType.PANDA).
            add(EntityType.PIG).
            add(EntityType.POLAR_BEAR).
            add(EntityType.RABBIT).
            add(EntityType.SHEEP).
            add(EntityType.SKELETON_HORSE).
            add(EntityType.SNIFFER).
            add(EntityType.STRIDER).
            add(EntityType.TRADER_LLAMA).
            add(EntityType.WOLF).
            add(EntityType.ZOMBIE_HORSE);
        this.modTag(AnimalPenTags.WATER_MOB_CONTAINER_PICKABLE).
            add(EntityType.AXOLOTL).
            add(EntityType.COD).
            add(EntityType.FROG).
            add(EntityType.PUFFERFISH).
            add(EntityType.SALMON).
            add(EntityType.TROPICAL_FISH).
            add(EntityType.DOLPHIN).
            add(EntityType.SQUID).
            add(EntityType.GLOW_SQUID).
            add(EntityType.TADPOLE).
            add(EntityType.TURTLE);
        this.modTag(AnimalPenTags.BIRD_CATCHER_PICKABLE).
            add(EntityType.BEE).
            add(EntityType.BAT).
            add(EntityType.PARROT);
    }
}
