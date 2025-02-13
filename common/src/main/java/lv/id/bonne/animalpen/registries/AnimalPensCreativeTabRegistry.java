//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import dev.architectury.registry.CreativeTabRegistry;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;


public class AnimalPensCreativeTabRegistry
{
    public static void register()
    {
    }

    public static final CreativeModeTab ANIMAL_PEN_TAB = CreativeTabRegistry.create(
        new ResourceLocation(AnimalPen.MOD_ID, "animal_pen"),
        () -> new ItemStack(AnimalPensItemRegistry.ANIMAL_CAGE.get())
    );
}
