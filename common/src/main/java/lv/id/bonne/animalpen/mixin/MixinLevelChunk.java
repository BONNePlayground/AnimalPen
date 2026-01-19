//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;


@Mixin(LevelChunk.class)
public class MixinLevelChunk
{
    @Redirect(method = "setBlockState",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean injected(BlockState instance, Block block)
    {
        return instance.is(block) ||
            instance.is(AnimalPenTags.AVIARIES_BLOCKS) &&
                block.builtInRegistryHolder().is(AnimalPenTags.AVIARIES_BLOCKS);
    }
}
