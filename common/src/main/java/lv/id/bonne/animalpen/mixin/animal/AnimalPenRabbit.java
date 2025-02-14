//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import java.util.List;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;


@Mixin(Rabbit.class)
public abstract class AnimalPenRabbit extends AnimalPenAnimal
{
    protected AnimalPenRabbit(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        return List.of(Items.CARROT.getDefaultInstance(),
            Items.GOLDEN_CARROT.getDefaultInstance(),
            Items.DANDELION.getDefaultInstance());
    }
}
