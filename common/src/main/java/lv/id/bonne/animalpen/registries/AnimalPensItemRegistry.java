//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import java.util.function.Supplier;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.items.AnimalCageItem;
import lv.id.bonne.animalpen.items.AnimalContainerItem;
import lv.id.bonne.animalpen.items.BirdCatcherItem;
import lv.id.bonne.animalpen.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;


public class AnimalPensItemRegistry
{
    public static void register()
    {
    }

    /**
     * The animal cage item
     */
    public static final Supplier<AnimalCageItem> ANIMAL_CAGE =
        Services.REGISTRY.registerItem(AnimalPen.resourceOf("animal_cage"),
            () -> new AnimalCageItem(new Item.Properties().
                stacksTo(1).
            setId(ResourceKey.create(Registries.ITEM,
                AnimalPen.resourceOf("animal_cage")))));

    /**
     * The animal container item
     */
    public static final Supplier<AnimalContainerItem> ANIMAL_CONTAINER =
        Services.REGISTRY.registerItem(AnimalPen.resourceOf("water_animal_container"),
            () -> new AnimalContainerItem(new Item.Properties().
                stacksTo(1).
            setId(ResourceKey.create(Registries.ITEM,
                AnimalPen.resourceOf("water_animal_container")))));


    public static final Supplier<BirdCatcherItem> BIRD_CATCHER =
        Services.REGISTRY.registerItem(AnimalPen.resourceOf("bird_catcher"),
            () -> new BirdCatcherItem(new Item.Properties().
                stacksTo(1).
            setId(ResourceKey.create(Registries.ITEM,
                AnimalPen.resourceOf("bird_catcher")))));
}
