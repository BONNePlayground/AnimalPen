//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.wateranimal;


import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;


@Mixin(AbstractFish.class)
public abstract class AnimalPenAbstractFish extends AnimalPenWaterAnimal
{
    protected AnimalPenAbstractFish(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Unique
    public boolean animal$isFood(ItemStack itemStack)
    {
        return itemStack.is(Items.SEAGRASS) || itemStack.is(Items.KELP);
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        return List.of(Items.SEAGRASS.getDefaultInstance(),
            Items.KELP.getDefaultInstance());
    }
}
