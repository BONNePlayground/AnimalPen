//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import java.util.Arrays;
import java.util.List;

import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;


@Mixin(Llama.class)
public abstract class AnimalPenLlama extends AnimalPenAnimal
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
