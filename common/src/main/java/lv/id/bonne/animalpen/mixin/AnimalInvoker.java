//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;


@Mixin(Animal.class)
public interface AnimalInvoker
{
    @Invoker("getExperienceReward")
    public int invokeGetExperienceReward(Player player);
}
