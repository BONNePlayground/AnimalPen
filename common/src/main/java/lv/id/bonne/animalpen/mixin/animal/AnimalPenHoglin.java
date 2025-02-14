//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


@Mixin(Hoglin.class)
public abstract class AnimalPenHoglin extends AnimalPenAnimal
{
    @Intrinsic(displace = false)
    public List<ItemStack> animalPen$getFood()
    {
        return Collections.singletonList(Items.CRIMSON_FUNGUS.getDefaultInstance());
    }
}
