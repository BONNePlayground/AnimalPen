//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import java.util.function.Supplier;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;


public class AnimalPensCreativeTabRegistry
{
    public static void register()
    {
    }


    public static final Supplier<CreativeModeTab> ANIMAL_PEN_TAB =
        Services.REGISTRY.registerCreativeTab(AnimalPen.resourceOf("animal_pen"), () ->
            Services.PLATFORM.createCreativeTab(Component.translatable("category.animal_pen.items"),
                () -> new ItemStack(AnimalPensItemRegistry.ANIMAL_CAGE.get()),
                Services.REGISTRY.getRegisterItems()
            ));
}
