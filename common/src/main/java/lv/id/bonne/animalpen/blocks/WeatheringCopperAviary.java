//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.blocks;


import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;
import java.util.function.Supplier;

import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;


public interface WeatheringCopperAviary extends WeatheringCopper
{
    public static final Supplier<BiMap<Block, Block>> UNWAXED_TO_WAXED_BLOCKS =
        Suppliers.memoize(() ->
        {
            ImmutableBiMap.Builder<Block, Block> builder = ImmutableBiMap.builder();

            for (WeatherState value : WeatherState.values())
            {
                builder.put(AnimalPenBlockRegistry.COPPER_AVIARIES.get(value).get(),
                    AnimalPenBlockRegistry.WAXED_COPPER_AVIARIES.get(value).get());
            }

            return builder.build();
        });

    public static final Supplier<BiMap<Block, Block>> WAXED_TO_UNWAXED_BLOCKS =
        Suppliers.memoize(() -> UNWAXED_TO_WAXED_BLOCKS.get().inverse());


    static Optional<Block> getPrevious(Block block)
    {
        if (block instanceof CopperAviaryBlock copperAviaryBlock)
        {
            int ordinal = copperAviaryBlock.getAge().ordinal();

            if (ordinal > 0)
            {
                return Optional.ofNullable(AnimalPenBlockRegistry.COPPER_AVIARIES.get(WeatherState.values()[ordinal -
                    1]).get());
            }
        }

        return Optional.empty();
    }


    static Optional<BlockState> getPrevious(BlockState blockState)
    {
        return getPrevious(blockState.getBlock()).map((block) -> block.withPropertiesOf(blockState));
    }


    @NotNull
    static Optional<Block> getNext(Block block)
    {
        if (block instanceof CopperAviaryBlock copperAviaryBlock)
        {
            int ordinal = copperAviaryBlock.getAge().ordinal();

            if (ordinal + 1 < WeatherState.values().length)
            {
                return Optional.ofNullable(AnimalPenBlockRegistry.COPPER_AVIARIES.get(WeatherState.values()[ordinal +
                    1]).get());
            }
        }

        return Optional.empty();
    }


    @Override
    @NotNull
    default Optional<BlockState> getNext(@NotNull BlockState blockState)
    {
        return getNext(blockState.getBlock()).map((block) -> block.withPropertiesOf(blockState));
    }
}
