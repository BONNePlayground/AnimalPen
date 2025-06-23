//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.neoforge;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;


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
    private BlockPos destroyBlockPos;


    @Inject(method = "lambda$startDestroyBlock$1",
        at = @At(value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V"))
    private void performBlockAttack(BlockState blockState,
        PlayerInteractEvent.LeftClickBlock event,
        BlockPos arg,
        Direction arg2,
        int i,
        CallbackInfoReturnable<Packet> cir)
    {
        if (blockState.is(AnimalPenTags.ANIMAL_PEN_BLOCKS) && this.destroyingItem.is(AnimalPenTags.ANIMAL_PEN_ATTACK_TOOLS) ||
            blockState.is(AnimalPenBlockRegistry.AQUARIUM.get()) && this.destroyingItem.is(AnimalPenTags.AQUARIUM_ATTACK_TOOLS))
        {
            this.destroyBlockPos = new BlockPos(-1, -1, -1);
        }
    }
}
