//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;


public class AnimalPensCreativeTabRegistry
{
    public static void register()
    {
        REGISTRY.register();
    }

    public static final DeferredRegister<CreativeModeTab> REGISTRY =
        DeferredRegister.create(AnimalPen.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> ANIMAL_PEN_TAB = REGISTRY.register("animal_pen", () ->
        CreativeTabRegistry.create(Component.translatable("category.animal_pen.animal_pen"),
            () -> new ItemStack(AnimalPensItemRegistry.ANIMAL_CAGE.get())));
}
