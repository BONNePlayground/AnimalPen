//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import java.util.List;

import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


@Mixin(Rabbit.class)
public abstract class AnimalPenRabbit extends AnimalPenAnimal
{
    @Intrinsic(displace = false)
    public List<ItemStack> animalPen$getFood()
    {
        return List.of(Items.CARROT.getDefaultInstance(),
            Items.GOLDEN_CARROT.getDefaultInstance(),
            Items.DANDELION.getDefaultInstance());
    }
}
