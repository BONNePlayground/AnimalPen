//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.implementations;


import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import java.util.List;
import java.util.Map;

import dev.architectury.registry.registries.Registries;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


@Mixin(Wolf.class)
public abstract class AnimalPenWolf extends AnimalPenAnimal
{
    @Intrinsic(displace = false)
    public List<ItemStack> animalPen$getFood()
    {
        if (ANIMAL_PEN$FOOD_LIST == null)
        {
            ANIMAL_PEN$FOOD_LIST = Registries.get(AnimalPen.MOD_ID).
                get(Registry.ITEM_REGISTRY).entrySet().stream().
                map(Map.Entry::getValue).
                filter(Item::isEdible).
                filter(item -> item.getFoodProperties() != null).
                filter(item -> item.getFoodProperties().isMeat()).
                map(Item::getDefaultInstance).
                toList();
        }

        return ANIMAL_PEN$FOOD_LIST;
    }

    @Unique
    private static List<ItemStack> ANIMAL_PEN$FOOD_LIST;
}
