//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.data.saveddata.IndividualPenStorage;
import lv.id.bonne.animalpen.interaction.function.FunctionKey;
import lv.id.bonne.animalpen.interaction.ingredient.ConsumerEntry;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.items.AbstractAnimalStorageItem;
import lv.id.bonne.animalpen.network.packets.UpdateVariantScreenData;
import lv.id.bonne.animalpen.processing.executor.AnimalInteractionExecutor;
import lv.id.bonne.animalpen.processing.executor.DispenserInteractionExecutor;
import lv.id.bonne.animalpen.processing.executor.PlayerInteractionExecutor;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import lv.id.bonne.animalpen.util.AnimalPenVariantHelper;
import lv.id.bonne.animalpen.util.ItemTransferUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;


public abstract class AbstractAnimalPenBlockEntity extends BlockEntity
{

    public AbstractAnimalPenBlockEntity(BlockEntityType<?> blockEntityType,
        BlockPos blockPos,
        BlockState blockState)
    {
        super(blockEntityType, blockPos, blockState);
    }


    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);

        tag.put(AnimalPenCompoundTags.TAG_INVENTORY, this.inventory.createTag());
        tag.put(AnimalPenCompoundTags.TAG_DEATH_TICKER, new IntArrayTag(this.deathTicker));
        tag.putLong(AnimalPenCompoundTags.TAG_DISPLAY_SIZE, this.displaySize);

        this.getOwner().ifPresent(owner -> tag.putUUID(AnimalPenCompoundTags.TAG_OWNER_UUID, owner));
        tag.putLong(AnimalPenCompoundTags.TAG_KEEP_AMOUNT, this.protectedAmount);
    }


    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);

        this.inventory.clearContent();
        this.deathTicker.clear();

        if (tag.contains(AnimalPenCompoundTags.TAG_INVENTORY, Tag.TAG_LIST))
        {
            this.inventory.fromTag(tag.getList(AnimalPenCompoundTags.TAG_INVENTORY, Tag.TAG_COMPOUND));
        }

        if (tag.contains(AnimalPenCompoundTags.TAG_DEATH_TICKER, Tag.TAG_INT_ARRAY))
        {
            int[] intArray = tag.getIntArray(AnimalPenCompoundTags.TAG_DEATH_TICKER);

            for (int i : intArray)
            {
                this.deathTicker.add(i);
            }
        }

        if (tag.contains(AnimalPenCompoundTags.TAG_DISPLAY_SIZE, Tag.TAG_LONG))
        {
            this.displaySize = tag.getLong(AnimalPenCompoundTags.TAG_DISPLAY_SIZE);
        }
        else
        {
            this.displaySize = -1;
        }

        if (tag.contains(AnimalPenCompoundTags.TAG_OWNER_UUID))
        {
            this.ownerUUID = tag.getUUID(AnimalPenCompoundTags.TAG_OWNER_UUID);
        }

        this.protectedAmount = tag.getLong(AnimalPenCompoundTags.TAG_KEEP_AMOUNT);

        if (this.getStoredAnimal().isEmpty())
        {
            this.storedAnimal = null;
        }

        if (this.getLevel() != null && this.getLevel().isClientSide())
        {
            if (this.storedAnimal != null)
            {
                // Update client entity
                this.storedAnimal.load(this.getItemStack().getOrCreateTag().
                    getCompound(AnimalPenCompoundTags.TAG_ANIMAL));
            }

            if (!this.getItemStack().isEmpty())
            {
                CompoundTag mobNBT = this.getItemStack().getOrCreateTag();
                CompoundTag animalData = mobNBT.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);
                CompoundTag coolDown = animalData.getCompound(AnimalPenCompoundTags.TAG_COOLDOWN);

                Set<String> allKeys = new HashSet<>(coolDown.getAllKeys());

                for (String key : allKeys)
                {
                    coolDown.putLong(key, coolDown.getLong(key) + this.level.getGameTime());
                }
            }
        }

        this.setChanged();
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
     * This method triggers update of tile entity that is send to clients.
     */
    public void triggerUpdate()
    {
        this.setChanged();

        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        this.level.sendBlockUpdated(this.getBlockPos(),
            this.getBlockState(),
            this.getBlockState(),
            Block.UPDATE_CLIENTS);
    }


// ---------------------------------------------------------------------
// Section: Interaction Entries
// ---------------------------------------------------------------------


    /**
     * The tick method for processing entity related stuff.
     */
    public void tick()
    {
        this.tickCounter++;

        boolean updated = this.getStoredAnimal().
            map(animal ->
            {
                CompoundTag mobNBT = this.getItemStack().getOrCreateTag();
                CompoundTag animalData = mobNBT.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);
                CompoundTag coolDown = animalData.getCompound(AnimalPenCompoundTags.TAG_COOLDOWN);

                boolean requiresUpdate = false;

                if (this.level instanceof ServerLevel serverLevel)
                {
                    Set<String> allKeys = new HashSet<>(coolDown.getAllKeys());

                    for (String key : allKeys)
                    {
                        long time = coolDown.getLong(key);

                        if (--time > 0)
                        {
                            coolDown.putLong(key, time);
                        }
                        else
                        {
                            coolDown.remove(key);
                            this.triggerEndFunctions(serverLevel, animal, mobNBT, key);
                            requiresUpdate = true;
                        }
                    }

                    // trigger continuous functions
                    requiresUpdate |= this.triggerStartFunctions(serverLevel, animal, mobNBT, coolDown);

                    if (!requiresUpdate && !allKeys.isEmpty() && this.tickCounter % 100 == 0)
                    {
                        // Trigger update every 5 seconds if there are cooldowns, as clients that visits
                        // area may not have cooldowns loaded.
                        requiresUpdate = true;
                    }
                }
                else
                {
                    Set<String> allKeys = new HashSet<>(coolDown.getAllKeys());

                    for (String key : allKeys)
                    {
                        long time = coolDown.getLong(key);

                        if (time < this.level.getGameTime())
                        {
                            // Remove expired cooldowns
                            coolDown.remove(key);
                        }
                    }
                }

                animalData.put(AnimalPenCompoundTags.TAG_COOLDOWN, coolDown);

                return requiresUpdate;
            }).
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
        if (interactionHand != InteractionHand.MAIN_HAND)
        {
            // Prevent interaction with non-main hand
            return false;
        }

        if (this.getOwner().isPresent() && !this.getOwner().get().equals(player.getUUID()))
        {
            AnimalPen.sendDebug("Protection is enabled");
            // Not an owner. Cannot interact.
            return false;
        }

        if (this.inventory.isEmpty())
        {
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            CompoundTag animalTag =
                itemInHand.getOrCreateTag().getCompound(AnimalPenCompoundTags.TAG_ANIMAL);

            if (!animalTag.contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
            {
                return false;
            }
            else
            {
                if (!player.level().isClientSide())
                {
                    this.inventory.addItem(itemInHand);
                    player.setItemInHand(interactionHand, ItemStack.EMPTY);

                    if (this.level != null && !this.level.isClientSide())
                    {
                        // Trigger screen Update
                        AnimalPen.CHANNEL.sendToPlayers(((ServerLevel) this.level).players().stream().
                                filter(other -> other.blockPosition().distSqr(this.getBlockPos()) < 30).
                                toList(),
                            new UpdateVariantScreenData(this.getBlockPos(), null));
                    }

                    AnimalPen.sendDebug("Deposit animal cage into pen");
                }

                return true;
            }
        }
        else
        {
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            CompoundTag itemTag = itemInHand.getOrCreateTag();
            CompoundTag animalTag = itemTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL);
            CompoundTag animalData = itemTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);

            if (!animalTag.contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
            {
                if (!player.isCrouching())
                {
                    // Empty... nothing to do.
                    return false;
                }

                if (player.level().isClientSide())
                {
                    // Next only on server.
                    return true;
                }

                Mob animal = this.getStoredAnimal().orElse(null);

                if (animal == null)
                {
                    // Animal is not loaded.
                    return false;
                }

                long currentCount = this.getAnimalCount();

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

                if (!this.updateAnimalCount(-newCount))
                {
                    AnimalPen.sendDebug("Fail to reduce animal count");
                    return false;
                }

                animal.save(animalTag);
                itemTag.put(AnimalPenCompoundTags.TAG_ANIMAL, animalTag);

                animalData.putLong(AnimalPenCompoundTags.TAG_AMOUNT, newCount);
                itemTag.put(AnimalPenCompoundTags.TAG_ANIMAL_DATA, animalData);
                itemInHand.setTag(itemTag);

                player.setItemInHand(interactionHand, itemInHand);
                this.inventory.setChanged();

                AnimalPen.sendDebug("Half the amount of animals in pen");
                // Remove half of animals.
                return true;
            }
            else
            {
                Mob animal = this.getStoredAnimal().orElse(null);

                if (animal == null ||
                    !animalTag.getString(AnimalPenCompoundTags.TAG_ENTITY_ID).
                        equals(animal.getType().arch$registryName().toString()))
                {
                    AnimalPen.sendDebug("Different animals");

                    // Cannot do with different animal types.
                    return false;
                }

                if (player.level().isClientSide())
                {
                    // Next only on server.
                    return true;
                }

                long newCount = animalData.getLong(AnimalPenCompoundTags.TAG_AMOUNT);

                if (newCount <= 0 || !this.updateAnimalCount(newCount))
                {
                    AnimalPen.sendDebug("Failed to deposit animal");
                    return false;
                }

                Consumer<String> errorSender = error ->
                {
                    if (this.getItemStack().getItem() instanceof AbstractAnimalStorageItem item)
                    {
                        item.error(player, error);
                    }
                };

                // Handle animal variants
                if (newCount > 1 &&
                    !AnimalPenVariantHelper.canMergeAnimalVariants(this.getItemStack(),
                        itemInHand,
                        this.level,
                        player,
                        errorSender))
                {
                    AnimalPen.sendDebug("Variants could not be merged");

                    this.updateAnimalCount(-1);
                    animalData.putLong(AnimalPenCompoundTags.TAG_AMOUNT, 1);
                    itemTag.put(AnimalPenCompoundTags.TAG_ANIMAL_DATA, animalData);

                    itemInHand.setTag(itemTag);
                }
                else
                {
                    AnimalPenVariantHelper.mergeAnimalVariants(this.getItemStack(),
                        itemInHand,
                        this.level,
                        player,
                        errorSender);
                    itemInHand.setTag(new CompoundTag());

                    if (this.level != null && !this.level.isClientSide())
                    {
                        // Trigger screen Update
                        AnimalPen.CHANNEL.sendToPlayers(((ServerLevel) this.level).players().stream().
                                filter(other ->
                                    other.blockPosition().distSqr(this.getBlockPos()) < 30).
                                toList(),
                            new UpdateVariantScreenData(this.getBlockPos(), this.getEntityVariants()));
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
            if (player.isCrouching() && !player.level().isClientSide())
            {
                ItemStack item = this.getItemStack();
                player.setItemInHand(interactionHand, item);
                this.inventory.setItem(0, ItemStack.EMPTY);
                this.inventory.setChanged();

                AnimalPen.sendDebug("Taking out animal cage");
            }

            return true;
        }

        if (!(this.level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer))
        {
            // Compute only on server side
            return true;
        }

        // Create executor for interactions.
        return this.performInteraction(new PlayerInteractionExecutor(serverPlayer, interactionHand, this),
            serverLevel,
            itemInHand).success();
    }


    /**
     * This method processes dispenser interaction with pen tile entity with any item.
     *
     * @param serverLevel the level where interaction happens
     * @param source the interaction source block
     * @param index the index of item from inventory
     * @param itemInHand the interaction item
     * @return modified item stack if interaction succeeded, or empty if failed.
     */
    public InteractionResult interactWithPen(ServerLevel serverLevel,
        DispenserBlockEntity source,
        int index,
        ItemStack itemInHand)
    {
        if (this.getOwner().isPresent())
        {
            AnimalPen.sendDebug("Protection is enabled");

            // Not an owner. Cannot interact.
            return InteractionResult.FAILED;
        }

        Mob animal = this.getStoredAnimal().orElse(null);

        if (animal == null)
        {
            AnimalPen.sendDebug("Animal is not set");
            return InteractionResult.FAILED;
        }

        if (source == null)
        {
            AnimalPen.sendDebug("Interaction success through block without container.");
            return InteractionResult.FAILED;
        }

        return this.performInteraction(new DispenserInteractionExecutor(serverLevel, source, index, this),
            serverLevel,
            itemInHand);
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

        Mob animal = this.getStoredAnimal().orElse(null);

        if (animal == null)
        {
            AnimalPen.sendDebug("Animal Pen is empty");
            return;
        }

        long amount = this.getAnimalCount();

        if (amount <= this.protectedAmount)
        {
            AnimalPen.sendDebug("Protected amount reached");
            // Cannot attack anymore
            return;
        }

        if (!this.updateAnimalCount(-1))
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

        if (this.getAnimalCount() <= 0)
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

        LootTable lootTable = level.getServer().getLootData().getLootTable(animal.getLootTable());

        LootParams.Builder paramsBuilder = new LootParams.Builder((ServerLevel) level).
            withParameter(LootContextParams.ORIGIN, position).
            withParameter(LootContextParams.THIS_ENTITY, animal).
            withParameter(LootContextParams.KILLER_ENTITY, player).
            withParameter(LootContextParams.DIRECT_KILLER_ENTITY, player).
            withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).
            withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player)).
            withLuck(player.getLuck());

        lootTable.getRandomItems(paramsBuilder.create(LootContextParamSets.ENTITY), level.getRandom().nextLong()).
            forEach(itemStack ->
                ItemTransferUtil.insertBellowOrDrop(level,
                    itemStack,
                    this.getBlockPos(),
                    this.getBlockPos().above()));

        animal.clearFire();

        int reward = animal.getExperienceReward();
        ExperienceOrb.award((ServerLevel) this.level, position.add(0.5, 1, 0.5), reward);

        if (AnimalPen.config().isTriggerAdvancements())
        {
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger((ServerPlayer) player,
                animal,
                level.damageSources().playerAttack(player));
        }

        if (AnimalPen.config().isIncreaseStatistics())
        {
            player.awardStat(Stats.MOB_KILLS);
            player.awardStat(Stats.ENTITY_KILLED.get(animal.getType()));
        }

        AnimalPen.sendDebug("Animal Kill process finished");
    }


// ---------------------------------------------------------------------
// Section: Processing methods
// ---------------------------------------------------------------------


    /**
     * This method performs interaction with animal using given item in hand.
     *
     * @param executor The process executor
     * @param serverLevel The server level
     * @param itemInHand The item that is used.
     * @return returns Result of interaction.
     */
    private InteractionResult performInteraction(AnimalInteractionExecutor executor,
        ServerLevel serverLevel,
        ItemStack itemInHand)
    {
        Mob animal = this.getStoredAnimal().orElse(null);

        if (animal == null)
        {
            AnimalPen.sendDebug("Animal is not set");
            return InteractionResult.FAILED;
        }

        CompoundTag mobNBT = this.getItemStack().getOrCreateTag();

        Optional<AnimalInteraction> interactionOptional =
            AnimalPenInteractionRegistry.matchInteraction(animal, mobNBT, itemInHand);

        if (interactionOptional.isEmpty())
        {
            AnimalPen.sendDebug("Interaction with " + this.getBlockPos() + " cannot be success with " + itemInHand);
            return InteractionResult.FAILED;
        }

        AnimalInteraction interaction = interactionOptional.get();
        long animalCount = interaction.normalizeMobCount(
            mobNBT.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA).
                getLong(AnimalPenCompoundTags.TAG_AMOUNT));

        // Indication data has been changed.
        boolean dataUpdate = false;

        // Calculate consumption
        ConsumerEntry itemConsumer = interaction.consumer();
        ItemStack consumedItem;
        int consumedAmount;

        if (itemConsumer == null)
        {
            consumedItem = ItemStack.EMPTY;
            consumedAmount = 1;
        }
        else
        {
            consumedItem = itemConsumer.getConsumedItem(itemInHand);
            consumedAmount = itemConsumer.calculateConsumption(executor,
                consumedItem,
                animalCount,
                interaction.even());
        }

        if (consumedAmount == 0)
        {
            AnimalPen.sendDebug("Need at least 1 items in stack");
            return InteractionResult.FAILED;
        }

        dataUpdate |= interaction.applyCooldown(mobNBT, animalCount);

        List<ItemStack> lootItems = interaction.lootEntry() == null ?
            Collections.emptyList() :
            interaction.lootEntry().processLootTable(serverLevel, animal, this.getBlockPos(), animalCount, consumedAmount);

        executor.triggerItemUse(animal, itemInHand, consumedAmount);

        if (itemConsumer == null)
        {
            lootItems.forEach(itemStack -> ItemTransferUtil.insertBellowOrDrop(serverLevel,
                itemStack,
                this.getBlockPos(),
                this.dropPosition()));
        }
        else
        {
            itemInHand = itemConsumer.
                consumeItems(executor, lootItems, itemInHand, consumedAmount);
        }

        if (interaction.sound() != null)
        {
            this.level.registryAccess().registry(Registries.SOUND_EVENT).
                flatMap(registry -> registry.getOptional(interaction.sound())).
                ifPresent(soundEvent -> level.playSound(null,
                    this.getBlockPos(),
                    soundEvent,
                    SoundSource.AMBIENT,
                    1.0F,
                    Mth.randomBetween(level.getRandom(), 0.8F, 1.2F)));
        }

        dataUpdate |= executor.triggerFunctions(interaction,
            consumedItem,
            consumedAmount,
            animal,
            mobNBT,
            this.getBlockPos());

        if (dataUpdate)
        {
            AnimalPen.sendDebug("Animal Interaction finished");

            ItemStack item = this.getItemStack();

            if (this.getAnimalCount() <= 0)
            {
                item.setTag(new CompoundTag());
                Block.popResource(serverLevel, this.getBlockPos().above(), item);
                this.inventory.setItem(0, ItemStack.EMPTY);
                AnimalPen.sendDebug("Dropping empty cage");
            }
            else
            {
                item.setTag(mobNBT);
                AnimalPen.sendDebug("Updating cage tag");
                this.inventory.setChanged();
            }

            // Trigger update.
            return new InteractionResult(true, itemInHand);
        }

        return InteractionResult.FAILED;
    }


    /**
     * This method trigger end functions for interaction with given key for given mob.
     */
    private void triggerEndFunctions(ServerLevel level, Mob mob, CompoundTag mobNBT, String key)
    {
        AnimalPenInteractionRegistry.getInteractions(mob).stream().
            filter(interaction -> key.equals(interaction.id())).
            filter(interaction -> !interaction.finishFunctions().isEmpty()).
            findFirst().
            map(AnimalInteraction::finishFunctions).
            orElse(List.of()).
            forEach(function ->
            {
                function.id().processFunction(level,
                    mob,
                    mobNBT,
                    this.getBlockPos(),
                    function.key(),
                    function.value());
            });
    }


    /**
     * This method triggers start functions for interactions without interaction item. It only runs for interactions it
     * can fulfil conditions
     */
    private boolean triggerStartFunctions(ServerLevel level, Mob mob, CompoundTag mobNBT, CompoundTag newCooldowns)
    {
        boolean anyChanges = false;

        // StreamAPI may cause some unneeded garbage collector calls.
        for (AnimalInteraction animalInteraction : AnimalPenInteractionRegistry.getInteractions(mob))
        {
            if (!newCooldowns.contains(animalInteraction.id()) &&
                animalInteraction.ingredient().isEmpty() &&
                animalInteraction.matchAllConditions(mobNBT))
            {
                anyChanges |= this.triggerStartFunction(level,
                    mob,
                    mobNBT,
                    newCooldowns,
                    animalInteraction);
            }
        }

        return anyChanges;
    }


    /**
     * This method triggers start functions for interaction without interaction item. It only runs for interaction it
     * can fulfil conditions
     */
    private boolean triggerStartFunction(ServerLevel level,
        Mob mob,
        CompoundTag mobNBT,
        CompoundTag newCooldowns,
        AnimalInteraction interaction)
    {
        boolean anyChanges = false;

        long animalCount = mobNBT.
            getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA).
            getLong(AnimalPenCompoundTags.TAG_AMOUNT);

        // Apply cooldown
        if (interaction.cooldown() != null)
        {
            long cooldownTime = interaction.cooldown().calculateCooldown(animalCount);
            newCooldowns.putLong(interaction.id(), cooldownTime);
            anyChanges = true;
        }

        if (interaction.even() && animalCount % 2 != 0)
        {
            animalCount--;
        }

        // Process loot table
        List<ItemStack> lootItems = interaction.lootEntry() == null ?
            Collections.emptyList() :
            interaction.lootEntry().processLootTable(level, mob, this.getBlockPos(), animalCount, 1);

        lootItems.forEach(itemStack -> ItemTransferUtil.insertBellowOrDrop(level,
            itemStack,
            this.getBlockPos(),
            this.dropPosition()));

            // Play sound
            if (interaction.sound() != null)
            {
                this.level.registryAccess().registry(Registries.SOUND_EVENT).
                    flatMap(registry -> registry.getOptional(interaction.sound())).
                    ifPresent(soundEvent -> level.playSound(null,
                        this.getBlockPos(),
                        soundEvent,
                        SoundSource.AMBIENT,
                        1.0F,
                        Mth.randomBetween(level.getRandom(), 0.8F, 1.2F)));
        }

        // Trigger start functions
        for (FunctionKey function : interaction.runFunctions())
        {
            anyChanges |= function.id().processFunction(level,
                mob,
                mobNBT,
                this.getBlockPos(),
                function.key(),
                function.value());
        }

        return anyChanges;
    }


// ---------------------------------------------------------------------
// Section: Entity Variant Handling
// ---------------------------------------------------------------------


    /**
     * Returns the list of entity variants stored in cage.
     *
     * @return List of entity variants in cage.
     */
    public ListTag getEntityVariants()
    {
        return this.getStoredAnimal().
            map(animal -> AnimalPenVariantHelper.getAnimalVariants(this.getItemStack(), this.level).
                map(IndividualPenStorage::getVariants).
                orElseGet(ListTag::new)).
            orElseGet(ListTag::new);
    }


    /**
     * This method sets new animal variant from given CompoundTag tag.
     *
     * @param index a new animal variant index
     */
    public void updateAnimalVariant(int index)
    {
        if (this.getStoredAnimal().isEmpty())
        {
            return;
        }

        ListTag entityVariants = this.getEntityVariants();

        if ( index >= entityVariants.size() || index < 0)
        {
            return;
        }

        CompoundTag animalVariant = (CompoundTag) entityVariants.get(index);

        if (animalVariant == null || animalVariant.isEmpty())
        {
            // Nothing to update
            return;
        }

        this.getStoredAnimal().ifPresent(animal ->
        {
            AnimalPen.sendDebug("Animal Variant changed");

            // load new variant
            animal.load(animalVariant);

            // Update animal variant in item stack.
            ItemStack itemStack = this.getItemStack();
            CompoundTag tag = itemStack.getOrCreateTag();
            tag.put(AnimalPenCompoundTags.TAG_ANIMAL, animalVariant);
            itemStack.setTag(tag);

            // Apply data
            this.triggerUpdate();

            if (this.level != null && !this.level.isClientSide())
            {
                // Trigger update.
                AnimalPen.CHANNEL.sendToPlayers(((ServerLevel) this.level).players().stream().
                        filter(other ->
                            other.blockPosition().distSqr(this.getBlockPos()) < 30).
                        toList(),
                    new UpdateVariantScreenData(this.getBlockPos(), null));
            }
        });
    }


    /**
     * This method removes animal pen variant with given index.
     *
     * @param index the variant index to be removed
     */
    public void removeAnimalVariant(int index)
    {
        if (this.getStoredAnimal().isEmpty())
        {
            return;
        }

        if (this.level instanceof ServerLevel serverLevel)
        {
            CompoundTag tag = this.getItemStack().getOrCreateTag();

            if (!tag.hasUUID(AnimalPenCompoundTags.TAG_STORAGE_ID))
            {
                AnimalPen.sendDebug("Storage not set for item stack");
                return;
            }

            IndividualPenStorage storage = IndividualPenStorage.getOrCreate(serverLevel,
                tag.getUUID(AnimalPenCompoundTags.TAG_STORAGE_ID));

            if (index < 0 || index >= storage.getVariants().size())
            {
                AnimalPen.sendDebug("Out of bounds variant deletion");
                return;
            }

            storage.getVariants().remove(index);

            if (storage.getVariants().isEmpty())
            {
                IndividualPenStorage.delete(serverLevel,
                    tag.getUUID(AnimalPenCompoundTags.TAG_STORAGE_ID));
                tag.remove(AnimalPenCompoundTags.TAG_STORAGE_ID);
                tag.remove(AnimalPenCompoundTags.TAG_STORAGE_AMOUNT);

            }
            else
            {
                storage.setDirty();
                tag.putInt(AnimalPenCompoundTags.TAG_STORAGE_AMOUNT, storage.getVariants().size());
            }


            // Trigger screen Update
            AnimalPen.CHANNEL.sendToPlayers(serverLevel.players().stream().
                    filter(other ->
                        other.blockPosition().distSqr(this.getBlockPos()) < 30).
                    toList(),
                new UpdateVariantScreenData(this.getBlockPos(), storage.getVariants()));

            AnimalPen.sendDebug("Animal variant removed");
        }
    }


// ---------------------------------------------------------------------
// Section: Abstract methods
// ---------------------------------------------------------------------


    public boolean validateItemStack()
    {
        return this.validateItemStack(this.getItemStack());
    }


    public abstract boolean validateItemStack(ItemStack itemStack);


    public abstract boolean canGrowEntity();


    public abstract BlockPos dropPosition();


// ---------------------------------------------------------------------
// Section: Variable Change Methods
// ---------------------------------------------------------------------


    public List<Integer> getDeathTicker()
    {
        return this.deathTicker;
    }


    public int getTickCounter()
    {
        return this.tickCounter;
    }


    /**
     * This method returns animal display size.
     *
     * @return The display size of animal.
     */
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
    public void setAnimalDisplaySize(long size)
    {
        this.displaySize = size;
        this.triggerUpdate();
    }


    public long getProtectedAmount()
    {
        return this.protectedAmount;
    }


    public void setProtectedAmount(long amount)
    {
        this.protectedAmount = amount;
        this.triggerUpdate();
    }


    public Optional<UUID> getOwner()
    {
        return Optional.ofNullable(this.ownerUUID);
    }


    public void setOwner(@Nullable UUID owner)
    {
        this.ownerUUID = owner;
        this.triggerUpdate();
    }


    /**
     * This method returns the count of animals in pen.
     *
     * @return The animal count in pen.
     */
    public long getAnimalCount()
    {
        if (!this.validateItemStack())
        {
            // Not a valid item stack for current block entity.
            return 0;
        }

        ItemStack itemStack = this.getItemStack();

        if (!itemStack.hasTag())
        {
            return 0L;
        }

        // getOrCreate to not write null-pointer check
        CompoundTag itemStackTag = itemStack.getOrCreateTag();

        if (!itemStackTag.contains(AnimalPenCompoundTags.TAG_ANIMAL_DATA, Tag.TAG_COMPOUND))
        {
            return 0L;
        }

        return itemStackTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA).getLong(AnimalPenCompoundTags.TAG_AMOUNT);
    }


    /**
     * This method is used to change count of animals in stored data.
     */
    public boolean updateAnimalCount(long change)
    {
        if (!this.validateItemStack())
        {
            // Not a valid item stack for current block entity.
            return false;
        }

        ItemStack itemStack = this.getItemStack();

        if (!itemStack.hasTag())
        {
            return false;
        }

        // getOrCreate to not write null-pointer check
        CompoundTag itemStackTag = itemStack.getOrCreateTag();

        if (!itemStackTag.contains(AnimalPenCompoundTags.TAG_ANIMAL_DATA, Tag.TAG_COMPOUND))
        {
            // Does not contain any animal pen data.
            return false;
        }

        CompoundTag animalData = itemStackTag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);
        long animalCount = animalData.getLong(AnimalPenCompoundTags.TAG_AMOUNT);

        if (change < 0 && animalCount + change < 0)
        {
            return false;
        }

        long maxCount = AnimalPen.config().getMaximalAnimalCountNormalized();

        if (animalCount + change > maxCount)
        {
            return false;
        }

        animalData.putLong(AnimalPenCompoundTags.TAG_AMOUNT, animalCount + change);

        return true;
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


    /**
     * This method returns stored animal for block entity.
     *
     * @return Animal instance stored in block entity.
     */
    public Optional<Mob> getStoredAnimal()
    {
        if (this.storedAnimal == null && !this.getItemStack().isEmpty())
        {
            CompoundTag tag = this.getItemStack().getOrCreateTag().getCompound(AnimalPenCompoundTags.TAG_ANIMAL);

            if (this.level == null ||
                !tag.contains(AnimalPenCompoundTags.TAG_ENTITY_ID))
            {
                return Optional.ofNullable(this.storedAnimal);
            }

            EntityType.create(tag, this.level).map(entity -> (Mob) entity).
                ifPresent(animal -> this.storedAnimal = animal);
        }
        else if (this.storedAnimal != null && this.getItemStack().isEmpty())
        {
            this.storedAnimal = null;
        }
        else if (this.storedAnimal != null)
        {
            CompoundTag tag = this.getItemStack().getOrCreateTag().getCompound(AnimalPenCompoundTags.TAG_ANIMAL);

            if (!new ResourceLocation(tag.getString(AnimalPenCompoundTags.TAG_ENTITY_ID)).
                equals(this.storedAnimal.getType().arch$registryName()))
            {
                this.storedAnimal = null;
            }
        }

        return Optional.ofNullable(this.storedAnimal);
    }


// ---------------------------------------------------------------------
// Section: Text Line Generation
// ---------------------------------------------------------------------


    /**
     * This method returns the description lines that will be displayed above tile entity.
     *
     * @param shortText Indicates if text should be short or long version
     * @return List of pairs that contains display icon and text next to it
     */
    public List<Pair<ItemStack[], Component>> getCooldownLines(boolean shortText)
    {
        List<Pair<ItemStack[], Component>> textLines = new ArrayList<>();

        CompoundTag tag = this.getItemStack().getTag();
        CompoundTag cooldown;
        long animalCount;

        if (tag != null)
        {
            CompoundTag animalData = tag.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);
            cooldown = animalData.getCompound(AnimalPenCompoundTags.TAG_COOLDOWN);
            animalCount = animalData.getLong(AnimalPenCompoundTags.TAG_AMOUNT);
        }
        else
        {
            // Empty tag.
            cooldown = new CompoundTag();
            animalCount = 0L;
        }

        this.getStoredAnimal().ifPresent(animal ->
            AnimalPenInteractionRegistry.getInteractions(animal).stream().
                filter(animalInteraction -> !animalInteraction.textLines().isEmpty()).
                filter(animalInteraction -> AnimalPen.config().isShowAllInteractions() ||
                    !shortText ||
                    animalInteraction.cooldown() != null &&
                        animalInteraction.cooldown().calculateCooldown(animalCount) != 0).
                forEach(animalInteraction ->
                    {
                        boolean matchConditions = animalInteraction.matchAllConditions(tag);

                        animalInteraction.textLines().forEach(line ->
                        {
                            boolean containsInCooldown = cooldown.contains(animalInteraction.id());
                            boolean shouldExecute = switch (line.visibility())
                            {
                                case READY -> matchConditions && !containsInCooldown;
                                case COOLDOWN -> matchConditions && containsInCooldown;
                                case NOT_MATCH -> !matchConditions;
                                case ON_MATCH -> matchConditions;
                            };

                            if (shouldExecute)
                            {
                                long reminingTime = cooldown.getLong(animalInteraction.id()) - this.level.getGameTime();

                                Pair<ItemStack[], Component> componentPair =
                                    line.animalPenGetLine(animalInteraction.ingredient(),
                                        tag,
                                        this.tickCounter,
                                        shortText,
                                        reminingTime);

                                if (componentPair != null)
                                {
                                    textLines.add(componentPair);
                                }
                            }
                        });
                    }
                )
        );

        return textLines;
    }


    /**
     * This method returns redstone signal based on current signal that should be sent out by animal.
     *
     * @return the redstone signal value based on bit value: - 1 - is animal - 2 - can feed - 4 - can interact 1 - 8 -
     * can interact 2
     */
    public int getRedStoneSignal()
    {
        return this.getStoredAnimal().map(animal ->
        {
            Collection<AnimalInteraction> interactions =
                AnimalPenInteractionRegistry.getInteractions(animal);

            CompoundTag tag = this.getItemStack().getOrCreateTag();
            CompoundTag cooldowns = tag.
                getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA).
                getCompound(AnimalPenCompoundTags.TAG_COOLDOWN);
            int returnValue = 1;

            for (AnimalInteraction interaction : interactions)
            {
                if (interaction.redstoneSignal() > 0 &&
                    interaction.redstoneSignal() < 4 &&
                    interaction.matchAllConditions(tag) &&
                    !cooldowns.contains(interaction.id()))
                {
                    returnValue |= (1 << interaction.redstoneSignal());
                }
            }

            return returnValue;
        }).orElse(0);
    }


// ---------------------------------------------------------------------
// Section: Classes
// ---------------------------------------------------------------------


    public record InteractionResult(boolean success, ItemStack result)
    {
        static InteractionResult FAILED = new InteractionResult(false, ItemStack.EMPTY);
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    private final List<Integer> deathTicker = new ArrayList<>();

    private Mob storedAnimal;

    private final SimpleContainer inventory = new SimpleContainer(1)
    {
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack)
        {
            return AbstractAnimalPenBlockEntity.this.validateItemStack(stack);
        }


        @Override
        public void setChanged()
        {
            super.setChanged();
            AbstractAnimalPenBlockEntity.this.triggerUpdate();
        }
    };

    private long displaySize = -1;

    private UUID ownerUUID;

    private long protectedAmount = 0;

    private int tickCounter;
}
