package lv.id.bonne.animalpen.util;


import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public final class ItemTransferUtil
{
    private ItemTransferUtil()
    {
    }


    /**
     * Checks whether items can be inserted into the block at the given position from the given side.
     */
    @ExpectPlatform
    public static boolean canInsert(Level level, BlockPos pos, Direction side, ItemStack stack)
    {
        throw new AssertionError();
    }


    /**
     * Attempts to insert the given ItemStack.
     *
     * @return the remaining stack (empty if fully inserted)
     */
    @ExpectPlatform
    public static ItemStack insert(Level level, BlockPos pos, Direction side, ItemStack stack)
    {
        throw new AssertionError();
    }
}