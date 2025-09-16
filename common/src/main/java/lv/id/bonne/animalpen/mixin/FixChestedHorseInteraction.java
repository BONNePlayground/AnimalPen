//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


/**
 * Either I rewrite everything to be an Interaction event, or just make that animal cage should not
 * trigger interaction with chested horses.
 */
@Mixin(AbstractChestedHorse.class)
public class FixChestedHorseInteraction
{
    @Inject(method = "mobInteract",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/horse/AbstractChestedHorse;isTamed()Z",
            ordinal = 1),
        cancellable = true)
    private void addAnimalCageException(Player player,
        InteractionHand interactionHand,
        CallbackInfoReturnable<InteractionResult> cir,
        @Local ItemStack itemStack)
    {
        if (itemStack.is(AnimalPensItemRegistry.ANIMAL_CAGE.get()))
        {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
