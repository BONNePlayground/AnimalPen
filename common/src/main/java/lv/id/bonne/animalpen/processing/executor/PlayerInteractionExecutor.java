package lv.id.bonne.animalpen.processing.executor;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import lv.id.bonne.animalpen.util.AnimalPenItemHelper;
import lv.id.bonne.animalpen.util.ItemTransferUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;


/**
 * Interaction made by player
 */
public final class PlayerInteractionExecutor implements AnimalInteractionExecutor
{

    public PlayerInteractionExecutor(ServerPlayer player,
        InteractionHand hand,
        AbstractAnimalPenBlockEntity blockEntity)
    {
        this.player = player;
        this.hand = hand;
        this.blockEntity = blockEntity;
    }


    @Override
    public int countAvailable(ItemStack item)
    {
        int count = 0;

        for (ItemStack containerItem : this.player.getInventory().items)
        {
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
        if (this.player.getAbilities().instabuild || amount <= 0)
        {
            return;
        }

        int removed = amount;

        if (ItemStack.isSameItem(this.player.getItemInHand(this.hand), consumedItem))
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

            if (ItemStack.isSameItem(stack, consumedItem))
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
        ItemTransferUtil.insertBellowOrDrop(this.player.serverLevel(),
            item,
            this.blockEntity.getBlockPos(),
            this.blockEntity.dropPosition());
    }


    @Override
    public void damageItem(ItemStack item, int amount)
    {
        item.hurtAndBreak(amount, this.player, this.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }


    @Override
    public boolean triggerFunctions(AnimalInteraction interaction,
        ItemStack consumedItem,
        int consumedAmount,
        Mob animal,
        ItemStack componentHolder,
        BlockPos blockPos)
    {
        return interaction.triggerFunctions(this.player,
            this.hand,
            consumedItem,
            consumedAmount,
            animal,
            componentHolder,
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

        // Animal Pen specific advancements.
        AnimalPenCriteriaTriggersRegistry.ANIMAL_INTERACT_TRIGGER.get().trigger(this.player,
            animal,
            itemStack);
    }


    private final ServerPlayer player;

    private final InteractionHand hand;

    private final AbstractAnimalPenBlockEntity blockEntity;
}