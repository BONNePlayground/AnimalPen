//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.SuspiciousEffectHolder;


@Mixin(MushroomCow.class)
public interface MushroomCowAccessor
{
    @Accessor
    List<SuspiciousEffectHolder.EffectEntry> getStewEffects();


    @Accessor
    void setStewEffects(List<SuspiciousEffectHolder.EffectEntry> stewEffects);
}
