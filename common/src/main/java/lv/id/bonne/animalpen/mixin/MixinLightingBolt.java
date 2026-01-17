//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

import lv.id.bonne.animalpen.blocks.CopperAviaryBlock;
import lv.id.bonne.animalpen.blocks.WeatheringCopperAviary;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;


@Mixin(LightningBolt.class)
public class MixinLightingBolt
{
    @Redirect(method = "randomStepCleaningCopper",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/WeatheringCopper;getPrevious(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private static Optional<BlockState> aviaryCleaningStep(BlockState blockState)
    {
        Optional<BlockState> previous = WeatheringCopper.getPrevious(blockState);

        if (previous.isPresent())
        {
            return previous;
        }

        return WeatheringCopperAviary.getPrevious(blockState);
    }


    @Redirect(method = "clearCopperOnLightningStrike",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/WeatheringCopper;getFirst(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState replaceFirstState(BlockState blockState)
    {
        if (blockState.getBlock() instanceof CopperAviaryBlock)
        {
            return AnimalPenBlockRegistry.COPPER_AVIARIES.get(WeatheringCopper.WeatherState.UNAFFECTED).get().
                withPropertiesOf(blockState);
        }

        return WeatheringCopper.getFirst(blockState);
    }
}
