//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.registries;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.platform.Services;
import lv.id.bonne.animalpen.processing.function.wrapper.EntityFunctionEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;


/**
 * Manages all custom registries specific to the Animal Pen mod.
 * This class provides centralized access points for key resources, such as entity functions.
 */
public class AnimalPenRegistry
{
    /**
     * Empty registration method.
     * This method is intended to hold logic that executes when the mod initializes,
     * potentially calling registration functions for various registry types.
     */
    public static void register()
    {
    }


    /**
     * The resource key used to uniquely identify the custom entity function registry
     * associated with the Animal Pen mod.
     */
    public static final ResourceKey<Registry<EntityFunctionEntry>> ENTITY_FUNCTIONS_REGISTRY_KEY =
        ResourceKey.createRegistryKey(AnimalPen.resourceOf("entity_function"));


    /**
     * The fully instantiated registry object for all Animal Pen entity functions.
     * This object must be initialized through {@link Services#REGISTRY}.
     */
    public static Registry<EntityFunctionEntry> ENTITY_FUNCTIONS =
        Services.REGISTRY.createRegistry(AnimalPenRegistry.ENTITY_FUNCTIONS_REGISTRY_KEY);
}
