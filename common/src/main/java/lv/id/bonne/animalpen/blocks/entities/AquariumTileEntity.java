//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import com.mojang.serialization.DataResult;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AquariumBlock;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import lv.id.bonne.animalpen.items.AnimalContainerItem;
import lv.id.bonne.animalpen.network.packets.UpdateVariantScreenData;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.entity.LivingEntity.getSlotForHand;


public class AquariumTileEntity extends BlockEntity implements AnimalPenBlockInterface<PathfinderMob>
{
    public AquariumTileEntity(
        BlockPos blockPos,
        BlockState blockState)
    {
        super(AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get(), blockPos, blockState);
    }


    @Override
    public void saveAdditional(ValueOutput valueOutput)
    {
        super.saveAdditional(valueOutput);

        ContainerHelper.saveAllItems(valueOutput, this.inventory.getItems(), true);

        if (!this.deathTicker.isEmpty())
        {
            valueOutput.putIntArray(TAG_DEATH_TICKER, this.deathTicker.stream().mapToInt(i->i).toArray());
        }

        valueOutput.putLong(TAG_DISPLAY_SIZE, this.displaySize);

        valueOutput.storeNullable(TAG_OWNER_UUID, UUIDUtil.CODEC, this.ownerUUID);

        valueOutput.putLong(TAG_KEEP_AMOUNT, this.protectedAmount);
    }


    @Override
    protected void loadAdditional(ValueInput valueInput)
    {
        super.loadAdditional(valueInput);

        this.inventory.clearContent();
        this.deathTicker.clear();
        this.storedAnimal = null;
        this.ownerUUID = null;

        ContainerHelper.loadAllItems(valueInput, this.inventory.getItems());

        valueInput.getIntArray(TAG_DEATH_TICKER).ifPresent(deaths -> {
            for (int death : deaths)
            {
                this.deathTicker.add(death);
            }
        });

        this.displaySize = valueInput.getLongOr(TAG_DISPLAY_SIZE, -1);

        DataResult<UUID> parse = UUIDUtil.CODEC.parse(NbtOps.INSTANCE, valueInput.get(TAG_OWNER_UUID));
        parse.ifSuccess(uuid -> this.ownerUUID = uuid);

        this.protectedAmount = valueInput.getLongOr(TAG_KEEP_AMOUNT, 0);
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
     * This method returns stored animal for block entity.
     *
     * @return Animal instance stored in block entity.
     */
    @Override
    public Optional<PathfinderMob> getStoredAnimal()
    {
        if (this.storedAnimal == null && !this.getItemStack().isEmpty())
        {
            CustomData customData = this.getItemStack().get(DataComponents.ENTITY_DATA);

            if (customData == null)
            {
                return Optional.ofNullable(this.storedAnimal);
            }

            CompoundTag tag = customData.copyTag();

            if (!tag.contains(AnimalContainerItem.TAG_ENTITY_ID) || this.level == null)
            {
                return Optional.ofNullable(this.storedAnimal);
            }

            try (ProblemReporter.ScopedCollector scopedCollector =
                     new ProblemReporter.ScopedCollector(this.problemPath(), AnimalPen.LOGGER))
            {
                ValueInput valueInput = TagValueInput.create(scopedCollector,
                    this.level.registryAccess(),
                    tag);

                EntityType.create(valueInput, this.level, EntitySpawnReason.TRIGGERED).
                    map(entity -> (PathfinderMob) entity).
                    ifPresent(animal -> this.storedAnimal = animal);
            }
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

            if (!itemInHand.has(DataComponents.ENTITY_DATA))
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
                        NetworkManager.sendToPlayers(((ServerLevel) this.level).players().stream().
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

            if (!itemInHand.has(DataComponents.ENTITY_DATA))
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

                LivingEntity animal = this.getStoredAnimal().orElse(null);
                
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

                CompoundTag tag;

                try (ProblemReporter.ScopedCollector scopedCollector =
                         new ProblemReporter.ScopedCollector(this.problemPath(), AnimalPen.LOGGER))
                {
                    TagValueOutput valueOutput =
                        TagValueOutput.createWithContext(scopedCollector, animal.registryAccess());
                    animal.save(valueOutput);
                    tag = valueOutput.buildResult();
                }

                tag.putLong(AnimalContainerItem.TAG_AMOUNT, newCount);
                itemInHand.set(DataComponents.ENTITY_DATA, CustomData.of(tag));

                player.setItemInHand(interactionHand, itemInHand);
                this.inventory.setChanged();

                AnimalPen.sendDebug("Half the amount of animals in pen");
                // Remove half of animals.
                return true;
            }
            else
            {
                Mob animal = this.getStoredAnimal().orElse(null);
                CompoundTag itemInHandTag = itemInHand.get(DataComponents.ENTITY_DATA).copyTag();

                if (animal == null ||
                    !itemInHandTag.getString(AnimalContainerItem.TAG_ENTITY_ID).orElse("").
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

                long newCount = itemInHandTag.getLongOr(AnimalContainerItem.TAG_AMOUNT, 0);

                if (newCount <= 0 || !((AnimalPenInterface) animal).animalPenUpdateCount(newCount))
                {
                    AnimalPen.sendDebug("Failed to deposit animal");
                    return false;
                }

                // Handle animal variants
                if (newCount > 1 &&
                    !AnimalContainerItem.canMergeAnimalVariants(this.getItemStack(), itemInHand, player))
                {
                    AnimalPen.sendDebug("Variants could not be merged");

                    ((AnimalPenInterface) animal).animalPenUpdateCount(-1);
                    itemInHandTag.putLong(AnimalContainerItem.TAG_AMOUNT, 1);
                    itemInHand.set(DataComponents.ENTITY_DATA, CustomData.of(itemInHandTag));
                }
                else
                {
                    AnimalContainerItem.mergeAnimalVariants(this.getItemStack(), itemInHand, player);
                    itemInHand.remove(DataComponents.ENTITY_DATA);
                    itemInHand.remove(AnimalPenDataComponentRegistry.ENTITY_VARIANTS.get());

                    if (this.level != null && !this.level.isClientSide())
                    {
                        // Trigger screen Update
                        NetworkManager.sendToPlayers(((ServerLevel) this.level).players().stream().
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
     * This method processes player interaction with pen tile entity with any item in mian hand.
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

        Mob animal = this.getStoredAnimal().orElse(null);

        if (animal == null)
        {
            AnimalPen.sendDebug("Animal is not set");
            return false;
        }

        if (((AnimalPenInterface) animal).animalPenInteract(player, interactionHand, this.getBlockPos()))
        {
            AnimalPen.sendDebug("Animal Interaction finished");

            ItemStack item = this.getItemStack();

            // Reset tag, as some animals may need it.
            CompoundTag tag;

            try (ProblemReporter.ScopedCollector scopedCollector =
                     new ProblemReporter.ScopedCollector(this.problemPath(), AnimalPen.LOGGER))
            {
                TagValueOutput valueOutput =
                    TagValueOutput.createWithContext(scopedCollector, animal.registryAccess());
                animal.save(valueOutput);
                tag = valueOutput.buildResult();
            }

            item.set(DataComponents.ENTITY_DATA, CustomData.of(tag));

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

        Mob animal = this.getStoredAnimal().orElse(null);

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

        EnchantmentHelper.doPostAttackEffectsWithItemSource((ServerLevel) level,
            animal,
            level.damageSources().playerAttack(player),
            weapon);

        if (AnimalPen.config().isIncreaseStatistics())
        {
            player.awardStat(Stats.ITEM_USED.get(weapon.getItem()));
        }

        weapon.hurtAndBreak(1, player, getSlotForHand(InteractionHand.MAIN_HAND));

        this.deathTicker.add(0);

        if (((AnimalPenInterface) animal).animalPenGetCount() <= 0)
        {
            ItemStack item = this.getItemStack();
            item.remove(DataComponents.ENTITY_DATA);
            item.remove(AnimalPenDataComponentRegistry.ENTITY_VARIANTS.get());

            Block.popResource(level, this.getBlockPos().above(), item);
            this.inventory.setItem(0, ItemStack.EMPTY);
            AnimalPen.sendDebug("Dropping empty cage");
        }

        this.triggerUpdate();

        Vec3 position = new Vec3(this.worldPosition.getX(),
            this.worldPosition.getY(),
            this.worldPosition.getZ());

        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(animal.getLootTable().get());

        LootParams.Builder paramsBuilder = new LootParams.Builder((ServerLevel) level).
            withParameter(LootContextParams.ORIGIN, position).
            withParameter(LootContextParams.THIS_ENTITY, animal).
            withParameter(LootContextParams.ATTACKING_ENTITY, player).
            withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player).
            withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player).
            withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player)).
            withLuck(player.getLuck());

        lootTable.getRandomItems(paramsBuilder.create(LootContextParamSets.ENTITY), level.getRandom().nextLong()).
            forEach(itemStack -> Block.popResource(level, this.getBlockPos().above(), itemStack));

        animal.clearFire();

        int reward = animal.getExperienceReward((ServerLevel) level, player);
        ExperienceOrb.award((ServerLevel)this.level, position.add(0.5, 1.5, 0.5), reward);

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
            animal ->         {
                CompoundTag tag;

            try (ProblemReporter.ScopedCollector scopedCollector =
                     new ProblemReporter.ScopedCollector(this.problemPath(), AnimalPen.LOGGER))
            {
                TagValueOutput valueOutput =
                    TagValueOutput.createWithContext(scopedCollector, animal.registryAccess());
                animal.save(valueOutput);
                tag = valueOutput.buildResult();
            }

                this.getItemStack().set(DataComponents.ENTITY_DATA, CustomData.of(tag));
            });

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


    @Override
    public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState)
    {
        if (this.level != null)
        {
            Containers.dropContents(this.level, blockPos, this.inventory.getItems());
        }
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
        return this.getStoredAnimal().
            map(animal -> AnimalContainerItem.getAnimalVariants(this.getItemStack()).orElseGet(ListTag::new)).
            orElseGet(ListTag::new);
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
    public void updateAnimalVariant(ValueInput animalVariant)
    {
        if (this.getStoredAnimal().isEmpty() || animalVariant == null)
        {
            // Nothing to update
            return;
        }

        this.getStoredAnimal().ifPresent(animal ->
        {
            AnimalPen.sendDebug("Animal Variant changed");

            // Save extra data
            CompoundTag extraData;

        try (ProblemReporter.ScopedCollector scopedCollector =
                 new ProblemReporter.ScopedCollector(this.problemPath(), AnimalPen.LOGGER))
        {
            TagValueOutput valueOutput =
                TagValueOutput.createWithContext(scopedCollector, this.level.registryAccess());
            ((AnimalPenInterface) animal).animalPenSaveTag(valueOutput);

            extraData = valueOutput.buildResult();

            // load new variant
            animal.load(animalVariant);

            ValueInput valueInput =
                TagValueInput.create(scopedCollector, this.level.registryAccess(), extraData);

            // Apply data
            ((AnimalPenInterface) animal).animalPenLoadTag(valueInput);
        }

        this.triggerUpdate();

            if (this.level != null && !this.level.isClientSide())
            {
                NetworkManager.sendToPlayers(((ServerLevel) this.level).players().stream().
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
     * @param index the variant index to be removed
     */
    @Override
    public void removeAnimalVariant(int index)
    {
        if (this.getStoredAnimal().isEmpty() || this.getEntityVariants().size() <= index)
        {
            return;
        }

        CustomData customData = this.getItemStack().get(AnimalPenDataComponentRegistry.ENTITY_VARIANTS.get());

        if (customData == null)
        {
            return;
        }

        this.getItemStack().set(AnimalPenDataComponentRegistry.ENTITY_VARIANTS.get(),
            customData.update(tag ->
                tag.getList(AnimalContainerItem.TAG_VARIANTS).ifPresent(list -> list.remove(index)))
        );

        this.inventory.setChanged();

        if (this.level != null && !this.level.isClientSide())
        {
            NetworkManager.sendToPlayers(((ServerLevel) this.level).players().stream().
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
        return AnimalPen.config().isGrowWaterAnimals();
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
            return stack.is(AnimalPensItemRegistry.ANIMAL_CONTAINER.get());
        }


        @Override
        public void setChanged()
        {
            super.setChanged();
            AquariumTileEntity.this.triggerUpdate();
        }
    };


    private PathfinderMob storedAnimal;

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
