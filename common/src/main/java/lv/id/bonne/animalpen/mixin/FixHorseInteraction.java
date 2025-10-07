//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


/**
 * Either I rewrite everything to be an Interaction event, or just make that animal cage should not
 * trigger interaction with chested horses.
 */
@Mixin(Horse.class)
public class FixHorseInteraction
{
    @Inject(method = "mobInteract",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/horse/Horse;isTamed()Z",
            ordinal = 1),
        cancellable = true)
    private void addAnimalCageException(Player player,
        InteractionHand interactionHand,
        CallbackInfoReturnable<InteractionResult> cir)
    {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        if (itemStack.is(AnimalPensItemRegistry.ANIMAL_CAGE.get()))
        {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
