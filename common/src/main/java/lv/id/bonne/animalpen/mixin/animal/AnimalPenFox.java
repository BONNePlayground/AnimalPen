//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.spongepowered.asm.mixin.*;
import java.util.List;
import java.util.Map;

import dev.architectury.registry.registries.Registries;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.Registry;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


@Mixin(Fox.class)
public abstract class AnimalPenFox extends AnimalPenAnimal
{
    protected AnimalPenFox(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        if (ANIMAL_PEN$FOOD_LIST == null)
        {
            ANIMAL_PEN$FOOD_LIST = Registries.get(AnimalPen.MOD_ID).
                get(Registry.ITEM_REGISTRY).entrySet().stream().
                map(Map.Entry::getValue).
                map(Item::getDefaultInstance).
                filter(stack -> stack.is(ItemTags.FOX_FOOD)).
                toList();
        }

        return ANIMAL_PEN$FOOD_LIST;
    }


    @Unique
    private static List<ItemStack> ANIMAL_PEN$FOOD_LIST;
}
