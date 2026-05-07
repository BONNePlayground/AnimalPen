package lv.id.bonne.animalpen.platform.neoforge;


import lv.id.bonne.animalpen.platform.services.IItemTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;


public class ItemTransferHelperImpl implements IItemTransferHelper
{
    @Override
    public boolean canInsert(Level level, BlockPos pos, Direction side, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return false;
        }

        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);

        if (handler == null)
        {
            return false;
        }

        ItemResource resource = ItemResource.of(stack);

        try (Transaction tx = Transaction.open(null))
        {
            long inserted = handler.insert(resource, stack.getCount(), tx);
            return inserted > 0;
        }
    }


    @Override
    public ItemStack insert(Level level, BlockPos pos, Direction side, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return stack;
        }

        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, pos, side);

        if (handler == null)
        {
            return stack;
        }


        ItemResource resource = ItemResource.of(stack);

        try (Transaction tx = Transaction.open(null))
        {
            int inserted = handler.insert(resource, stack.getCount(), tx);
            tx.commit();
            stack.shrink(inserted);

            return stack;
        }
    }
}