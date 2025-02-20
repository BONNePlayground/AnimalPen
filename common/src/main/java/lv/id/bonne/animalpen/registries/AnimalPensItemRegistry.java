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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
        DeferredRegister.create(AnimalPen.MOD_ID, Registries.ITEM);

    /**
     * The animal cage item
     */
    public static final RegistrySupplier<AnimalCageItem> ANIMAL_CAGE =
        REGISTRY.register("animal_cage", () -> new AnimalCageItem(new Item.Properties().
            arch$tab(AnimalPensCreativeTabRegistry.ANIMAL_PEN_TAB).stacksTo(1).
            setId(ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(AnimalPen.MOD_ID, "animal_cage")))));

    /**
     * The animal container item
     */
    public static final RegistrySupplier<AnimalContainerItem> ANIMAL_CONTAINER =
        REGISTRY.register("water_animal_container", () -> new AnimalContainerItem(new Item.Properties().
            arch$tab(AnimalPensCreativeTabRegistry.ANIMAL_PEN_TAB).stacksTo(1).
            setId(ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(AnimalPen.MOD_ID, "water_animal_container")))));
}
