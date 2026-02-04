//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.registries;


import lv.id.bonne.animalpen.advancements.critereon.AnimalCaughtTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalInteractTrigger;
import net.minecraft.advancements.CriteriaTriggers;


public class AnimalPenCriteriaTriggersRegistry
{
    public static void register()
    {
    }


    public static final AnimalCaughtTrigger ANIMAL_CAUGHT_TRIGGER = CriteriaTriggers.register(new AnimalCaughtTrigger());

    public static final AnimalInteractTrigger ANIMAL_INTERACT_TRIGGER = CriteriaTriggers.register(new AnimalInteractTrigger());
}
