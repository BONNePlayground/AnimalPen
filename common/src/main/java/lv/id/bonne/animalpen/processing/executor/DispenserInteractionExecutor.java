package lv.id.bonne.animalpen.processing.executor;


import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.util.AnimalPenItemHelper;
import lv.id.bonne.animalpen.util.ItemTransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;


/**
 * Interaction made by dispenser
 */
public final class DispenserInteractionExecutor implements AnimalInteractionExecutor
{

    public DispenserInteractionExecutor(ServerLevel level,
        Container container,
        AbstractAnimalPenBlockEntity blockEntity)
    {
        this.level = level;
        this.container = container;
        this.blockEntity = blockEntity;
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
        int removed = amount;

        for (int i = 0; i < this.container.getContainerSize() && removed > 0; i++)
        {
            ItemStack stack = this.container.getItem(i);

            if (ItemStack.isSameItem(stack, consumedItem))
            {
                if (consumedItem.getMaxStackSize() == 1)
                {
                    this.container.setItem(i, AnimalPenItemHelper.replacement(stack));
                    removed--;
                }
                else
                {
                    if (stack.getCount() < removed)
                    {
                        removed -= stack.getCount();
                        stack = ItemStack.EMPTY;
                    }
                    else
                    {
                        stack.shrink(removed);
                        removed = 0;
                    }

                    this.container.setItem(i, stack);
                }
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
        if (item.hurt(amount, this.level.getRandom(), null))
        {
            item.shrink(1);
        }
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
        CompoundTag mobNBT,
        BlockPos blockPos)
    {
        return interaction.triggerFunctions(this.level,
            this.container,
            consumedItem,
            consumedAmount,
            animal,
            mobNBT,
            blockPos);
    }


    @Override
    public void triggerItemUse(Mob animal, ItemStack itemInHand, int consumedAmount)
    {
        // Do nothing. This is a player-only option
    }


    private final ServerLevel level;

    private final Container container;

    private final AbstractAnimalPenBlockEntity blockEntity;
}