//
// Created by BONNe
// Copyright - 2026
//

package lv.id.bonne.animalpen.interaction.ingredient;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Iterator;
import java.util.List;

import lv.id.bonne.animalpen.processing.executor.AnimalInteractionExecutor;
import net.minecraft.world.item.ItemStack;


public interface ConsumerEntry
{
    ItemStack getConsumedItem(ItemStack itemInHand);


    int calculateConsumption(AnimalInteractionExecutor executor,
        ItemStack itemStack,
        int animalCount,
        boolean evenCount);


    ItemStack consumeItems(AnimalInteractionExecutor executor,
        List<ItemStack> lootItems,
        ItemStack itemInHand,
        int consumedAmount);


    /**
     * Damage indicates that item should receive damage with given value.
     * @param damage The damage amount
     */
    public record Damage(int damage) implements ConsumerEntry
    {
        @Override
        public ItemStack getConsumedItem(ItemStack itemInHand)
        {
            return ItemStack.EMPTY;
        }


        public int calculateConsumption(AnimalInteractionExecutor executor,
            ItemStack itemStack,
            int animalCount,
            boolean evenCount)
        {
            return 1;
        }


        @Override
        public ItemStack consumeItems(AnimalInteractionExecutor executor,
            List<ItemStack> lootItems,
            ItemStack itemInHand,
            int consumedAmount)
        {
            executor.damageItem(itemInHand, this.damage);
            lootItems.forEach(executor::drop);
            return itemInHand;
        }


        public static final Codec<Damage> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("damage").forGetter(Damage::damage)
            ).apply(instance, Damage::new));
    }


    /**
     * Replace indicates that item will be replaced if possible.
     */
    public record Replace() implements ConsumerEntry
    {
        @Override
        public ItemStack getConsumedItem(ItemStack itemInHand)
        {
            return itemInHand.copy();
        }


        @Override
        public int calculateConsumption(AnimalInteractionExecutor executor,
            ItemStack itemStack,
            int animalCount,
            boolean evenCount)
        {
            return 1;
        }


        @Override
        public ItemStack consumeItems(AnimalInteractionExecutor executor,
            List<ItemStack> lootItems,
            ItemStack itemInHand,
            int consumedAmount)
        {
            if (lootItems.isEmpty())
            {
                executor.consume(itemInHand, 1);
                return itemInHand;
            }

            Iterator<ItemStack> iterator = lootItems.iterator();

            // Replace first item with consumed item
            itemInHand = executor.giveFirst(itemInHand, iterator.next());

            // Drop all remining items
            iterator.forEachRemaining(executor::drop);

            return itemInHand;
        }


        public static final Codec<Replace> CODEC = Codec.unit(new Replace());
    }


    /**
     * Consume means that applied item is consumed.
     * @param limitToStack true means that interaction is limited to hand item,
     *                     false means that it will consume all items of type in provided inventory.
     */
    public record Consume(boolean limitToStack) implements ConsumerEntry
    {
        @Override
        public ItemStack getConsumedItem(ItemStack itemInHand)
        {
            return itemInHand.copy();
        }


        @Override
        public int calculateConsumption(AnimalInteractionExecutor executor,
            ItemStack itemStack,
            int animalCount,
            boolean evenCount)
        {
            int consumedAmount;

            if (!this.limitToStack)
            {
                consumedAmount = executor.countAvailable(itemStack);
            }
            else
            {
                consumedAmount = itemStack.getCount();
            }

            consumedAmount = Math.min(animalCount, consumedAmount);

            if (evenCount && (consumedAmount & 1) == 1)
            {
                consumedAmount--;
            }

            return consumedAmount;
        }


        @Override
        public ItemStack consumeItems(AnimalInteractionExecutor executor,
            List<ItemStack> lootItems,
            ItemStack itemInHand,
            int consumedAmount)
        {
            executor.consume(itemInHand, consumedAmount);
            lootItems.forEach(executor::drop);

            return ItemStack.EMPTY;
        }


        public static final Codec<Consume> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("limit_to_stack").forGetter(Consume::limitToStack)
            ).apply(instance, Consume::new));
    }


    /**
     * Interact means that applied is not consumed or damaged. Just an interaction item.
     */
    public record Interact() implements ConsumerEntry
    {
        @Override
        public ItemStack getConsumedItem(ItemStack itemInHand)
        {
            return ItemStack.EMPTY;
        }


        @Override
        public int calculateConsumption(AnimalInteractionExecutor executor,
            ItemStack itemStack,
            int animalCount,
            boolean evenCount)
        {
            return 1;
        }


        @Override
        public ItemStack consumeItems(AnimalInteractionExecutor executor,
            List<ItemStack> lootItems,
            ItemStack itemInHand,
            int consumedAmount)
        {
            lootItems.forEach(executor::drop);
            return itemInHand;
        }


        public static final Codec<Interact> CODEC = Codec.unit(new Interact());
    }


    Codec<ConsumerEntry> CODEC = Codec.STRING.
        dispatch("type",
            entry ->
            {
                if (entry instanceof Damage)
                {
                    return "damage";
                }
                else if (entry instanceof Replace)
                {
                    return "replace";
                }
                else if (entry instanceof Interact)
                {
                    return "interact";
                }
                else if (entry instanceof Consume)
                {
                    return "consume";
                }
                else
                {
                    // This should never happen
                    return entry.toString();
                }
            },
            type -> switch (type)
            {
                case "damage" -> Damage.CODEC;
                case "interact" -> Interact.CODEC;
                case "consume" -> Consume.CODEC;
                case "replace" -> Replace.CODEC;
                default -> Codec.EMPTY.codec().flatXmap(
                    empty -> DataResult.error("Unknown cooldown type: " + type),
                    entry -> DataResult.error("Unknown cooldown type: " + type)
                );
            });
}
