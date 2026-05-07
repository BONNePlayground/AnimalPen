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
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"))
    private boolean injected(BlockState instance, Object block)
    {
        return instance.is((Block) block) ||
            instance.is(AnimalPenTags.AVIARIES_BLOCKS) &&
                ((Block) block).builtInRegistryHolder().is(AnimalPenTags.AVIARIES_BLOCKS);
    }
}
