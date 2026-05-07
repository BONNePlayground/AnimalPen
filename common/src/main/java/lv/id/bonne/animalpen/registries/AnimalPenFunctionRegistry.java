//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import java.util.function.Supplier;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.platform.Services;
import lv.id.bonne.animalpen.processing.function.core.*;
import lv.id.bonne.animalpen.processing.function.wrapper.EntityFunctionEntry;


/**
 * This class makes new ENTITY_FUNCTIONS registry for everyone to access.
 */
public class AnimalPenFunctionRegistry
{
    public static void register()
    {
    }


// ---------------------------------------------------------------------
// Section: Registry Objects
// ---------------------------------------------------------------------

    public static final Supplier<EntityFunctionEntry> FEEDING = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("feeding"),
        () -> new EntityFunctionEntry(new Feeding(false))
    );

    public static final Supplier<EntityFunctionEntry> DUPLICATE = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("duplicate"),
        () -> new EntityFunctionEntry(new Feeding(true))
    );

    /**
     * This is replaced with `mob_set_sheared` and left just as backup.
     */
    @Deprecated
    public static final Supplier<EntityFunctionEntry> SHEEP_SET_SHEARED = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("sheep_set_sheared"),
        () -> new EntityFunctionEntry(new MobSetSheared())
    );

    public static final Supplier<EntityFunctionEntry> MOB_SET_SHEARED = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("mob_set_sheared"),
        () -> new EntityFunctionEntry(new MobSetSheared())
    );

    public static final Supplier<EntityFunctionEntry> SHEEP_CHANGE_COLOR = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("sheep_change_color"),
        () -> new EntityFunctionEntry(new SheepChangeColor())
    );

    public static final Supplier<EntityFunctionEntry> MOOSHROOM_SET_EFFECT = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("mooshroom_set_effect"),
        () -> new EntityFunctionEntry(new MooshroomEffectApply())
    );

    public static final Supplier<EntityFunctionEntry> MOOSHROOM_FAILED_EFFECT = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("mooshroom_failed_effect"),
        () -> new EntityFunctionEntry(new MooshroomEffectFail())
    );

    public static final Supplier<EntityFunctionEntry> MOOSHROOM_REMOVE_EFFECT = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("mooshroom_remove_effect"),
        () -> new EntityFunctionEntry(new MooshroomEffectRemove())
    );

    /**
     * This is replaced with `bucketable_pickup` and left just as backup.
     */
    @Deprecated
    public static final Supplier<EntityFunctionEntry> WATER_BUCKET_PICKUP = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("water_bucket_pickup"),
        () -> new EntityFunctionEntry(new BucketablePickup())
    );

    public static final Supplier<EntityFunctionEntry> BUCKETABLE_PICKUP = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("bucketable_pickup"),
        () -> new EntityFunctionEntry(new BucketablePickup())
    );

    public static final Supplier<EntityFunctionEntry> INCREMENT_KEY = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("increment_key"),
        () -> new EntityFunctionEntry(new IncrementIntegerDataKey())
    );

    public static final Supplier<EntityFunctionEntry> TURTLE_DROP_SCUTE = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("turtle_drop_scute"),
        () -> new EntityFunctionEntry(new TurtleScuteDrop())
    );

    public static final Supplier<EntityFunctionEntry> DROP_LOOT = Services.REGISTRY.registerFunction(
        AnimalPen.resourceOf("drop_loot"),
        () -> new EntityFunctionEntry(new DropRequestedLoot())
    );
}
