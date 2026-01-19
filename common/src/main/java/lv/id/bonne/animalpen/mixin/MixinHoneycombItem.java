//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import lv.id.bonne.animalpen.blocks.WeatheringCopperAviary;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.state.BlockState;


@Mixin(HoneycombItem.class)
public class MixinHoneycombItem
{
    @Inject(method = "getWaxed", at = @At("RETURN"), cancellable = true)
    private static void replaceWaxedAviary(BlockState blockState, CallbackInfoReturnable<Optional<BlockState>> cir)
    {
        if (cir.getReturnValue().isPresent())
        {
            return;
        }

        cir.setReturnValue(Optional.ofNullable(WeatheringCopperAviary.UNWAXED_TO_WAXED_BLOCKS.get().
                get(blockState.getBlock())).
            map(block -> block.withPropertiesOf(blockState)));
    }
}
