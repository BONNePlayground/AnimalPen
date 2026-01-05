package lv.id.bonne.animalpen.util.fabric;


import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public class ItemTransferUtilImpl
{
    public static boolean canInsert(Level level, BlockPos pos, Direction side, ItemStack stack)
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof Container)
        {
            return true;
        }

        // Check Fabric Transfer API
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);

        if (storage == null)
        {
            return false;
        }

        try (Transaction transaction = Transaction.openOuter())
        {
            long inserted = StorageUtil.simulateInsert(storage, ItemVariant.of(stack), stack.getCount(), transaction);
            return inserted > 0;
        }
    }


    public static ItemStack insert(Level level, BlockPos pos, Direction side, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        // Handle vanilla containers
        if (blockEntity instanceof Container container)
        {
            return insertIntoVanillaContainer(container, stack);
        }

        // Handle Fabric Transfer API
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);

        if (storage == null)
        {
            return stack;
        }

        ItemVariant variant = ItemVariant.of(stack);
        ItemStack remaining = stack.copy();

        try (Transaction transaction = Transaction.openOuter())
        {
            long inserted = StorageUtil.tryInsertStacking(storage, variant, remaining.getCount(), transaction);

            if (inserted > 0)
            {
                remaining.shrink((int) inserted);
                transaction.commit();
            }
        }

        return remaining;
    }


    private static ItemStack insertIntoVanillaContainer(Container container, ItemStack stack)
    {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < container.getContainerSize() && !remaining.isEmpty(); i++)
        {
            ItemStack slotStack = container.getItem(i);

            if (slotStack.isEmpty())
            {
                container.setItem(i, remaining.copy());
                remaining = ItemStack.EMPTY;
            }
            else if (ItemStack.isSameItemSameTags(slotStack, remaining))
            {
                int maxStackSize = Math.min(container.getMaxStackSize(), slotStack.getMaxStackSize());
                int canInsert = maxStackSize - slotStack.getCount();
                int toInsert = Math.min(canInsert, remaining.getCount());

                if (toInsert > 0)
                {
                    slotStack.grow(toInsert);
                    remaining.shrink(toInsert);
                }
            }
        }

        container.setChanged();
        return remaining;
    }
}