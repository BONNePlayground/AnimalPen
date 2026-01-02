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

import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;


/**
 * This mixin injects into method that controls player attacking on block. In situation when it attacks to the animal
 * pen or aquarium with attack tool it resets destroyBlockPos to default state. This will allow to hold mouse button and
 * continuously trigger block attack method.
 */
@Mixin(MultiPlayerGameMode.class)
public class InfinitePlayerAttack
{
    @Inject(method = "startDestroyBlock",
        at = @At(value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V"))
    private void performBlockAttack(BlockPos blockPos,
        Direction direction,
        CallbackInfoReturnable<Boolean> cir)
    {
        BlockState blockState = this.minecraft.level.getBlockState(this.destroyBlockPos);

        if (blockState.is(AnimalPenTags.ANIMAL_PEN_BLOCKS) &&
            this.destroyingItem.is(AnimalPenTags.ANIMAL_PEN_ATTACK_TOOLS) ||
            blockState.is(AnimalPenBlockRegistry.AQUARIUM.get()) &&
                this.destroyingItem.is(AnimalPenTags.AQUARIUM_ATTACK_TOOLS) ||
            blockState.is(AnimalPenBlockRegistry.AVIARY.get()) &&
                this.destroyingItem.is(AnimalPenTags.AVIARY_ATTACK_TOOLS))
        {
            this.destroyBlockPos = new BlockPos(-1, -1, -1);
        }
    }


    @Shadow
    private ItemStack destroyingItem;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private BlockPos destroyBlockPos;
}
