//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.items.AnimalCageItem;
import lv.id.bonne.animalpen.items.AnimalContainerItem;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;


public class AnimalPensItemRegistry
{
    public static void register()
    {
        REGISTRY.register();
    }


    /**
     * The main item registry.
     */
    public static final DeferredRegister<Item> REGISTRY =
        DeferredRegister.create(AnimalPen.MOD_ID, Registry.ITEM_REGISTRY);

    /**
     * The animal cage item
     */
    public static final RegistrySupplier<AnimalCageItem> ANIMAL_CAGE =
        REGISTRY.register("animal_cage", () -> new AnimalCageItem(new Item.Properties().
            tab(AnimalPensCreativeTabRegistry.ANIMAL_PEN_TAB)));

    /**
     * The animal container item
     */
    public static final RegistrySupplier<AnimalContainerItem> ANIMAL_CONTAINER =
        REGISTRY.register("water_animal_container", () -> new AnimalContainerItem(new Item.Properties().
            tab(AnimalPensCreativeTabRegistry.ANIMAL_PEN_TAB)));
}
