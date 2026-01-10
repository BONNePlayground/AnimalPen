//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


@Mixin(Allay.class)
public class FixAllayPickup
{
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void preventPickingCatcher(Player player,
        InteractionHand interactionHand,
        CallbackInfoReturnable<InteractionResult> cir)
    {
        ItemStack itemInHand = player.getItemInHand(interactionHand);

        if (itemInHand.is(AnimalPensItemRegistry.ANIMAL_CAGE.get()) &&
            ((Allay) (Object) this).getType().is(AnimalPenTags.ANIMAL_CAGE_PICKABLE) ||
            itemInHand.is(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()) &&
                ((Allay) (Object) this).getType().is(AnimalPenTags.WATER_MOB_CONTAINER_PICKABLE) ||
            itemInHand.is(AnimalPensItemRegistry.BIRD_CATCHER.get()) &&
                ((Allay) (Object) this).getType().is(AnimalPenTags.BIRD_CATCHER_PICKABLE))
        {
            cir.setReturnValue(InteractionResult.PASS);
            cir.cancel();
        }
    }
}
