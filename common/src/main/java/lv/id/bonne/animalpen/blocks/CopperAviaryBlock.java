package lv.id.bonne.animalpen.blocks;


import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;


public class CopperAviaryBlock extends AviaryBlock implements WeatheringCopperAviary
{
    public CopperAviaryBlock(WeatheringCopper.WeatherState weatherState, Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
        this.weatherState = weatherState;
    }


    @Override
    @NotNull
    protected MapCodec<CopperAviaryBlock> codec()
    {
        return CODEC;
    }


// ---------------------------------------------------------------------
// Section: Weathering methods
// ---------------------------------------------------------------------


    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource random)
    {
        this.changeOverTime(blockState, serverLevel, blockPos, random);
    }


    @Override
    public boolean isRandomlyTicking(BlockState blockState)
    {
        return WeatheringCopperAviary.getNext(blockState.getBlock()).isPresent();
    }


    @Override
    @NotNull
    public WeatheringCopper.WeatherState getAge()
    {
        return this.weatherState;
    }


    private final WeatheringCopper.WeatherState weatherState;


    public static final MapCodec<CopperAviaryBlock> CODEC = RecordCodecBuilder.mapCodec(
        (instance) ->
            instance.group(WeatherState.CODEC.fieldOf("weathering_state").
                forGetter(CopperAviaryBlock::getAge), propertiesCodec()).
                apply(instance, CopperAviaryBlock::new));
}