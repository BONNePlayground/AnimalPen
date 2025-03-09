//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.AquariumBlock;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;


/**
 * This mixin injects into method that controls player attacking on block.
 * In situation when it attacks to the animal pen or aquarium with attack tool it resets destroyBlockPos
 * to default state.
 * This will allow to hold mouse button and continuously trigger block attack method.
 */
@Mixin(MultiPlayerGameMode.class)
public class InfinitePlayerAttack
{
    @Shadow
    private ItemStack destroyingItem;

    @Shadow
    @Final
    private Minecraft minecraft;


    @Shadow
    private BlockPos destroyBlockPos;


    @Inject(method = "startDestroyBlock",
        at = @At(value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V"))
    private void performBlockAttack(BlockPos blockPos,
        Direction direction,
        CallbackInfoReturnable<Boolean> cir)
    {
        BlockState blockState = this.minecraft.level.getBlockState(this.destroyBlockPos);

        if (blockState.is(AnimalPenBlock.ANIMAL_PENS) && this.destroyingItem.is(AnimalPenBlock.ATTACK_TOOLS) ||
            blockState.is(AnimalPenBlockRegistry.AQUARIUM.get()) && this.destroyingItem.is(AquariumBlock.ATTACK_TOOLS))
        {
            this.destroyBlockPos = new BlockPos(-1, -1, -1);
        }
    }
}
