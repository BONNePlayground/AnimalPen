//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.implementations;


import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;
import java.util.List;

import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;


@Mixin(Strider.class)
public abstract class AnimalPenStrider extends AnimalPenAnimal
{
    @Shadow
    @Final
    private static Ingredient FOOD_ITEMS;


    @Intrinsic(displace = false)
    public List<ItemStack> animalPen$getFood()
    {
        return Arrays.stream(FOOD_ITEMS.getItems()).toList();
    }
}
