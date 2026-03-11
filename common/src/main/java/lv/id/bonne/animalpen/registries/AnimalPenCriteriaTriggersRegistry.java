//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.registries;


import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.advancements.critereon.AnimalItemUseTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalInteractTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalVariantChangeTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;


public class AnimalPenCriteriaTriggersRegistry
{
    public static void register()
    {
        REGISTRY.register();
    }

    public static final DeferredRegister<CriterionTrigger<?>> REGISTRY =
        DeferredRegister.create(AnimalPen.MOD_ID, Registries.TRIGGER_TYPE);

    public static final RegistrySupplier<AnimalItemUseTrigger> ANIMAL_ITEM_USE_TRIGGER =
        REGISTRY.register(AnimalPen.resourceOf("animal_caught"), AnimalItemUseTrigger::new);

    public static final RegistrySupplier<AnimalInteractTrigger> ANIMAL_INTERACT_TRIGGER =
        REGISTRY.register(AnimalPen.resourceOf("animal_interact"), AnimalInteractTrigger::new);

    public static final RegistrySupplier<AnimalVariantChangeTrigger> ANIMAL_VARIANT_CHANGE_TRIGGER =
        REGISTRY.register(AnimalPen.resourceOf("change_variant"), AnimalVariantChangeTrigger::new);
}
