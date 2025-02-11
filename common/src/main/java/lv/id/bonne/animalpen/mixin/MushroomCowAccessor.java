//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.item.ItemStack;


@Mixin(MushroomCow.class)
public interface MushroomCowAccessor
{
    @Accessor("effect")
    public MobEffect getEffect();


    @Accessor("effect")
    public void setEffect(MobEffect effect);


    @Accessor("effectDuration")
    public int getEffectDuration();


    @Accessor("effectDuration")
    public void setEffectDuration(int effectDuration);

    @Invoker("getEffectFromItemStack")
    public Optional<Pair<MobEffect, Integer>> invokeGetEffectFromItemStack(ItemStack itemStack);
}