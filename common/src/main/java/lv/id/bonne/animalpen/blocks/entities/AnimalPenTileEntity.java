//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import lv.id.bonne.animalpen.items.AnimalContainerItem;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
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
        tag.put(TAG_INVENTORY, this.inventory.createTag());
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

        if (tag.contains(TAG_INVENTORY, Tag.TAG_LIST))
        {
            this.inventory.fromTag(tag.getList(TAG_INVENTORY, Tag.TAG_COMPOUND));
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


    /**
     * This method processes player interaction with pen tile entity with animal container item in main hand.
     *
     * @param player the player
     * @param interactionHand the interaction hand
     * @param itemInHand the item in hand
     * @return {@code true} if interaction was successful, {@code false} otherwise.
     */
    public boolean processContainer(Player player, InteractionHand interactionHand, ItemStack itemInHand)
    {
        if (this.inventory.isEmpty())
        {
            Optional<Animal> storedAnimal = AnimalContainerItem.getStoredAnimal(this.level, itemInHand);

            if (storedAnimal.isEmpty())
            {
                return false;
            }
            else
            {
                this.inventory.addItem(itemInHand);
                this.storedAnimal = storedAnimal.get();
                player.setItemInHand(interactionHand, ItemStack.EMPTY);

                return true;
            }
        }
        else
        {
            if (this.storedAnimal == null)
            {
                // Read stored animal from the inventory.
                AnimalContainerItem.getStoredAnimal(this.level, this.inventory.getItem(0)).
                    ifPresent(animal -> this.storedAnimal = animal);
            }

            Optional<Animal> storedAnimal = AnimalContainerItem.getStoredAnimal(this.level, itemInHand);

            if (storedAnimal.isEmpty())
            {
                if (!player.isCrouching())
                {
                    // Empty... nothing to do.
                    return false;
                }

                long currentCount = AnimalContainerItem.getStoredAnimalAmount(this.inventory.getItem(0));

                if (currentCount < 2)
                {
                    // Cannot split 1 or 0
                    return false;
                }

                long newCount = currentCount / 2;

                AnimalContainerItem.setStoredAnimal(this.inventory.getItem(0), this.storedAnimal, currentCount - newCount);
                this.inventory.setChanged();
                AnimalContainerItem.setStoredAnimal(itemInHand, this.storedAnimal, newCount);
                player.setItemInHand(interactionHand, itemInHand);

                // Remove half of animals.
                return true;
            }
            else
            {
                if (this.storedAnimal.getType() != storedAnimal.get().getType())
                {
                    // Cannot do with different animal types.
                    return false;
                }

                long count = AnimalContainerItem.getStoredAnimalAmount(this.inventory.getItem(0));
                long newCount = AnimalContainerItem.getStoredAnimalAmount(itemInHand);

                AnimalContainerItem.setStoredAnimal(this.inventory.getItem(0), this.storedAnimal, count + newCount);
                this.inventory.setChanged();
                // clear tag.
                itemInHand.setTag(new CompoundTag());
                player.setItemInHand(interactionHand, itemInHand);

                return true;
            }
        }
    }


    /**
     * This method processes player interaction with pen tile entity with any item in mian hand.
     * @param player the player
     * @param interactionHand the interaction hand
     * @param itemInHand the item in hand
     * @return {@code true} if interaction was successful, {@code false} otherwise.
     */
    public boolean interactWithPen(Player player, InteractionHand interactionHand, ItemStack itemInHand)
    {
        if (itemInHand.isEmpty() && !this.inventory.isEmpty())
        {
            player.setItemInHand(interactionHand, this.inventory.getItem(0));
            this.inventory.setItem(0, ItemStack.EMPTY);
            return true;
        }

        return false;
    }


    /**
     * The inventory of container.
     */
    private final SimpleContainer inventory = new SimpleContainer(1)
    {
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
    };


    private Animal storedAnimal;

    private int tickCounter;

    private final Map<String, Integer> cooldowns = new HashMap<>();

    public static final String TAG_COOLDOWNS = "Cooldowns";

    public static final String TAG_INVENTORY = "Inventory";
}
