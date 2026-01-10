//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.item.component.SuspiciousStewEffects;


@Mixin(MushroomCow.class)
public class MushroomStewEffectRemove
{
    @Shadow
    @Nullable
    private SuspiciousStewEffects stewEffects;


    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void removeEffectOnNull(CompoundTag compoundTag, CallbackInfo ci)
    {
        if (!compoundTag.contains("stew_effects", Tag.TAG_LIST))
        {
            // Remove existing effect.
            this.stewEffects = null;
        }
    }
}
