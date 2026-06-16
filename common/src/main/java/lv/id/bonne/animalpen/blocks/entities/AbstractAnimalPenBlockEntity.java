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

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.data.saveddata.IndividualPenStorage;
import lv.id.bonne.animalpen.interaction.function.FunctionKey;
import lv.id.bonne.animalpen.interaction.ingredient.ConsumerEntry;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.items.AbstractAnimalStorageItem;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.items.component.StoredMobData;
import lv.id.bonne.animalpen.items.component.StoredMobVariantKey;
import lv.id.bonne.animalpen.network.packets.UpdateVariantScreenData;
import lv.id.bonne.animalpen.processing.executor.AnimalInteractionExecutor;
import lv.id.bonne.animalpen.processing.executor.DispenserInteractionExecutor;
import lv.id.bonne.animalpen.processing.executor.PlayerInteractionExecutor;
import lv.id.bonne.animalpen.registries.AnimalPenCriteriaTriggersRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenInteractionRegistry;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import lv.id.bonne.animalpen.util.AnimalPenItemHelper;
import lv.id.bonne.animalpen.util.AnimalPenVariantHelper;
import lv.id.bonne.animalpen.util.ItemTransferUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.saveAdditional(tag, provider);

        tag.put(AnimalPenCompoundTags.TAG_INVENTORY, this.inventory.createTag(provider));
        tag.put(AnimalPenCompoundTags.TAG_DEATH_TICKER, new IntArrayTag(this.deathTicker));
        tag.putLong(AnimalPenCompoundTags.TAG_DISPLAY_SIZE, this.displaySize);

        this.getOwner().ifPresent(owner -> tag.putUUID(AnimalPenCompoundTags.TAG_OWNER_UUID, owner));
        tag.putLong(AnimalPenCompoundTags.TAG_KEEP_AMOUNT, this.protectedAmount);
    }


    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider)
    {
        super.loadAdditional(tag, provider);

        this.inventory.clearContent();
        this.deathTicker.clear();

        if (tag.contains(AnimalPenCompoundTags.TAG_INVENTORY, Tag.TAG_LIST))
        {
            this.inventory.fromTag(tag.getList(AnimalPenCompoundTags.TAG_INVENTORY, Tag.TAG_COMPOUND), provider);
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

        if (this.getItemStack().isEmpty())
        {
            // Remove entity
            this.storedAnimal = null;
        }
        else if (this.storedAnimal != null && this.level != null && this.level.isClientSide())
        {
            // Load stored mob data into client to update server changes.
            StoredMob storedMob = this.getItemStack().
                get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
            this.storedAnimal.load(storedMob.tag());

            Map<String, Long> cooldowns = AnimalPenItemHelper.getCooldowns(this.getItemStack());

            Set<String> allKeys = new HashSet<>(cooldowns.keySet());

            for (String key : allKeys)
            {
                cooldowns.put(key, cooldowns.get(key) + this.level.getGameTime());
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
    public CompoundTag getUpdateTag(HolderLookup.Provider provider)
    {
        return this.saveWithoutMetadata(provider);
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
                Map<String, Long> cooldowns = AnimalPenItemHelper.getCooldowns(this.getItemStack());
                boolean requiresUpdate = false;

                if (this.level instanceof ServerLevel serverLevel)
                {
                    Set<String> allKeys = new HashSet<>(cooldowns.keySet());

                    for (String key : allKeys)
                    {
                        long time = cooldowns.get(key);

                        if (--time > 0)
                        {
                            cooldowns.put(key, time);
                        }
                        else
                        {
                            cooldowns.remove(key);
                            this.triggerEndFunctions(serverLevel, animal, this.getItemStack(), key);
                            requiresUpdate = true;
                        }
                    }

                    // trigger continuous functions
                    requiresUpdate |= this.triggerStartFunctions(serverLevel, animal, this.getItemStack(), cooldowns);

                    if (!requiresUpdate && !allKeys.isEmpty() && this.tickCounter % 100 == 0)
                    {
                        // Trigger update every 5 seconds if there are cooldowns, as clients that visits
                        // area may not have cooldowns loaded.
                        requiresUpdate = true;
                    }
                }
                else
                {
                    Set<String> allKeys = new HashSet<>(cooldowns.keySet());

                    for (String key : allKeys)
                    {
                        long time = cooldowns.get(key);

                        if (time < this.level.getGameTime())
                        {
                            // Remove expired cooldowns
                            cooldowns.remove(key);
                        }
                    }
                }

                this.getItemStack().update(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
                    StoredMobData.EMPTY,
                    data -> StoredMobData.of(data.animalCount(), data.properties(), cooldowns));

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

            if (!itemInHand.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
            {
                return false;
            }
            else
            {
                if (!player.level().isClientSide())
                {
                    this.inventory.addItem(itemInHand);
                    player.setItemInHand(interactionHand, ItemStack.EMPTY);
                    this.triggerUpdate();

                    if (this.level != null && !this.level.isClientSide())
                    {
                        // Trigger screen Update
                        NetworkManager.sendToPlayers(((ServerLevel) this.level).players().stream().
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

            if (!itemInHand.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
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

                // Set exact mob data
                itemInHand.set(AnimalPenDataComponentRegistry.MOB_COMPONENT.get(),
                    this.getItemStack().get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()));

                // Set mob count and compy cooldowns.
                itemInHand.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
                    new StoredMobData(newCount,
                        new HashMap<>(0),
                        AnimalPenItemHelper.getCooldowns(this.getItemStack())));

                this.triggerUpdate();

                AnimalPen.sendDebug("Half the amount of animals in pen");
                // Remove half of animals.
                return true;
            }
            else
            {
                Mob animal = this.getStoredAnimal().orElse(null);

                StoredMob storedMob = itemInHand.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());

                if ((animal == null) || !animal.getType().equals(storedMob.entityType()))
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

                long newCount = AnimalPenItemHelper.getMobCount(itemInHand);

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

                    itemInHand.update(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
                        StoredMobData.EMPTY,
                        data -> StoredMobData.of(1, data.properties(), data.cooldowns()));
                }
                else
                {
                    AnimalPenVariantHelper.mergeAnimalVariants(this.getItemStack(),
                        itemInHand,
                        this.level,
                        player,
                        errorSender);
                    itemInHand.remove(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
                    StoredMobData removedData = itemInHand.remove(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());

                    if (removedData != null && !removedData.cooldowns().isEmpty())
                    {
                        Map<String, Long> cooldowns = AnimalPenItemHelper.getCooldowns(this.getItemStack());

                        // Take the biggest cooldown from both
                        removedData.cooldowns().forEach((key, value) -> cooldowns.put(key,
                            Math.max(value, cooldowns.getOrDefault(key, 0L))));

                        this.getItemStack().update(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
                            StoredMobData.EMPTY,
                            data -> StoredMobData.of(data.animalCount(), data.properties(), cooldowns));
                    }

                    if (this.level != null && !this.level.isClientSide())
                    {
                        // Trigger screen Update
                        NetworkManager.sendToPlayers(((ServerLevel) this.level).players().stream().
                                filter(other ->
                                    other.blockPosition().distSqr(this.getBlockPos()) < 30).
                                toList(),
                            new UpdateVariantScreenData(this.getBlockPos(), this.getEntityVariants()));
                    }
                }

                player.setItemInHand(interactionHand, itemInHand);
                this.triggerUpdate();

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

                this.triggerUpdate();

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

        weapon.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

        this.deathTicker.add(0);

        if (this.getAnimalCount() <= 0)
        {
            ItemStack item = this.getItemStack();
            item.remove(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
            item.remove(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());
            item.remove(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get());

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

        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(animal.getLootTable());

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

        ItemStack mobItemStack = this.getItemStack();
        Optional<AnimalInteraction> interactionOptional =
            AnimalPenInteractionRegistry.matchInteraction(animal, mobItemStack, itemInHand);

        if (interactionOptional.isEmpty())
        {
            AnimalPen.sendDebug("Interaction with " + this.getBlockPos() + " cannot be success with " + itemInHand);
            return InteractionResult.FAILED;
        }

        if (!mobItemStack.has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()) ||
            !mobItemStack.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
        {
            AnimalPen.sendDebug("Interaction with " + this.getBlockPos() + " cannot be success as data is missing.");
            return InteractionResult.FAILED;
        }

        StoredMob storedMob = mobItemStack.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
        StoredMobData storedMobData = mobItemStack.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());

        AnimalInteraction interaction = interactionOptional.get();
        long animalCount = interaction.normalizeMobCount(storedMobData.animalCount());

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
            consumedAmount = (int) itemConsumer.calculateConsumption(executor,
                consumedItem,
                animalCount,
                interaction.even());
        }

        if (consumedAmount == 0)
        {
            AnimalPen.sendDebug("Need at least 1 items in stack");
            return InteractionResult.FAILED;
        }

        dataUpdate |= interaction.applyCooldown(mobItemStack, animalCount);

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
            mobItemStack,
            this.getBlockPos());

        if (dataUpdate)
        {
            AnimalPen.sendDebug("Animal Interaction finished");

            if (this.getAnimalCount() <= 0)
            {
                mobItemStack.remove(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
                mobItemStack.remove(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());
                mobItemStack.remove(AnimalPenDataComponentRegistry.MOB_VARIANT_COMPONENT.get());

                Block.popResource(serverLevel, this.getBlockPos().above(), mobItemStack);
                this.inventory.setItem(0, ItemStack.EMPTY);
                AnimalPen.sendDebug("Dropping empty cage");
            }
            else
            {
                AnimalPen.sendDebug("Updating cage tag");
                this.inventory.setChanged();
            }

            this.triggerUpdate();

            // Trigger update.
            return new InteractionResult(true, itemInHand);
        }

        return InteractionResult.FAILED;
    }


    /**
     * This method returns if client must receive cooldown update from server.
     * Only cooldowns with text entries should, as it is required for display.
     */
    private boolean requiresClientUpdate(Mob mob, String key)
    {
        return AnimalPenInteractionRegistry.getInteractions(mob).stream().
            filter(interaction -> key.equals(interaction.id())).
            anyMatch(interaction -> !interaction.textLines().isEmpty());
    }


    /**
     * This method trigger end functions for interaction with given key for given mob.
     */
    private void triggerEndFunctions(ServerLevel level, Mob mob, ItemStack componentHolder, String key)
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
                    componentHolder,
                    this.getBlockPos(),
                    function.key(),
                    function.value());
            });
    }


    /**
     * This method triggers start functions for interactions without interaction item. It only runs for interactions it
     * can fulfil conditions
     */
    private boolean triggerStartFunctions(ServerLevel level, Mob mob, ItemStack componentHolder, Map<String, Long> newCooldowns)
    {
        boolean anyChanges = false;

        // StreamAPI may cause some unneeded garbage collector calls.
        for (AnimalInteraction animalInteraction : AnimalPenInteractionRegistry.getInteractions(mob))
        {
            if (!newCooldowns.containsKey(animalInteraction.id()) &&
                animalInteraction.ingredient().isEmpty() &&
                animalInteraction.matchAllConditions(componentHolder))
            {
                anyChanges |= this.triggerStartFunction(level,
                    mob,
                    componentHolder,
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
        ItemStack componentHolder,
        Map<String, Long> newCooldowns,
        AnimalInteraction interaction)
    {
        boolean anyChanges = false;

        long animalCount = AnimalPenItemHelper.getMobCount(componentHolder);

        // Apply cooldown
        if (interaction.cooldown() != null)
        {
            long cooldownTime = interaction.cooldown().calculateCooldown(animalCount);
            newCooldowns.put(interaction.id(), cooldownTime);
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
                componentHolder,
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
        return AnimalPenVariantHelper.getAnimalVariants(this.getItemStack(), this.level).
            map(IndividualPenStorage::getVariants).
            orElse(new ListTag());
    }


    /**
     * This method sets new animal variant from given CompoundTag tag.
     *
     * @param index a new animal variant index
     */
    public void updateAnimalVariant(ServerPlayer player, int index)
    {
        if (this.getStoredAnimal().isEmpty())
        {
            return;
        }

        ListTag entityVariants = this.getEntityVariants();

        if (index >= entityVariants.size() || index < 0)
        {
            return;
        }

        CompoundTag animalVariant = entityVariants.getCompound(index);

        if (animalVariant.isEmpty())
        {
            // Nothing to update
            return;
        }

        this.getStoredAnimal().ifPresent(animal ->
        {
            AnimalPen.sendDebug("Animal Variant changed");

            // load new variant
            animal.load(animalVariant);

            AnimalPenCriteriaTriggersRegistry.ANIMAL_VARIANT_CHANGE_TRIGGER.get().trigger(player);

            // Update animal variant in item stack.
            ItemStack itemStack = this.getItemStack();

            StoredMob storedMob = itemStack.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
            itemStack.set(AnimalPenDataComponentRegistry.MOB_COMPONENT.get(),
                StoredMob.of(storedMob.entityType(), animalVariant));

            // Apply data
            this.triggerUpdate();

            NetworkManager.sendToPlayers(player.serverLevel().players().stream().
                    filter(other ->
                        other.blockPosition().distSqr(this.getBlockPos()) < 30).
                    toList(),
                new UpdateVariantScreenData(this.getBlockPos(), null));
        });
    }


    /**
     * This method removes animal pen variant with given index.
     *
     * @param index the variant index to be removed
     */
    public void removeAnimalVariant(int index)
    {
        ItemStack itemStack = this.getItemStack();

        if (itemStack.isEmpty())
        {
            return;
        }

        if (!(this.level instanceof ServerLevel serverLevel))
        {
            return;
        }

        if (!itemStack.has(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get()))
        {
            AnimalPen.sendDebug("Storage not set for item stack");
            return;
        }

        StoredMobVariantKey variantKey =
            itemStack.get(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());

        IndividualPenStorage storage = IndividualPenStorage.getOrCreate(serverLevel,
            variantKey.key());

        if (index < 0 || index >= storage.getVariants().size())
        {
            AnimalPen.sendDebug("Out of bounds variant deletion");
            return;
        }

        storage.getVariants().remove(index);
        storage.setDirty();

        if (storage.getVariants().isEmpty())
        {
            IndividualPenStorage.delete(serverLevel, variantKey.key());
            itemStack.remove(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get());
        }
        else
        {
            itemStack.set(AnimalPenDataComponentRegistry.MOB_VARIANT_KEY.get(),
                variantKey.withSize(storage.getVariants().size()));
        }

        // Trigger screen Update
        NetworkManager.sendToPlayers(serverLevel.players().stream().
            filter(other -> other.blockPosition().distSqr(this.getBlockPos()) < 30).toList(),
            new UpdateVariantScreenData(this.getBlockPos(), storage.getVariants()));

        AnimalPen.sendDebug("Animal variant removed");
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

        return AnimalPenItemHelper.getMobCount(this.getItemStack());
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

        if (!itemStack.has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()))
        {
            return false;
        }

        StoredMobData data = itemStack.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());
        long animalCount = data.animalCount();

        if (change < 0 && animalCount + change < 0)
        {
            return false;
        }

        long maxCount = AnimalPen.config().getMaximalAnimalCountNormalized();

        if (animalCount + change > maxCount)
        {
            return false;
        }

        itemStack.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
            StoredMobData.of(animalCount + change, data.properties(), data.cooldowns()));

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
        if (this.level == null)
        {
            return Optional.ofNullable(this.storedAnimal);
        }
        else
        {
            ItemStack itemStack = this.getItemStack();

            if (this.storedAnimal == null && !itemStack.isEmpty())
            {
                StoredMob customData = itemStack.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());

                if (customData == null)
                {
                    return Optional.ofNullable(this.storedAnimal);
                }

                StoredMob storedMob = itemStack.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
                Entity entity = storedMob.entityType().create(this.level);

                if (entity instanceof Mob mob)
                {
                    mob.load(storedMob.tag());
                    this.storedAnimal = mob;
                }
            }
            else if (this.storedAnimal != null && itemStack.isEmpty())
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

        long animalCount;
        Map<String, Long> cooldowns;
        Map<String, Integer> properties;

        if (this.getItemStack().has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()))
        {
            StoredMobData storedMobData =
                this.getItemStack().get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());

            animalCount = storedMobData.animalCount();
            cooldowns = storedMobData.cooldowns();
            properties = storedMobData.properties();
        }
        else
        {
            // Should not run into
            animalCount = 0L;
            cooldowns = Collections.emptyMap();
            properties = Collections.emptyMap();
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
                        boolean matchConditions = animalInteraction.matchAllConditions(this.getItemStack());

                        animalInteraction.textLines().forEach(line ->
                        {
                            boolean containsInCooldown = cooldowns.containsKey(animalInteraction.id());
                            boolean shouldExecute = switch (line.visibility())
                            {
                                case READY -> matchConditions && !containsInCooldown;
                                case COOLDOWN -> matchConditions && containsInCooldown;
                                case NOT_MATCH -> !matchConditions;
                                case ON_MATCH -> matchConditions;
                            };

                            if (shouldExecute)
                            {
//                                long reminingTime = cooldowns.getOrDefault(animalInteraction.id(), 0L);

                                long reminingTime =
                                    cooldowns.getOrDefault(animalInteraction.id(), this.level.getGameTime()) -
                                    this.level.getGameTime();

                                Pair<ItemStack[], Component> componentPair =
                                    line.animalPenGetLine(animalInteraction.ingredient(),
                                        properties,
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

            Map<String, Long> cooldowns = AnimalPenItemHelper.getCooldowns(this.getItemStack());

            int returnValue = 1;

            for (AnimalInteraction interaction : interactions)
            {
                if (interaction.redstoneSignal() > 0 &&
                    interaction.redstoneSignal() < 4 &&
                    interaction.matchAllConditions(this.getItemStack()) &&
                    !cooldowns.containsKey(interaction.id()))
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
        public void fromTag(ListTag listTag, HolderLookup.Provider provider)
        {
            this.clearContent();

            for(int i = 0; i < listTag.size(); ++i)
            {
                CompoundTag tag = listTag.getCompound(i);
                ItemStack.parse(provider, tag).ifPresent(itemStack ->
                {
                    if (tag.contains("tag"))
                    {
                        // Upgrade from < 1.20.5 versions to new data.
                        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.getCompound("tag")));
                    }

                    this.addItem(itemStack);
                });
            }
        }


        @Override
        public void setChanged()
        {
            super.setChanged();
        }
    };

    private long displaySize = -1;

    private UUID ownerUUID;

    private long protectedAmount = 0;

    private int tickCounter;
}
