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

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;


@Mixin(Pig.class)
public abstract class AnimalPenPig extends AnimalPenAnimal
{
    protected AnimalPenPig(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Shadow
    @Final
    private static Ingredient FOOD_ITEMS;


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        return Arrays.stream(FOOD_ITEMS.getItems()).toList();
    }
}
