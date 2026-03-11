//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.registries;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.advancements.critereon.AnimalItemUseTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalInteractTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalVariantChangeTrigger;
import net.minecraft.advancements.CriteriaTriggers;


public class AnimalPenCriteriaTriggersRegistry
{
    public static void register()
    {
    }


    public static final AnimalItemUseTrigger ANIMAL_ITEM_USE_TRIGGER =
        CriteriaTriggers.register(AnimalPen.resourceOf("animal_caught").toString(), new AnimalItemUseTrigger());

    public static final AnimalInteractTrigger ANIMAL_INTERACT_TRIGGER =
        CriteriaTriggers.register(AnimalPen.resourceOf("animal_interact").toString(), new AnimalInteractTrigger());

    public static final AnimalVariantChangeTrigger ANIMAL_VARIANT_CHANGE_TRIGGER =
        CriteriaTriggers.register(AnimalPen.resourceOf("change_variant").toString(), new AnimalVariantChangeTrigger());
}
