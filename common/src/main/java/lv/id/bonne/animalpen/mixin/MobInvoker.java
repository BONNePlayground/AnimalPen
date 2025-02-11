//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.Map;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;


@Mixin(Mob.class)
public interface MobInvoker
{
    @Invoker("getAmbientSound")
    public SoundEvent invokeGetAmbientSound();
}
