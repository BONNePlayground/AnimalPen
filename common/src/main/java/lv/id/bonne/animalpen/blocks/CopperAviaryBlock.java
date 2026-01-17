package lv.id.bonne.animalpen.blocks;


import org.jetbrains.annotations.NotNull;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;


public class CopperAviaryBlock extends AviaryBlock implements WeatheringCopperAviary
{
    public CopperAviaryBlock(WeatheringCopper.WeatherState weatherState, Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
        this.weatherState = weatherState;
    }


// ---------------------------------------------------------------------
// Section: Weathering methods
// ---------------------------------------------------------------------


    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random)
    {
        this.onRandomTick(blockState, serverLevel, blockPos, random);
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
}