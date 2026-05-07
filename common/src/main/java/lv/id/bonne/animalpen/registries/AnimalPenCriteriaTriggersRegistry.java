//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.registries;


import java.util.function.Supplier;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.advancements.critereon.AnimalInteractTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalItemUseTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalVariantChangeTrigger;
import lv.id.bonne.animalpen.platform.Services;


public class AnimalPenCriteriaTriggersRegistry
{
    public static void register()
    {
    }


    public static final Supplier<AnimalItemUseTrigger> ANIMAL_ITEM_USE_TRIGGER =
        Services.REGISTRY.registerTrigger(AnimalPen.resourceOf("animal_caught"), AnimalItemUseTrigger::new);

    public static final Supplier<AnimalInteractTrigger> ANIMAL_INTERACT_TRIGGER =
        Services.REGISTRY.registerTrigger(AnimalPen.resourceOf("animal_interact"), AnimalInteractTrigger::new);

    public static final Supplier<AnimalVariantChangeTrigger> ANIMAL_VARIANT_CHANGE_TRIGGER =
        Services.REGISTRY.registerTrigger(AnimalPen.resourceOf("change_variant"), AnimalVariantChangeTrigger::new);
}
