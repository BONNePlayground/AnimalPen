package lv.id.bonne.animalpen.mixin.neoforge;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import lv.id.bonne.animalpen.blocks.WeatheringCopperAviary;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.extensions.IBlockExtension;


@Mixin(IBlockExtension.class)
public interface MixinIBlockExtension
{
    @Inject(method = "getToolModifiedState", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
    private void modifyOxidizedBlock(BlockState state,
        UseOnContext context,
        ItemAbility toolAction,
        boolean simulate,
        CallbackInfoReturnable<BlockState> cir)
    {
        if (cir.getReturnValue() != null)
        {
            return;
        }

        cir.setReturnValue(WeatheringCopperAviary.getPrevious(state).orElse(null));
    }



    @Inject(method = "getToolModifiedState", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private void modifyWaxedBlock(BlockState state,
        UseOnContext context,
        ItemAbility toolAction,
        boolean simulate,
        CallbackInfoReturnable<BlockState> cir)
    {
        if (cir.getReturnValue() != null)
        {
            return;
        }

        Optional<BlockState> optional =
            Optional.ofNullable(WeatheringCopperAviary.WAXED_TO_UNWAXED_BLOCKS.get().get(state.getBlock())).
                map(block -> block.withPropertiesOf(state));

        cir.setReturnValue(optional.orElse(null));
    }
}
