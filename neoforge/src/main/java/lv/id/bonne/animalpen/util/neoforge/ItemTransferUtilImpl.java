package lv.id.bonne.animalpen.util.neoforge;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;


public class ItemTransferUtilImpl
{
    public static boolean canInsert(Level level, BlockPos pos, Direction side, ItemStack stack)
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity == null)
        {
            return false;
        }

        IItemHandler capability = Capabilities.ItemHandler.BLOCK.getCapability(level,
            pos,
            blockEntity.getBlockState(),
            blockEntity,
            side);

        if (capability == null)
        {
            return false;
        }

        // Check if there's at least one slot that can accept items
        for (int i = 0; i < capability.getSlots(); i++)
        {
            ItemStack itemStack = capability.insertItem(i, stack, true);

            if (itemStack.isEmpty() || itemStack.getCount() != stack.getCount())
            {
                return true;
            }
        }

        return false;
    }


    public static ItemStack insert(Level level, BlockPos pos, Direction side, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity == null)
        {
            return stack;
        }

        IItemHandler capability = Capabilities.ItemHandler.BLOCK.getCapability(level,
            pos,
            blockEntity.getBlockState(),
            blockEntity,
            side);

        if (capability == null)
        {
            return stack;
        }

        ItemStack remaining = stack.copy();

        for (int i = 0; i < capability.getSlots() && !remaining.isEmpty(); i++)
        {
            remaining = capability.insertItem(i, remaining, false);
        }

        return remaining;
    }
}