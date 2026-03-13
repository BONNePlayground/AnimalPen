//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.processing.function.core.*;
import lv.id.bonne.animalpen.processing.function.wrapper.EntityFunctionEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;


/**
 * This class makes new ENTITY_FUNCTIONS registry for everyone to access.
 */
public class AnimalPenFunctionRegistry
{
    public static void register()
    {
    }


    /**
     * Registry entity_function resource key.
     */
    public static final ResourceKey<Registry<EntityFunctionEntry>> ENTITY_FUNCTIONS_REGISTRY_KEY =
        ResourceKey.createRegistryKey(AnimalPen.resourceOf("entity_function"));


    /**
     * The actual registry of entity_functions.
     */
    public static final Registrar<EntityFunctionEntry> ENTITY_FUNCTIONS = RegistrarManager.get(AnimalPen.MOD_ID).
        builder(AnimalPen.resourceOf("entity_function"), new EntityFunctionEntry[0]).
        saveToDisc().
        build();


// ---------------------------------------------------------------------
// Section: Registry Objects
// ---------------------------------------------------------------------

    public static final RegistrySupplier<EntityFunctionEntry> FEEDING = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("feeding"),
        () -> new EntityFunctionEntry(new Feeding(false))
    );

    public static final RegistrySupplier<EntityFunctionEntry> DUPLICATE = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("duplicate"),
        () -> new EntityFunctionEntry(new Feeding(true))
    );

    /**
     * This is replaced with `mob_set_sheared` and left just as backup.
     */
    @Deprecated
    public static final RegistrySupplier<EntityFunctionEntry> SHEEP_SET_SHEARED = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("sheep_set_sheared"),
        () -> new EntityFunctionEntry(new MobSetSheared())
    );

    public static final RegistrySupplier<EntityFunctionEntry> MOB_SET_SHEARED = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("mob_set_sheared"),
        () -> new EntityFunctionEntry(new MobSetSheared())
    );

    public static final RegistrySupplier<EntityFunctionEntry> SHEEP_CHANGE_COLOR = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("sheep_change_color"),
        () -> new EntityFunctionEntry(new SheepChangeColor())
    );

    public static final RegistrySupplier<EntityFunctionEntry> MOOSHROOM_SET_EFFECT = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("mooshroom_set_effect"),
        () -> new EntityFunctionEntry(new MooshroomEffectApply())
    );

    public static final RegistrySupplier<EntityFunctionEntry> MOOSHROOM_FAILED_EFFECT = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("mooshroom_failed_effect"),
        () -> new EntityFunctionEntry(new MooshroomEffectFail())
    );

    public static final RegistrySupplier<EntityFunctionEntry> MOOSHROOM_REMOVE_EFFECT = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("mooshroom_remove_effect"),
        () -> new EntityFunctionEntry(new MooshroomEffectRemove())
    );

    /**
     * This is replaced with `bucketable_pickup` and left just as backup.
     */
    @Deprecated
    public static final RegistrySupplier<EntityFunctionEntry> WATER_BUCKET_PICKUP = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("water_bucket_pickup"),
        () -> new EntityFunctionEntry(new BucketablePickup())
    );

    public static final RegistrySupplier<EntityFunctionEntry> BUCKETABLE_PICKUP = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("bucketable_pickup"),
        () -> new EntityFunctionEntry(new BucketablePickup())
    );

    public static final RegistrySupplier<EntityFunctionEntry> INCREMENT_KEY = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("increment_key"),
        () -> new EntityFunctionEntry(new IncrementIntegerDataKey())
    );

    public static final RegistrySupplier<EntityFunctionEntry> TURTLE_DROP_SCUTE = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("turtle_drop_scute"),
        () -> new EntityFunctionEntry(new TurtleScuteDrop())
    );

    public static final RegistrySupplier<EntityFunctionEntry> DROP_LOOT = ENTITY_FUNCTIONS.register(
        AnimalPen.resourceOf("drop_loot"),
        () -> new EntityFunctionEntry(new DropRequestedLoot())
    );
}
