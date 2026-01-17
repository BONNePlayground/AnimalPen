//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

import lv.id.bonne.animalpen.blocks.WeatheringCopperAviary;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


@Mixin(AxeItem.class)
public class MixinAxeItem
{
    @ModifyVariable(method = "useOn", at = @At(value = "STORE"), ordinal = 1)
    private Optional<BlockState> modifyOxidizedBlock(
        Optional<BlockState> originalBlockState,
        UseOnContext context)
    {
        if (originalBlockState.isPresent())
        {
            return originalBlockState;
        }

        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = world.getBlockState(blockPos);

        return WeatheringCopperAviary.getPrevious(blockState);
    }


    @ModifyVariable(method = "useOn", at = @At(value = "STORE"), ordinal = 2)
    private Optional<BlockState> modifyWaxedBlock(
        Optional<BlockState> originalBlockState,
        UseOnContext context)
    {
        if (originalBlockState.isPresent())
        {
            return originalBlockState;
        }

        Level world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = world.getBlockState(blockPos);

        return Optional.ofNullable(
            WeatheringCopperAviary.WAXED_TO_UNWAXED_BLOCKS.get().get(blockState.getBlock())).
            map(block -> block.withPropertiesOf(blockState));
    }
}
