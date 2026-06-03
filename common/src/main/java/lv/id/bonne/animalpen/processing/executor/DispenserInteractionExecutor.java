package lv.id.bonne.animalpen.processing.executor;


import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.util.AnimalPenItemHelper;
import lv.id.bonne.animalpen.util.ItemTransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * Interaction made by dispenser
 */
public final class DispenserInteractionExecutor implements AnimalInteractionExecutor
{

    public DispenserInteractionExecutor(ServerLevel level,
        Container container,
        int index,
        AbstractAnimalPenBlockEntity blockEntity)
    {
        this.level = level;
        this.container = container;
        this.blockEntity = blockEntity;
        this.index = index;
    }


    @Override
    public int countAvailable(ItemStack item)
    {
        int count = 0;

        for (int i = 0; i < this.container.getContainerSize(); i++)
        {
            ItemStack containerItem = this.container.getItem(i);

            if (ItemStack.isSameItem(containerItem, item))
            {
                count += containerItem.getCount();
            }
        }

        return count;
    }


    @Override
    public void consume(ItemStack consumedItem, int amount)
    {
        // Start with initial item index.
        int removed = this.removeAndReturnRemining(consumedItem,
            this.index,
            amount);

        for (int i = 0; i < this.container.getContainerSize() && removed > 0; i++)
        {
            if (i != this.index)
            {
                removed = this.removeAndReturnRemining(consumedItem, i, removed);
            }
        }
    }


    @Override
    public void drop(ItemStack item)
    {
        ItemTransferUtil.insertBellowOrDrop(this.level,
            item,
            this.blockEntity.getBlockPos(),
            this.blockEntity.dropPosition());
    }


    @Override
    public void damageItem(ItemStack item, int amount)
    {
        item.hurtAndBreak(amount, this.level, null, damaged -> {});
    }


    @Override
    public ItemStack giveFirst(ItemStack consumedItem, ItemStack lootItem)
    {
        if (consumedItem.getCount() == 1)
        {
            ItemStack result = AnimalPenItemHelper.replacement(consumedItem);

            if (result.isEmpty())
            {
                consumedItem.shrink(1);
                return lootItem;
            }
            else
            {
                consumedItem.shrink(1);
                this.drop(lootItem);
                return result;
            }
        }
        else
        {
            consumedItem.shrink(1);

            if (!consumedItem.isEmpty())
            {
                this.drop(lootItem);
                return consumedItem;
            }

            return lootItem;
        }
    }


    @Override
    public boolean triggerFunctions(AnimalInteraction interaction,
        ItemStack consumedItem,
        int consumedAmount,
        Mob animal,
        ItemStack componentHolder,
        BlockPos blockPos)
    {
        return interaction.triggerFunctions(this.level,
            this.container,
            this.index,
            consumedItem,
            consumedAmount,
            animal,
            componentHolder,
            blockPos);
    }


    @Override
    public void triggerItemUse(Mob animal, ItemStack itemInHand, int consumedAmount)
    {
        // Do nothing. This is a player-only option
    }


    /**
     * this method removes items from inventory till leftOver is 0.
     * @param consumedItem the consumed item that need to be removed.
     * @param index the index of item that is removed from inventory
     * @param leftOver the left-over amount of items to be removed
     * @return the remining items that still requires removing from inventory.
     */
    private int removeAndReturnRemining(ItemStack consumedItem, int index, int leftOver)
    {
        ItemStack stack = this.container.getItem(index);

        if (ItemStack.isSameItem(stack, consumedItem))
        {
            if (consumedItem.getMaxStackSize() == 1)
            {
                this.container.setItem(index, AnimalPenItemHelper.replacement(stack));
                leftOver--;
            }
            else
            {
                if (stack.getCount() < leftOver)
                {
                    leftOver -= stack.getCount();
                    stack = ItemStack.EMPTY;
                }
                else
                {
                    stack.shrink(leftOver);
                    leftOver = 0;
                }

                this.container.setItem(index, stack);
            }
        }

        return leftOver;
    }


    private final ServerLevel level;

    private final Container container;

    private final int index;

    private final AbstractAnimalPenBlockEntity blockEntity;
}