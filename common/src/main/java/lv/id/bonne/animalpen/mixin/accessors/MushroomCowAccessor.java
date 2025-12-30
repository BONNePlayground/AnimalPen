//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.animal.MushroomCow;


@Mixin(MushroomCow.class)
public interface MushroomCowAccessor
{
    @Accessor
    void setEffect(MobEffect effect);


    @Accessor
    void setEffectDuration(int effectDuration);
}
