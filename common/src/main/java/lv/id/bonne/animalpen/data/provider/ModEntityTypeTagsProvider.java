//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;


public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider
{
    public ModEntityTypeTagsProvider(DataGenerator generator)
    {
        super(generator);
    }


    @Override
    protected void addTags()
    {
        this.tag(AnimalPenTags.ANIMAL_CAGE_PICKABLE).
            add(EntityType.BEE).
            add(EntityType.CAT).
            add(EntityType.CHICKEN).
            add(EntityType.COW).
            add(EntityType.DONKEY).
            add(EntityType.FOX).
            add(EntityType.GOAT).
            add(EntityType.HOGLIN).
            add(EntityType.HORSE).
            add(EntityType.LLAMA).
            add(EntityType.MOOSHROOM).
            add(EntityType.MULE).
            add(EntityType.OCELOT).
            add(EntityType.PANDA).
            add(EntityType.PARROT).
            add(EntityType.PIG).
            add(EntityType.POLAR_BEAR).
            add(EntityType.RABBIT).
            add(EntityType.SHEEP).
            add(EntityType.SKELETON_HORSE).
            add(EntityType.STRIDER).
            add(EntityType.TRADER_LLAMA).
            add(EntityType.WOLF).
            add(EntityType.ZOMBIE_HORSE);
        this.tag(AnimalPenTags.WATER_MOB_CONTAINER_PICKABLE).
            add(EntityType.AXOLOTL).
            add(EntityType.COD).
            add(EntityType.PUFFERFISH).
            add(EntityType.SALMON).
            add(EntityType.TROPICAL_FISH).
            add(EntityType.DOLPHIN).
            add(EntityType.SQUID).
            add(EntityType.GLOW_SQUID).
            add(EntityType.TURTLE);
    }


    @Override
    public String getName()
    {
        return "Animal Pen EntityType Tags";
    }
}
