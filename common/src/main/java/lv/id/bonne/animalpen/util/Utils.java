//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.util;


import java.util.*;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


public class Utils
{
    public static List<ItemStack> mergeItemStacks(List<ItemStack> stacks)
    {
        Map<ItemStackKey, Integer> itemCounts = new HashMap<>();
        List<ItemStack> mergedStacks = new ArrayList<>();

        for (ItemStack stack : stacks)
        {
            if (stack.isEmpty())
            {
                continue;
            }

            ItemStackKey key = new ItemStackKey(stack);
            int totalAmount = itemCounts.getOrDefault(key, 0) + stack.getCount();
            itemCounts.put(key, totalAmount);
        }

        for (Map.Entry<ItemStackKey, Integer> entry : itemCounts.entrySet())
        {
            ItemStackKey key = entry.getKey();
            int totalAmount = entry.getValue();
            int maxStackSize = key.getMaxStackSize();

            while (totalAmount > 0)
            {
                int stackSize = Math.min(totalAmount, maxStackSize);
                ItemStack newStack = key.createStack(stackSize);
                mergedStacks.add(newStack);
                totalAmount -= stackSize;
            }
        }

        return mergedStacks;
    }


    public static class ItemStackKey
    {
        private final Item item;

        private final CompoundTag nbt;

        private final int maxStackSize;


        public ItemStackKey(ItemStack stack)
        {
            this.item = stack.getItem();
            this.nbt = stack.getTag() != null ? stack.getTag().copy() : null;
            this.maxStackSize = stack.getMaxStackSize();
        }


        public int getMaxStackSize()
        {
            return maxStackSize;
        }


        public ItemStack createStack(int count)
        {
            ItemStack stack = new ItemStack(item, count);
            if (nbt != null)
            {
                stack.setTag(nbt.copy());
            }
            return stack;
        }


        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }

            if (!(obj instanceof ItemStackKey other))
            {
                return false;
            }

            return this.item == other.item && Objects.equals(nbt, other.nbt);
        }


        @Override
        public int hashCode()
        {
            return Objects.hash(this.item, this.nbt);
        }
    }
}
