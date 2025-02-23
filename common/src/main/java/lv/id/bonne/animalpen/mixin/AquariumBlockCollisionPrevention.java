package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;


/**
 * This mixin prevents other blocks to be placed above aquarium block.
 */
@Mixin(BlockBehaviour.class)
public abstract class AquariumBlockCollisionPrevention
{
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void preventBlockPlacementAbove(BlockState state,
        LevelReader world,
        BlockPos pos,
        CallbackInfoReturnable<Boolean> cir)
    {
        BlockPos belowPos = pos.below();
        BlockState belowState = world.getBlockState(belowPos);

        if (belowState.is(AnimalPenBlockRegistry.AQUARIUM.get()))
        {
            cir.setReturnValue(false);
        }
    }
}