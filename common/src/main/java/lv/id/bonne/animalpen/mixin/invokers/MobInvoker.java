//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.invokers;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;


@Mixin(Mob.class)
public interface MobInvoker
{
    @Invoker
    float callGetEquipmentDropChance(EquipmentSlot equipmentSlot);

    @Invoker("getExperienceReward")
    public int invokeGetExperienceReward(Player player);
}
