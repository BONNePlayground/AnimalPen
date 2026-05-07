package lv.id.bonne.animalpen.platform.services;


import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;


public interface IItemTransferHelper
{
    /**
     * Checks whether items can be inserted into the block at the given position from the given side.
     */
    boolean canInsert(Level level, BlockPos pos, Direction side, ItemStack stack);


    /**
     * Attempts to insert the given ItemStack.
     *
     * @return the remaining stack (empty if fully inserted)
     */
    ItemStack insert(Level level, BlockPos pos, Direction side, ItemStack stack);


    /**
     * This method tries to insert item in inventory bellow it, and if fails, drops it on ground.
     * @param level The level where it happens
     * @param stack The item stack that need to be processed
     * @param blockPos The block position from where it needs to calculate inventory bellow
     * @param dropPos The block position where remining items will be dropped
     */
    default void insertBellowOrDrop(@NotNull Level level,
        ItemStack stack,
        BlockPos blockPos,
        BlockPos dropPos)
    {
        if (level.isClientSide())
        {
            // Cannot insert
            return;
        }

        BlockPos below = blockPos.below();

        if (this.canInsert(level, below, Direction.UP, stack))
        {
            ItemStack remaining = this.insert(level, below, Direction.UP, stack);

            if (!remaining.isEmpty())
            {
                Block.popResource(level, dropPos, remaining);
            }
        }
        else
        {
            Block.popResource(level, dropPos, stack);
        }
    }
}