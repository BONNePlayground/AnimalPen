package lv.id.bonne.animalpen.util.forge;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;


public class ItemTransferUtilImpl
{
    public static boolean canInsert(Level level, BlockPos pos, Direction side, ItemStack stack)
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity == null)
        {
            return false;
        }

        LazyOptional<IItemHandler> capability = blockEntity.getCapability(
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);

        return capability.map(handler ->
        {
            // Check if there's at least one slot that can accept items
            for (int i = 0; i < handler.getSlots(); i++)
            {
                ItemStack itemStack = handler.insertItem(i, stack, true);

                if (itemStack.isEmpty() || itemStack.getCount() != stack.getCount())
                {
                    return true;
                }
            }

            return false;
        }).orElse(false);
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

        LazyOptional<IItemHandler> capability = blockEntity.getCapability(
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);

        return capability.map(handler ->
        {
            ItemStack remaining = stack.copy();

            for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++)
            {
                remaining = handler.insertItem(i, remaining, false);
            }

            return remaining;
        }).orElse(stack);
    }
}