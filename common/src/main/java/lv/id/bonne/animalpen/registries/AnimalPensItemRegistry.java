//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.items.AnimalCageItem;
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
     * The animal container item
     */
    public static final RegistrySupplier<AnimalCageItem> ANIMAL_CAGE =
        REGISTRY.register("animal_cage", () -> new AnimalCageItem(new Item.Properties().
            tab(AnimalPensCreativeTabRegistry.ANIMAL_PEN_TAB)));
}
