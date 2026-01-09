//
// Created by BONNe
// Copyright - 2025
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

        if (itemStack.is(AnimalPensItemRegistry.ANIMAL_CAGE.get()) &&
            this.getType().is(AnimalPenTags.ANIMAL_CAGE_PICKABLE) ||
            itemStack.is(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()) &&
                this.getType().is(AnimalPenTags.WATER_MOB_CONTAINER_PICKABLE) ||
            itemStack.is(AnimalPensItemRegistry.BIRD_CATCHER.get()) &&
                this.getType().is(AnimalPenTags.BIRD_CATCHER_PICKABLE))
        {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
