package lv.id.bonne.animalpen.processing.executor;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.util.AnimalPenItemHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;


/**
 * Interaction made by player
 */
public final class PlayerInteractionExecutor implements AnimalInteractionExecutor
{

    public PlayerInteractionExecutor(ServerPlayer player,
        InteractionHand hand)
    {
        this.player = player;
        this.hand = hand;
    }


    @Override
    public int countAvailable(ItemStack item)
    {
        if (item.getMaxStackSize() == 1)
        {
            int count = 0;

            for (ItemStack stack : this.player.getInventory().items)
            {
                if (ItemStack.isSame(stack, item))
                {
                    count++;
                }
            }

            return count;
        }

        return item.getCount();
    }


    @Override
    public void consume(ItemStack consumedItem, int amount)
    {
        if (this.player.getAbilities().instabuild || amount <= 0)
        {
            return;
        }

        int removed = amount;

        if (ItemStack.isSame(this.player.getItemInHand(this.hand), consumedItem))
        {
            if (consumedItem.getMaxStackSize() == 1)
            {
                this.player.setItemInHand(this.hand, AnimalPenItemHelper.replacement(consumedItem));
                removed--;
            }
            else
            {
                ItemStack itemInHand = this.player.getItemInHand(this.hand);

                if (itemInHand.getCount() < removed)
                {
                    removed -= itemInHand.getCount();
                    itemInHand = ItemStack.EMPTY;
                }
                else
                {
                    itemInHand.shrink(removed);
                    removed = 0;
                }

                this.player.setItemInHand(this.hand, itemInHand);
            }
        }

        for (int i = 0; i < this.player.getInventory().getContainerSize() && removed > 0; i++)
        {
            ItemStack stack = this.player.getInventory().getItem(i);

            if (ItemStack.isSame(stack, consumedItem))
            {
                if (consumedItem.getMaxStackSize() == 1)
                {
                    this.player.getInventory().setItem(i, AnimalPenItemHelper.replacement(stack));
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

                    this.player.getInventory().setItem(i, stack);
                }
            }
        }
    }


    @Override
    public void give(ItemStack item)
    {
        if (!this.player.getInventory().add(item))
        {
            this.player.drop(item, false);
        }
    }


    @Override
    public ItemStack giveFirst(ItemStack consumedItem, ItemStack lootItem)
    {
        ItemStack remainingStack = ItemUtils.createFilledResult(consumedItem,
            this.player,
            lootItem);
        this.player.setItemInHand(this.hand, remainingStack);
        return remainingStack;
    }


    @Override
    public void drop(ItemStack item)
    {
        player.drop(item, false);
    }


    @Override
    public void damageItem(ItemStack item, int amount)
    {
        item.hurtAndBreak(amount, this.player,
            p -> p.broadcastBreakEvent(this.hand));
    }


    @Override
    public boolean triggerFunctions(AnimalInteraction interaction,
        ItemStack consumedItem,
        int consumedAmount,
        Mob animal,
        CompoundTag mobNBT,
        BlockPos blockPos)
    {
        return interaction.triggerFunctions(this.player,
            this.hand,
            consumedItem,
            consumedAmount,
            animal,
            mobNBT,
            blockPos);
    }


    @Override
    public void triggerItemUse(Mob animal, ItemStack itemStack, int amount)
    {
        if (AnimalPen.config().isTriggerAdvancements())
        {
            CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(this.player,
                itemStack,
                animal);
        }

        if (AnimalPen.config().isIncreaseStatistics())
        {
            this.player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()), amount);
        }
    }


    private final ServerPlayer player;

    private final InteractionHand hand;
}