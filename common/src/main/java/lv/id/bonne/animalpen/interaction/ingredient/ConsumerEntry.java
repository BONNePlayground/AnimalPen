//
// Created by BONNe
// Copyright - 2026
//

package lv.id.bonne.animalpen.interaction.ingredient;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import java.util.Iterator;
import java.util.List;

import lv.id.bonne.animalpen.processing.executor.AnimalInteractionExecutor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;


public sealed interface ConsumerEntry permits
    ConsumerEntry.Damage,
    ConsumerEntry.Replace,
    ConsumerEntry.Consume,
    ConsumerEntry.Interact
{
    ItemStack getConsumedItem(ItemStack itemInHand);


    long calculateConsumption(AnimalInteractionExecutor executor,
        ItemStack itemStack,
        long animalCount,
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


        public long calculateConsumption(AnimalInteractionExecutor executor,
            ItemStack itemStack,
            long animalCount,
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

        public static final MapCodec<Damage> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
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
        public long calculateConsumption(AnimalInteractionExecutor executor,
            ItemStack itemStack,
            long animalCount,
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


        public static final MapCodec<Replace> CODEC = MapCodec.unit(new Replace());
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
        public long calculateConsumption(AnimalInteractionExecutor executor,
            ItemStack itemStack,
            long animalCount,
            boolean evenCount)
        {
            long consumedAmount;

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

            return (int) Math.min(consumedAmount, Integer.MAX_VALUE);
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


        public static final MapCodec<Consume> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
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
        public long calculateConsumption(AnimalInteractionExecutor executor,
            ItemStack itemStack,
            long animalCount,
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


        public static final MapCodec<Interact> CODEC = MapCodec.unit(new Interact());
    }


    enum ConsumerType implements StringRepresentable
    {
        DAMAGE("damage"),
        REPLACE("replace"),
        INTERACT("interact"),
        CONSUME("consume");

        ConsumerType(String name)
        {
            this.name = name;
        }


        @Override
        @NotNull
        public String getSerializedName()
        {
            return this.name;
        }


        public static final Codec<ConsumerType> CODEC =
            StringRepresentable.fromEnum(ConsumerType::values);

        private final String name;
    }


    public static final Codec<ConsumerEntry> CODEC =
        ConsumerType.CODEC.dispatch(
            "type",
            entry -> switch (entry) {
                case Damage d -> ConsumerType.DAMAGE;
                case Replace r -> ConsumerType.REPLACE;
                case Interact i -> ConsumerType.INTERACT;
                case Consume c -> ConsumerType.CONSUME;
            },
            type -> switch (type) {
                case DAMAGE -> Damage.CODEC;
                case REPLACE -> Replace.CODEC;
                case INTERACT -> Interact.CODEC;
                case CONSUME -> Consume.CODEC;
            }
        );
}
