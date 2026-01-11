//
// Created by BONNe
// Copyright - 2025
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
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


/**
 * Either I rewrite everything to be an Interaction event, or just make that animal cage should not trigger interaction
 * with chested horses.
 */
@Mixin(AbstractChestedHorse.class)
public abstract class FixChestedHorseInteraction extends AbstractHorse
{
    protected FixChestedHorseInteraction(EntityType<? extends AbstractHorse> entityType, Level level)
    {
        super(entityType, level);
    }


    @Inject(method = "mobInteract",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/equine/AbstractChestedHorse;isTamed()Z",
            ordinal = 1),
        cancellable = true)
    private void addAnimalCageException(Player player,
        InteractionHand interactionHand,
        CallbackInfoReturnable<InteractionResult> cir)
    {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        if (AnimalPenVariantHelper.customInteraction(this.getType(), itemStack))
        {
            cir.setReturnValue(InteractionResult.PASS);
            cir.cancel();
        }
    }
}
