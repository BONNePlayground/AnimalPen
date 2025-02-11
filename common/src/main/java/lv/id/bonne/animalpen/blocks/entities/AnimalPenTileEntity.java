//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class AnimalPenTileEntity extends BlockEntity
{
    public AnimalPenTileEntity(
        BlockPos blockPos,
        BlockState blockState)
    {
        super(AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get(), blockPos, blockState);
    }


    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        CompoundTag cooldownTag = new CompoundTag();

        for (Map.Entry<String, Integer> entry : this.cooldowns.entrySet())
        {
            cooldownTag.putInt(entry.getKey(), entry.getValue());
        }

        tag.put(TAG_COOLDOWNS, cooldownTag);
    }


    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        if (tag.contains(TAG_COOLDOWNS, Tag.TAG_COMPOUND))
        {
            CompoundTag cooldownTag = tag.getCompound(TAG_COOLDOWNS);
            for (String key : cooldownTag.getAllKeys())
            {
                this.cooldowns.put(key, cooldownTag.getInt(key));
            }
        }
    }


    public void tick()
    {
        if (this.getLevel() == null || this.getLevel().isClientSide())
        {
            return;
        }

        this.tickCounter++;

        boolean updated = false;
        Iterator<Map.Entry<String, Integer>> iterator = this.cooldowns.entrySet().iterator();

        while (iterator.hasNext())
        {
            Map.Entry<String, Integer> entry = iterator.next();
            if (entry.getValue() > 0)
            {
                this.cooldowns.put(entry.getKey(), entry.getValue() - 1);
                updated = true;
            }
            else
            {
                // Remove expired cooldowns
                iterator.remove();
                updated = true;
            }
        }

        if (updated)
        {
            // Save the changes
            this.setChanged();
        }
    }


    public class AnimalPenInventory extends SimpleContainer
    {
        public AnimalPenInventory()
        {
            super(1);
        }


        @Override
        public boolean canPlaceItem(int slot, ItemStack stack)
        {
            return stack.is(AnimalPensItemRegistry.ANIMAL_CONTAINER.get());
        }


        @Override
        public void setChanged()
        {
            super.setChanged();
            AnimalPenTileEntity.this.setChanged();
        }
    }


    private int tickCounter;

    private final Map<String, Integer> cooldowns = new HashMap<>();

    public static final String TAG_COOLDOWNS = "Cooldowns";
}
