//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;


@Mixin(Mob.class)
public interface MobInvoker
{
    @Invoker("getAmbientSound")
    public SoundEvent invokeGetAmbientSound();
}
