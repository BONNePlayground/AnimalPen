//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import java.util.List;
import java.util.Map;

import dev.architectury.registry.registries.RegistrarManager;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


@Mixin(Wolf.class)
public abstract class AnimalPenWolf extends AnimalPenAnimal
{
    protected AnimalPenWolf(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        if (ANIMAL_PEN$FOOD_LIST == null)
        {
            ANIMAL_PEN$FOOD_LIST = RegistrarManager.get(AnimalPen.MOD_ID).
                get(Registries.ITEM).entrySet().stream().
                map(Map.Entry::getValue).
                map(Item::getDefaultInstance).
                filter(stack -> stack.is(ItemTags.WOLF_FOOD)).
                toList();
        }

        return ANIMAL_PEN$FOOD_LIST;
    }


    @Unique
    private static List<ItemStack> ANIMAL_PEN$FOOD_LIST;
}
