//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AquariumBlock;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import lv.id.bonne.animalpen.items.AnimalContainerItem;
import lv.id.bonne.animalpen.mixin.accessors.WaterAnimalInvoker;
import lv.id.bonne.animalpen.network.packets.UpdateVariantScreenData;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;


public class AquariumTileEntity extends BlockEntity implements AnimalPenBlockInterface<WaterAnimal>
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
        tag.putLong(TAG_DISPLAY_SIZE, this.displaySize);
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

        if (tag.contains(TAG_DISPLAY_SIZE, Tag.TAG_LONG))
        {
            this.displaySize = tag.getLong(TAG_DISPLAY_SIZE);
        }
        else
        {
            this.displaySize = -1;
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
    @Override
    public WaterAnimal getStoredAnimal()
    {
        if (this.storedAnimal == null && !this.getItemStack().isEmpty())
        {
            CompoundTag tag = this.getItemStack().getOrCreateTag();

            if (!tag.contains(AnimalContainerItem.TAG_ENTITY_ID) || this.level == null)
            {
                return this.storedAnimal;
            }

            EntityType.create(tag, this.level).map(entity -> (WaterAnimal) entity).
                ifPresent(animal -> this.storedAnimal = animal);
        }
        else if (this.storedAnimal != null && this.getItemStack().isEmpty())
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

                    if (this.level != null && !this.level.isClientSide())
                    {
                        // Trigger screen Update
                        NetworkManager.sendToPlayers(((ServerLevel) this.level).players().stream().
                                filter(other ->
                                    other.distanceToSqr(this.getBlockPos().getX(),
                                        this.getBlockPos().getY(),
                                        this.getBlockPos().getZ()) < 50).
                                toList(),
                            UpdateVariantScreenData.ID,
                            UpdateVariantScreenData.encode(this.getBlockPos()));
                    }
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

                // Handle animal variants
                if (newCount > 1 &&
                    !AnimalContainerItem.canMergeAnimalVariants(this.getItemStack(), itemInHand, player))
                {
                    ((AnimalPenInterface) animal).animalPenUpdateCount(-1);
                    itemInHandTag.putLong(AnimalContainerItem.TAG_AMOUNT, 1);
                    itemInHand.setTag(itemInHandTag);
                }
                else
                {
                    AnimalContainerItem.mergeAnimalVariants(this.getItemStack(), itemInHand, player);
                    itemInHand.setTag(new CompoundTag());

                    if (this.level != null && !this.level.isClientSide())
                    {
                        // Trigger screen Update
                        NetworkManager.sendToPlayers(((ServerLevel) this.level).players().stream().
                                filter(other ->
                                    other.distanceToSqr(this.getBlockPos().getX(),
                                        this.getBlockPos().getY(),
                                        this.getBlockPos().getZ()) < 50).
                                toList(),
                            UpdateVariantScreenData.ID,
                            UpdateVariantScreenData.encode(this.getBlockPos()));
                    }
                }

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
                ItemStack item = this.getItemStack();
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
            ItemStack item = this.getItemStack();

            Optional<ListTag> optionalVariants = AnimalContainerItem.getAnimalVariants(item);

            // Reset tag, as some animals may need it.
            CompoundTag tag = new CompoundTag();
            animal.save(tag);

            // Restore animal variants
            optionalVariants.ifPresent(variants -> tag.put(AnimalContainerItem.TAG_VARIANTS, variants));

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

        WaterAnimal animal = this.getStoredAnimal();

        if (animal == null)
        {
            return;
        }

        if (!((AnimalPenInterface) animal).animalPenUpdateCount(-1))
        {
            return;
        }

        weapon.hurtAndBreak(1, player, (playerx) -> playerx.broadcastBreakEvent(InteractionHand.MAIN_HAND));

        this.deathTicker.add(0);

        if (((AnimalPenInterface) animal).animalPenGetCount() <= 0)
        {
            ItemStack item = this.getItemStack();
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
            Block.popResource(level, this.getBlockPos().offset(0.5, 1.5, 0.5), itemStack));

        int reward = ((WaterAnimalInvoker) animal).invokeGetExperienceReward(player);
        ExperienceOrb.award((ServerLevel)this.level, position.add(0.5, 1.5, 0.5), reward);
    }


    /**
     * This method returns redstone signal based on current signal that should be sent out by animal.
     * @return the redstone signal value based on bit value:
     *    - 1 - is animal
     *    - 2 - can feed
     *    - 4 - can interact 1
     *    - 8 - can interact 2
     */
    public int getRedStoneSignal()
    {
        if (this.getStoredAnimal() == null)
        {
            return 0;
        }

        return ((AnimalPenInterface) this.storedAnimal).getRedStoneSignal();
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
            animal.save(this.getItemStack().getOrCreateTag());
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
     * This method returns item stack of currently stored cage.
     * @return The currently stored cage.
     */
    private ItemStack getItemStack()
    {
        return this.inventory.getItem(0);
    }


// ---------------------------------------------------------------------
// Section: Animal Pen Block Interface
// ---------------------------------------------------------------------


    /**
     * Returns the list of entity variants stored in cage.
     * @return List of entity variants in cage.
     */
    @Override
    public ListTag getEntityVariants()
    {
        if (this.getStoredAnimal() == null)
        {
            return new ListTag();
        }

        return AnimalContainerItem.getAnimalVariants(this.getItemStack()).orElseGet(ListTag::new);
    }


    /**
     * This method returns animal display size.
     * @return The display size of animal.
     */
    @Override
    public long getAnimalDisplaySize()
    {
        return this.displaySize < 1 ? this.getAnimalCount() :
            Math.min(this.displaySize, this.getAnimalCount());
    }


    /**
     * This method changes animal display size.
     * @param size The animal display size.
     */
    @Override
    public void setAnimalDisplaySize(long size)
    {
        this.displaySize = size;
        this.triggerUpdate();
    }


    /**
     * This method sets new animal variant from given CompoundTag tag.
     * @param animalVariant a new animal variant
     */
    @Override
    public void updateAnimalVariant(CompoundTag animalVariant)
    {
        if (this.getStoredAnimal() == null || animalVariant == null || animalVariant.isEmpty())
        {
            return;
        }

        // Save extra data
        CompoundTag extraData = new CompoundTag();
        ((AnimalPenInterface) this.storedAnimal).animalPenSaveTag(extraData);

        // load new variant
        this.storedAnimal.load(animalVariant);

        // Apply data
        ((AnimalPenInterface) this.storedAnimal).animalPenLoadTag(extraData);
        this.triggerUpdate();

        if (this.level != null && !this.level.isClientSide())
        {
            NetworkManager.sendToPlayers(((ServerLevel) this.level).players().stream().
                    filter(player ->
                        player.distanceToSqr(this.getBlockPos().getX(),
                            this.getBlockPos().getY(),
                            this.getBlockPos().getZ()) < 50).
                    toList(),
                UpdateVariantScreenData.ID,
                UpdateVariantScreenData.encode(this.getBlockPos()));
        }
    }


    /**
     * This method removes animal pen variant with given index.
     * @param index the variant index to be removed
     */
    @Override
    public void removeAnimalVariant(int index)
    {
        if (this.getStoredAnimal() == null || this.getEntityVariants().size() <= index)
        {
            return;
        }

        this.getEntityVariants().remove(index);
        this.inventory.setChanged();

        if (this.level != null && !this.level.isClientSide())
        {
            NetworkManager.sendToPlayers(((ServerLevel) this.level).players().stream().
                    filter(player ->
                        player.distanceToSqr(this.getBlockPos().getX(),
                            this.getBlockPos().getY(),
                            this.getBlockPos().getZ()) < 50).
                    toList(),
                UpdateVariantScreenData.ID,
                UpdateVariantScreenData.encode(this.getBlockPos()));
        }
    }


    /**
     * This method returns the count of animals in pen.
     * @return The animal count in pen.
     */
    @Override
    public long getAnimalCount()
    {
        return ((AnimalPenInterface) this.getStoredAnimal()).animalPenGetCount();
    }


    @Override
    public boolean canGrowEntity()
    {
        return AnimalPen.CONFIG_MANAGER.getConfiguration().isGrowWaterAnimals();
    }


    /**
     * This method returns the description lines that will be displayed above tile entity.
     * @param shortText Indicates if text should be short or long version
     * @return List of pairs that contains display icon and text next to it
     */
    @Override
    public List<Pair<ItemStack[], Component>> getCooldownLines(boolean shortText)
    {
        if (this.getStoredAnimal() == null)
        {
            return Collections.emptyList();
        }

        return ((AnimalPenInterface) this.storedAnimal).animalPenGetLines(this.getTickCounter(), shortText);
    }

    
// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    
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

    private long displaySize = -1;

    private int tickCounter;

    private final List<Integer> deathTicker = new ArrayList<>();

    public static final String TAG_INVENTORY = "inventory";

    public static final String TAG_DEATH_TICKER = "death_ticker";

    public static final String TAG_DISPLAY_SIZE = "display_size";
}
