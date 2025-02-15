//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.spongepowered.asm.mixin.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import dev.architectury.registry.registries.Registries;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.Registry;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;


@Mixin(Frog.class)
public abstract class AnimalPenFrog extends AnimalPenAnimal
{
    @Shadow @Final
    public static Ingredient TEMPTATION_ITEM;


    protected AnimalPenFrog(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        return Arrays.stream(TEMPTATION_ITEM.getItems()).toList();
    }
}
