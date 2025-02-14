//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.Entity;


@Mixin(Entity.class)
public interface EntityAccessor
{
    @Accessor("wasTouchingWater")
    public void setWasTouchingWater(boolean value);
}
