//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import lv.id.bonne.animalpen.items.AnimalCageItem;
import lv.id.bonne.animalpen.network.packets.UpdateVariantScreenData;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;


public class AnimalPenTileEntity extends BlockEntity implements AnimalPenBlockInterface<Animal>
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

        tag.put(TAG_INVENTORY, this.inventory.createTag());
        tag.put(TAG_DEATH_TICKER, new IntArrayTag(this.deathTicker));
        tag.putLong(TAG_DISPLAY_SIZE, this.displaySize);

        this.getOwner().ifPresent(owner -> tag.putUUID(TAG_OWNER_UUID, owner));
        tag.putLong(TAG_KEEP_AMOUNT, this.protectedAmount);
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

        if (tag.contains(TAG_OWNER_UUID))
        {
            this.ownerUUID = tag.getUUID(TAG_OWNER_UUID);
        }

        this.protectedAmount = tag.getLong(TAG_KEEP_AMOUNT);
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
    public Optional<Animal> getStoredAnimal()
    {
        if (this.storedAnimal == null && !this.getItemStack().isEmpty())
        {
            CompoundTag tag = this.getItemStack().getOrCreateTag();

            if (!tag.contains(AnimalCageItem.TAG_ENTITY_ID) || this.level == null)
            {
                return Optional.ofNullable(this.storedAnimal);
            }

            EntityType.create(tag, this.level).map(entity -> (Animal) entity).
                ifPresent(animal -> this.storedAnimal = animal);
        }
        else if (this.storedAnimal != null && this.getItemStack().isEmpty())
        {
            this.storedAnimal = null;
        }

        return Optional.ofNullable(this.storedAnimal);
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

        boolean updated = this.getStoredAnimal().
            map(animal -> ((AnimalPenInterface) animal).animalPenTick(this)).
            orElse(false);

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
        if (this.getOwner().isPresent() && !this.getOwner().get().equals(player.getUUID()))
        {
            AnimalPen.sendDebug("Protection is enabled");
            // Not an owner. Cannot interact.
            return false;
        }

        if (this.inventory.isEmpty())
        {
            ItemStack itemInHand = player.getItemInHand(interactionHand);

            if (!itemInHand.getOrCreateTag().contains(AnimalCageItem.TAG_ENTITY_ID))
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
                        AnimalPen.CHANNEL.sendToPlayers(((ServerLevel) this.level).players().stream().
                                filter(other ->
                                    other.distanceToSqr(this.getBlockPos().getX(),
                                        this.getBlockPos().getY(),
                                        this.getBlockPos().getZ()) < 50).
                                toList(),
                            new UpdateVariantScreenData(this.getBlockPos()));
                    }

                    AnimalPen.sendDebug("Deposit animal cage into pen");
                }

                return true;
            }
        }
        else
        {
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            CompoundTag itemInHandTag = itemInHand.getOrCreateTag();

            if (!itemInHandTag.contains(AnimalCageItem.TAG_ENTITY_ID))
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

                Animal animal = this.getStoredAnimal().orElse(null);

                if (animal == null)
                {
                    // Animal is not loaded.
                    return false;
                }

                long currentCount = ((AnimalPenInterface) animal).animalPenGetCount();

                if (currentCount < 2)
                {
                    AnimalPen.sendDebug("Cannot split 1");
                    // Cannot split 1 or 0
                    return false;
                }

                long newCount = Math.min(currentCount - this.protectedAmount, currentCount / 2);

                if (newCount <= 0)
                {
                    AnimalPen.sendDebug("Protected amount prevents from splitting");
                    // Only positive numbers allowed
                    return false;
                }

                if (!((AnimalPenInterface) animal).animalPenUpdateCount(-newCount))
                {
                    AnimalPen.sendDebug("Fail to reduce animal count");
                    return false;
                }

                animal.save(itemInHandTag);
                itemInHandTag.putLong(AnimalCageItem.TAG_AMOUNT, newCount);

                player.setItemInHand(interactionHand, itemInHand);
                this.inventory.setChanged();

                AnimalPen.sendDebug("Half the amount of animals in pen");
                // Remove half of animals.
                return true;
            }
            else
            {
                Animal animal = this.getStoredAnimal().orElse(null);

                if (animal == null ||
                    !itemInHandTag.getString(AnimalCageItem.TAG_ENTITY_ID).
                        equals(animal.getType().arch$registryName().toString()))
                {
                    AnimalPen.sendDebug("Different animals");

                    // Cannot do with different animal types.
                    return false;
                }

                if (player.getLevel().isClientSide())
                {
                    // Next only on server.
                    return true;
                }

                long newCount = itemInHandTag.getLong(AnimalCageItem.TAG_AMOUNT);

                if (newCount <= 0 || !((AnimalPenInterface) animal).animalPenUpdateCount(newCount))
                {
                    AnimalPen.sendDebug("Failed to deposit animal");
                    return false;
                }

                // Handle animal variants
                if (newCount > 1 &&
                    !AnimalCageItem.canMergeAnimalVariants(this.getItemStack(), itemInHand, player))
                {
                    AnimalPen.sendDebug("Variants could not be merged");

                    ((AnimalPenInterface) animal).animalPenUpdateCount(-1);
                    itemInHandTag.putLong(AnimalCageItem.TAG_AMOUNT, 1);
                    itemInHand.setTag(itemInHandTag);
                }
                else
                {
                    AnimalCageItem.mergeAnimalVariants(this.getItemStack(), itemInHand, player);
                    itemInHand.setTag(new CompoundTag());

                    if (this.level != null && !this.level.isClientSide())
                    {
                        // Trigger screen Update
                        AnimalPen.CHANNEL.sendToPlayers(((ServerLevel) this.level).players().stream().
                                filter(other ->
                                    other.distanceToSqr(this.getBlockPos().getX(),
                                        this.getBlockPos().getY(),
                                        this.getBlockPos().getZ()) < 50).
                                toList(),
                            new UpdateVariantScreenData(this.getBlockPos()));
                    }
                }

                player.setItemInHand(interactionHand, itemInHand);
                this.inventory.setChanged();

                AnimalPen.sendDebug("Cage merged into pen");

                return true;
            }
        }
    }


    /**
     * This method processes player interaction with pen tile entity with any item in main hand.
     *
     * @param player the player
     * @param interactionHand the interaction hand
     * @return {@code true} if interaction was successful, {@code false} otherwise.
     */
    public boolean interactWithPen(Player player, InteractionHand interactionHand)
    {
        if (this.getOwner().isPresent() && !this.getOwner().get().equals(player.getUUID()))
        {
            AnimalPen.sendDebug("Protection is enabled");

            // Not an owner. Cannot interact.
            return false;
        }

        ItemStack itemInHand = player.getItemInHand(interactionHand);

        if (itemInHand.isEmpty() && !this.inventory.isEmpty())
        {
            if (player.isCrouching() && !player.getLevel().isClientSide())
            {
                ItemStack item = this.getItemStack();
                player.setItemInHand(interactionHand, item);
                this.inventory.setItem(0, ItemStack.EMPTY);
                this.inventory.setChanged();

                AnimalPen.sendDebug("Taking out animal cage");
            }

            return true;
        }

        Animal animal = this.getStoredAnimal().orElse(null);

        if (animal == null)
        {
            AnimalPen.sendDebug("Animal is not set");
            return false;
        }

        if (((AnimalPenInterface) animal).animalPenInteract(player, interactionHand, this.getBlockPos()))
        {
            AnimalPen.sendDebug("Animal Interaction finished");

            ItemStack item = this.getItemStack();

            Optional<ListTag> optionalVariants = AnimalCageItem.getAnimalVariants(item);

            // Reset tag, as some animals may need it.
            CompoundTag tag = new CompoundTag();
            animal.save(tag);

            // Restore animal variants
            optionalVariants.ifPresent(variants -> tag.put(AnimalCageItem.TAG_VARIANTS, variants));

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
        if (this.getOwner().isPresent() && !this.getOwner().get().equals(player.getUUID()))
        {
            AnimalPen.sendDebug("Protection is enabled");
            // Not an owner. Cannot interact.
            return;
        }

        ItemStack weapon = player.getItemInHand(InteractionHand.MAIN_HAND);

        Animal animal = this.getStoredAnimal().orElse(null);

        if (animal == null)
        {
            AnimalPen.sendDebug("Animal Pen is empty");
            return;
        }

        long amount = ((AnimalPenInterface) animal).animalPenGetCount();

        if (amount <= this.protectedAmount)
        {
            AnimalPen.sendDebug("Protected amount reached");
            // Cannot attack anymore
            return;
        }

        if (!((AnimalPenInterface) animal).animalPenUpdateCount(-1))
        {
            AnimalPen.sendDebug("Failed to update animal count");
            return;
        }

        int fireAspect = EnchantmentHelper.getFireAspect(player);

        if (AnimalPen.config().isIncreaseStatistics())
        {
            player.awardStat(Stats.ITEM_USED.get(weapon.getItem()));
        }

        weapon.hurtAndBreak(1, player, (playerx) -> playerx.broadcastBreakEvent(InteractionHand.MAIN_HAND));

        this.deathTicker.add(0);

        if (((AnimalPenInterface) animal).animalPenGetCount() <= 0)
        {
            ItemStack item = this.getItemStack();
            item.setTag(new CompoundTag());

            Block.popResource(level, this.getBlockPos().above(), item);
            this.inventory.setItem(0, ItemStack.EMPTY);
            AnimalPen.sendDebug("Dropping empty cage");
        }

        this.triggerUpdate();

        Vec3 position = new Vec3(this.worldPosition.getX(),
            this.worldPosition.getY(),
            this.worldPosition.getZ());

        // Set fire-ticks (use fire aspect, to get rid of any stored value before).
        animal.setRemainingFireTicks(fireAspect);

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
            Block.popResource(level, this.getBlockPos().offset(0.5, 1, 0.5), itemStack));

        animal.clearFire();

        int reward = animal.getExperienceReward();
        ExperienceOrb.award((ServerLevel) this.level, position.add(0.5, 1, 0.5), reward);

        if (AnimalPen.config().isTriggerAdvancements())
        {
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger((ServerPlayer) player,
                animal,
                DamageSource.playerAttack(player));
        }

        if (AnimalPen.config().isIncreaseStatistics())
        {
            player.awardStat(Stats.MOB_KILLS);
            player.awardStat(Stats.ENTITY_KILLED.get(animal.getType()));
        }

        AnimalPen.sendDebug("Animal Kill process finished");
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
        return this.getStoredAnimal().
            map(animal -> ((AnimalPenInterface) animal).getRedStoneSignal()).
            orElse(0);
    }


    public void triggerUpdate()
    {
        this.setChanged();

        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        this.getStoredAnimal().ifPresent(
            animal -> animal.save(this.getItemStack().getOrCreateTag()));

        this.level.sendBlockUpdated(this.getBlockPos(),
            this.getBlockState(),
            this.getBlockState(),
            Block.UPDATE_CLIENTS);
    }


    /**
     * Returns inventory of this tile entity
     *
     * @return inventory
     */
    public SimpleContainer getInventory()
    {
        return this.inventory;
    }


    /**
     * This method returns item stack of currently stored cage.
     *
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
     *
     * @return List of entity variants in cage.
     */
    @Override
    public ListTag getEntityVariants()
    {
        return this.getStoredAnimal().
            map(animal -> AnimalCageItem.getAnimalVariants(this.getItemStack()).orElseGet(ListTag::new)).
            orElseGet(ListTag::new);
    }


    /**
     * This method returns animal display size.
     *
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
     *
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
     *
     * @param animalVariant a new animal variant
     */
    @Override
    public void updateAnimalVariant(CompoundTag animalVariant)
    {
        if (animalVariant == null || animalVariant.isEmpty())
        {
            // Nothing to update
            return;
        }

        this.getStoredAnimal().ifPresent(animal ->
        {
            AnimalPen.sendDebug("Animal Variant changed");

            // Save extra data
            CompoundTag extraData = new CompoundTag();
            ((AnimalPenInterface) animal).animalPenSaveTag(extraData);

            // load new variant
            animal.load(animalVariant);

            // Apply data
            ((AnimalPenInterface) animal).animalPenLoadTag(extraData);
            this.triggerUpdate();

            if (this.level != null && !this.level.isClientSide())
            {
                // Trigger update.
                AnimalPen.CHANNEL.sendToPlayers(((ServerLevel) this.level).players().stream().
                        filter(other ->
                            other.distanceToSqr(this.getBlockPos().getX(),
                                this.getBlockPos().getY(),
                                this.getBlockPos().getZ()) < 50).
                        toList(),
                    new UpdateVariantScreenData(this.getBlockPos()));
            }
        });
    }


    /**
     * This method removes animal pen variant with given index.
     *
     * @param index the variant index to be removed
     */
    @Override
    public void removeAnimalVariant(int index)
    {
        if (this.getStoredAnimal().isEmpty() || this.getEntityVariants().size() <= index)
        {
            return;
        }

        this.getEntityVariants().remove(index);
        this.inventory.setChanged();

        if (this.level != null && !this.level.isClientSide())
        {
            // Trigger screen Update
            AnimalPen.CHANNEL.sendToPlayers(((ServerLevel) this.level).players().stream().
                    filter(other ->
                        other.distanceToSqr(this.getBlockPos().getX(),
                            this.getBlockPos().getY(),
                            this.getBlockPos().getZ()) < 50).
                    toList(),
                new UpdateVariantScreenData(this.getBlockPos()));

            AnimalPen.sendDebug("Animal variant removed");
        }
    }


    /**
     * This method returns the count of animals in pen.
     *
     * @return The animal count in pen.
     */
    @Override
    public long getAnimalCount()
    {
        return this.getStoredAnimal().
            map(animal -> ((AnimalPenInterface) animal).animalPenGetCount()).
            orElse(0L);
    }


    @Override
    public boolean canGrowEntity()
    {
        return AnimalPen.config().isGrowAnimals();
    }


    /**
     * This method returns the description lines that will be displayed above tile entity.
     * @param shortText Indicates if text should be short or long version
     * @return List of pairs that contains display icon and text next to it
     */
    @Override
    public List<Pair<ItemStack[], Component>> getCooldownLines(boolean shortText)
    {
        return this.getStoredAnimal().
            map(animal -> ((AnimalPenInterface) animal).animalPenGetLines(this.getTickCounter(), shortText)).
            orElse(Collections.emptyList());
    }


    @Override
    public long getProtectedAmount()
    {
        return this.protectedAmount;
    }


    @Override
    public void setProtectedAmount(long amount)
    {
        this.protectedAmount = amount;
        this.triggerUpdate();
    }


    @Override
    public Optional<UUID> getOwner()
    {
        return Optional.ofNullable(this.ownerUUID);
    }


    @Override
    public void setOwner(@Nullable UUID owner)
    {
        this.ownerUUID = owner;
        this.triggerUpdate();
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
            return stack.is(AnimalPensItemRegistry.ANIMAL_CAGE.get());
        }


        @Override
        public void setChanged()
        {
            super.setChanged();
            AnimalPenTileEntity.this.triggerUpdate();
        }
    };


    private Animal storedAnimal;

    private long displaySize = -1;

    private UUID ownerUUID;

    private long protectedAmount = 0;

    private int tickCounter;

    private final List<Integer> deathTicker = new ArrayList<>();

    public static final String TAG_INVENTORY = "inventory";

    public static final String TAG_DEATH_TICKER = "death_ticker";

    public static final String TAG_DISPLAY_SIZE = "display_size";

    public static final String TAG_OWNER_UUID = "owner_uuid";

    public static final String TAG_KEEP_AMOUNT = "keep_amount";
}
