//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

import lv.id.bonne.animalpen.blocks.AquariumBlock;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import lv.id.bonne.animalpen.items.AnimalContainerItem;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;


public class AquariumTileEntity extends BlockEntity
{
    public AquariumTileEntity(
        BlockPos blockPos,
        BlockState blockState)
    {
        super(AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get(), blockPos, blockState);
    }


    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);

        tag.put(TAG_INVENTORY, this.inventory.createTag());
        tag.put(TAG_DEATH_TICKER, new IntArrayTag(this.deathTicker));
    }


    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);

        this.inventory.clearContent();
        this.deathTicker.clear();
        this.storedAnimal = null;

        if (tag.contains(TAG_INVENTORY, Tag.TAG_LIST))
        {
            this.inventory.fromTag(tag.getList(TAG_INVENTORY, Tag.TAG_COMPOUND));
        }

        if (tag.contains(TAG_DEATH_TICKER, Tag.TAG_INT_ARRAY))
        {
            int[] intArray = tag.getIntArray(TAG_DEATH_TICKER);

            for (int i : intArray)
            {
                this.deathTicker.add(i);
            }
        }
    }


    /**
     * This method updates NBT tag.
     *
     * @return Method that updates NBT tag.
     */
    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithoutMetadata();
    }


    /**
     * This method updates table content to client.
     *
     * @return Packet that is sent to client
     */
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    /**
     * This method returns stored animal for block entity.
     *
     * @return Animal instance stored in block entity.
     */
    public WaterAnimal getStoredAnimal()
    {
        if (this.storedAnimal == null && !this.inventory.getItem(0).isEmpty())
        {
            CompoundTag tag = this.inventory.getItem(0).getOrCreateTag();

            if (!tag.contains(AnimalContainerItem.TAG_ENTITY_ID) || this.level == null)
            {
                return this.storedAnimal;
            }

            EntityType.create(tag, this.level).map(entity -> (WaterAnimal) entity).
                ifPresent(animal -> this.storedAnimal = animal);
        }
        else if (this.storedAnimal != null && this.inventory.getItem(0).isEmpty())
        {
            this.storedAnimal = null;
        }

        return this.storedAnimal;
    }


    public List<Integer> getDeathTicker()
    {
        return this.deathTicker;
    }


    public int getTickCounter()
    {
        return this.tickCounter;
    }


    public void tick()
    {
        this.tickCounter++;

        if (this.getLevel() == null || this.getLevel().isClientSide())
        {
            return;
        }

        boolean updated = false;

        WaterAnimal animal = this.getStoredAnimal();

        if (animal != null && ((AnimalPenInterface) animal).animalPenTick(this))
        {
            updated = true;
        }

        for (int i = 0; i < this.deathTicker.size(); i++)
        {
            this.deathTicker.set(i, this.deathTicker.get(i) + 1);
            updated = true;
        }

        this.deathTicker.removeIf(integer -> integer > 20);

        if (updated)
        {
            // Save the changes
            this.triggerUpdate();
        }
    }


    /**
     * This method processes player interaction with pen tile entity with animal container item in main hand.
     *
     * @param player the player
     * @param interactionHand the interaction hand
     * @return {@code true} if interaction was successful, {@code false} otherwise.
     */
    public boolean processContainer(Player player, InteractionHand interactionHand)
    {
        if (this.inventory.isEmpty())
        {
            ItemStack itemInHand = player.getItemInHand(interactionHand);

            if (!itemInHand.getOrCreateTag().contains(AnimalContainerItem.TAG_ENTITY_ID))
            {
                return false;
            }
            else
            {
                if (!player.getLevel().isClientSide())
                {
                    this.inventory.addItem(itemInHand);
                    player.setItemInHand(interactionHand, ItemStack.EMPTY);
                }

                return true;
            }
        }
        else
        {
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            CompoundTag itemInHandTag = itemInHand.getOrCreateTag();

            if (!itemInHandTag.contains(AnimalContainerItem.TAG_ENTITY_ID))
            {
                if (!player.isCrouching())
                {
                    // Empty... nothing to do.
                    return false;
                }

                if (player.getLevel().isClientSide())
                {
                    // Next only on server.
                    return true;
                }

                WaterAnimal animal = this.getStoredAnimal();
                
                if (animal == null)
                {
                    // Animal is not loaded.
                    return false;
                }

                long currentCount = ((AnimalPenInterface) animal).animalPenGetCount();

                if (currentCount < 2)
                {
                    // Cannot split 1 or 0
                    return false;
                }

                long newCount = currentCount / 2;

                if (!((AnimalPenInterface) animal).animalPenUpdateCount(-newCount))
                {
                    return false;
                }

                animal.save(itemInHandTag);
                itemInHandTag.putLong(AnimalContainerItem.TAG_AMOUNT, newCount);

                player.setItemInHand(interactionHand, itemInHand);
                this.inventory.setChanged();

                // Remove half of animals.
                return true;
            }
            else
            {
                WaterAnimal animal = this.getStoredAnimal();

                if (animal == null ||
                    !itemInHandTag.getString(AnimalContainerItem.TAG_ENTITY_ID).
                        equals(animal.getType().arch$registryName().toString()))
                {
                    // Cannot do with different animal types.
                    return false;
                }

                if (player.getLevel().isClientSide())
                {
                    // Next only on server.
                    return true;
                }

                long newCount = itemInHandTag.getLong(AnimalContainerItem.TAG_AMOUNT);

                if (newCount <= 0 || !((AnimalPenInterface) animal).animalPenUpdateCount(newCount))
                {
                    return false;
                }

                // clear tag.
                itemInHand.setTag(new CompoundTag());
                player.setItemInHand(interactionHand, itemInHand);

                this.inventory.setChanged();

                return true;
            }
        }
    }


    /**
     * This method processes player interaction with pen tile entity with any item in mian hand.
     * @param player the player
     * @param interactionHand the interaction hand
     * @return {@code true} if interaction was successful, {@code false} otherwise.
     */
    public boolean interactWithPen(Player player, InteractionHand interactionHand)
    {
        ItemStack itemInHand = player.getItemInHand(interactionHand);

        if (itemInHand.isEmpty() && !this.inventory.isEmpty())
        {
            if (player.isCrouching() && !player.getLevel().isClientSide())
            {
                ItemStack item = this.inventory.getItem(0);
                player.setItemInHand(interactionHand, item);
                this.inventory.setItem(0, ItemStack.EMPTY);
                this.inventory.setChanged();
            }

            return true;
        }

        WaterAnimal animal = this.getStoredAnimal();

        if (animal == null)
        {
            return false;
        }

        if (((AnimalPenInterface) animal).animalPenInteract(player, interactionHand, this.getBlockPos()))
        {
            ItemStack item = this.inventory.getItem(0);

            // Reset tag, as some animals may need it.
            CompoundTag tag = new CompoundTag();
            animal.save(tag);
            item.setTag(tag);

            this.inventory.setChanged();

            // Trigger update.
            return true;
        }

        return false;
    }


    /**
     * Allows to attach entity in the animal pen. Generates loot that would be for killing it.
     *
     * @param player the player
     * @param level the level
     */
    public void attackThePen(Player player, Level level)
    {
        ItemStack weapon = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (!(weapon.getItem() instanceof SwordItem) && !(weapon.getItem() instanceof AxeItem))
        {
            // not a weapon in hand.
            return;
        }

        WaterAnimal animal = this.getStoredAnimal();

        if (animal == null)
        {
            return;
        }

        if (!((AnimalPenInterface) animal).animalPenUpdateCount(-1))
        {
            return;
        }

        this.deathTicker.add(0);

        if (((AnimalPenInterface) animal).animalPenGetCount() <= 0)
        {
            ItemStack item = this.inventory.getItem(0);
            item.setTag(new CompoundTag());

            Block.popResource(level, this.getBlockPos().above(), item);
            this.inventory.setItem(0, ItemStack.EMPTY);
        }

        this.triggerUpdate();

        Vec3 position = new Vec3(this.worldPosition.getX(),
            this.worldPosition.getY(),
            this.worldPosition.getZ());

        LootTable lootTable = level.getServer().getLootTables().get(animal.getLootTable());

        LootContext.Builder contextBuilder = new LootContext.Builder((ServerLevel) level).
            withParameter(LootContextParams.ORIGIN, position).
            withParameter(LootContextParams.THIS_ENTITY, animal).
            withParameter(LootContextParams.KILLER_ENTITY, player).
            withParameter(LootContextParams.DIRECT_KILLER_ENTITY, player).
            withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).
            withParameter(LootContextParams.DAMAGE_SOURCE, DamageSource.playerAttack(player)).
            withLuck(player.getLuck()).
            withRandom(level.random);

        lootTable.getRandomItems(contextBuilder.create(LootContextParamSets.ENTITY)).forEach(itemStack ->
            Block.popResource(level, this.getBlockPos().above(), itemStack));

        int reward = animal.getExperienceReward();
        ExperienceOrb.award((ServerLevel)this.level, position, reward);
    }


    private void triggerUpdate()
    {
        this.setChanged();

        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        WaterAnimal animal = this.getStoredAnimal();

        if (animal != null)
        {
            animal.save(this.inventory.getItem(0).getOrCreateTag());
        }

        BlockState oldState = this.getBlockState();
        BlockState newState = this.getBlockState();

        if (oldState.getValue(AquariumBlock.FILLED) == this.inventory.isEmpty())
        {
            newState = oldState.setValue(AquariumBlock.FILLED, !this.inventory.isEmpty());
            this.level.setBlock(this.getBlockPos(), newState, Block.UPDATE_CLIENTS);
            this.setChanged();
        }

        this.level.sendBlockUpdated(this.getBlockPos(),
            oldState,
            newState,
            Block.UPDATE_CLIENTS);
    }


    /**
     * Returns inventory of this tile entity
     * @return inventory
     */
    public SimpleContainer getInventory()
    {
        return this.inventory;
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
            AquariumTileEntity.this.triggerUpdate();
        }
    };


    private WaterAnimal storedAnimal;

    private int tickCounter;

    private final List<Integer> deathTicker = new ArrayList<>();

    public static final String TAG_INVENTORY = "inventory";

    public static final String TAG_DEATH_TICKER = "death_ticker";
}
