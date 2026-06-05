//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;


/**
 * This mixin is injecting custom behaviour before dispenser checks dispense method.
 * I was not planning to add it, however, I found out some mods overwrite already registered dispense
 * too behaviour without using it as backup when their one does nothing.
 *
 * But there is also benefit of this, as now I have slot index which allows to properly handle items
 * form dispensers, and allows all interactions to work with dispenser, not only ones I registered.
 */
@Mixin(DispenserBlock.class)
public class DispenserMixin
{
    @Inject(method = "dispenseFrom", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/DispenserBlock;getDispenseMethod(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/core/dispenser/DispenseItemBehavior;"),
        cancellable = true)
    private void animalPen$customDispenseFromAction(ServerLevel serverLevel,
        BlockState blockState,
        BlockPos blockPos,
        CallbackInfo ci,
        @Local DispenserBlockEntity dispenserBlockEntity,
        @Local int i,
        @Local ItemStack itemStack)
    {
        if (AnimalPen.config().isBlockDispenserInteractions())
        {
            return;
        }

        BlockPos targetedPos = blockPos.relative(blockState.getValue(DispenserBlock.FACING));
        BlockState targetState = serverLevel.getBlockState(targetedPos);

        if (!targetState.is(AnimalPenTags.ANIMAL_PEN_BLOCKS) &&
            !targetState.is(AnimalPenBlockRegistry.AQUARIUM.get()) &&
            !targetState.is(AnimalPenTags.AVIARIES_BLOCKS))
        {
            // If not animal pen/aquarium then return to original output
            return;
        }

        if (serverLevel.getBlockEntity(targetedPos) instanceof AbstractAnimalPenBlockEntity ani)
        {
            AbstractAnimalPenBlockEntity.InteractionResult result =
                ani.interactWithPen(serverLevel, dispenserBlockEntity, i, itemStack);
            ani.triggerUpdate();

            serverLevel.levelEvent(LevelEvent.PARTICLES_SHOOT_SMOKE,
                blockPos,
                dispenserBlockEntity.getBlockState().getValue(DispenserBlock.FACING).get3DDataValue());

            if (result.success())
            {
                serverLevel.levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, blockPos, 0);
            }
            else
            {
                serverLevel.levelEvent(LevelEvent.SOUND_DISPENSER_FAIL, blockPos, 0);
            }

            if (ci != null && ci.isCancellable())
            {
                ci.cancel();
            }
        }
    }
}
