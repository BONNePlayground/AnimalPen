//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import com.google.common.collect.BiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

import lv.id.bonne.animalpen.blocks.WeatheringCopperAviary;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;


@Mixin(AxeItem.class)
public class MixinAxeItem
{
    @Redirect(method = "evaluateNewBlockState",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/WeatheringCopper;getPrevious(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private Optional<BlockState> modifyOxidizedBlock(BlockState blockState)
    {
        Optional<BlockState> previous = WeatheringCopper.getPrevious(blockState);

        if (previous.isPresent())
        {
            return previous;
        }

        return WeatheringCopperAviary.getPrevious(blockState);
    }


    @Redirect(method = "evaluateNewBlockState",
        at = @At(value = "INVOKE",
            target = "Lcom/google/common/collect/BiMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object modifyWaxedBlock(BiMap instance, Object key)
    {
        Object original = instance.get(key);

        if (original != null)
        {
            return original;
        }

        if (key instanceof Block block)
        {
            return WeatheringCopperAviary.WAXED_TO_UNWAXED_BLOCKS.get().get(block);
        }

        return null;
    }
}
