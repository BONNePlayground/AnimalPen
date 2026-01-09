//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.item.component.SuspiciousStewEffects;


@Mixin(MushroomCow.class)
public interface MushroomCowAccessor
{
    @Accessor
    void setStewEffects(SuspiciousStewEffects stewEffects);
}
