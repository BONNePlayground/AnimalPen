//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lv.id.bonne.animalpen.util.AnimalPenVariantHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


@Mixin(Allay.class)
public abstract class FixAllayPickup extends LivingEntity
{
    protected FixAllayPickup(EntityType<? extends LivingEntity> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void preventPickingCatcher(Player player,
        InteractionHand interactionHand,
        CallbackInfoReturnable<InteractionResult> cir)
    {
        ItemStack itemInHand = player.getItemInHand(interactionHand);

        if (AnimalPenVariantHelper.customInteraction(this.getType(), itemInHand))
        {
            cir.setReturnValue(InteractionResult.PASS);
            cir.cancel();
        }
    }
}
