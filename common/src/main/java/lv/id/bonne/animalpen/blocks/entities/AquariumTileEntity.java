//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


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
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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


public class AquariumTileEntity extends BlockEntity implements AnimalPenBlockInterface<LivingEntity>
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
    }


    @Override
    protected void loadAdditional(ValueInput valueInput)
    {
        super.loadAdditional(valueInput);

        this.inventory.clearContent();
        this.deathTicker.clear();
        this.storedAnimal = null;

        ContainerHelper.loadAllItems(valueInput, this.inventory.getItems());

        valueInput.getIntArray(TAG_DEATH_TICKER).ifPresent(deaths -> {
            for (int death : deaths)
            {
                this.deathTicker.add(death);
            }
        });

        this.displaySize = valueInput.getLongOr(TAG_DISPLAY_SIZE, -1);
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
    public Mob getStoredAnimal()
    {
        if (this.storedAnimal == null && !this.getItemStack().isEmpty())
        {
            CustomData customData = this.getItemStack().get(DataComponents.ENTITY_DATA);

            if (customData == null)
            {
                return this.storedAnimal;
            }

            CompoundTag tag = customData.copyTag();

            if (!tag.contains(AnimalContainerItem.TAG_ENTITY_ID) || this.level == null)
            {
                return this.storedAnimal;
            }

            try (ProblemReporter.ScopedCollector scopedCollector =
                     new ProblemReporter.ScopedCollector(this.problemPath(), AnimalPen.LOGGER))
            {
                ValueInput valueInput = TagValueInput.create(scopedCollector,
                    this.level.registryAccess(),
                    tag);

                EntityType.create(valueInput, this.level, EntitySpawnReason.TRIGGERED).
                    map(entity -> (Mob) entity).
                    ifPresent(animal -> this.storedAnimal = animal);
            }
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

        Mob animal = this.getStoredAnimal();

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

                Mob animal = this.getStoredAnimal();
                
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

                // Remove half of animals.
                return true;
            }
            else
            {
                Mob animal = this.getStoredAnimal();
                CompoundTag itemInHandTag = itemInHand.get(DataComponents.ENTITY_DATA).copyTag();

                if (animal == null ||
                    !itemInHandTag.getString(AnimalContainerItem.TAG_ENTITY_ID).orElse("").
                        equals(animal.getType().arch$registryName().toString()))
                {
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
                    return false;
                }

                // Handle animal variants
                if (newCount > 1 &&
                    !AnimalContainerItem.canMergeAnimalVariants(this.getItemStack(), itemInHand, player))
                {
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
            if (player.isCrouching() && !player.level().isClientSide())
            {
                ItemStack item = this.getItemStack();
                player.setItemInHand(interactionHand, item);
                this.inventory.setItem(0, ItemStack.EMPTY);
                this.inventory.setChanged();
            }

            return true;
        }

        Mob animal = this.getStoredAnimal();

        if (animal == null)
        {
            return false;
        }

        if (((AnimalPenInterface) animal).animalPenInteract(player, interactionHand, this.getBlockPos()))
        {
            ItemStack item = this.getItemStack();

            Optional<ListTag> optionalVariants = AnimalContainerItem.getAnimalVariants(item);

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

            // Restore animal variants
            optionalVariants.ifPresent(variants -> tag.put(AnimalContainerItem.TAG_VARIANTS, variants));

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
        ItemStack weapon = player.getItemInHand(InteractionHand.MAIN_HAND);

        Mob animal = this.getStoredAnimal();

        if (animal == null)
        {
            return;
        }

        if (!((AnimalPenInterface) animal).animalPenUpdateCount(-1))
        {
            return;
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

        int reward = animal.getExperienceReward((ServerLevel) level, player);
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


    public void triggerUpdate()
    {
        this.setChanged();

        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        Mob animal = this.getStoredAnimal();

        if (animal != null)
        {
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
    public void updateAnimalVariant(ValueInput animalVariant)
    {
        if (this.getStoredAnimal() == null || animalVariant == null)
        {
            return;
        }

        // Save extra data
        CompoundTag extraData;

        try (ProblemReporter.ScopedCollector scopedCollector =
                 new ProblemReporter.ScopedCollector(this.problemPath(), AnimalPen.LOGGER))
        {
            TagValueOutput valueOutput =
                TagValueOutput.createWithContext(scopedCollector, this.level.registryAccess());
            ((AnimalPenInterface) this.storedAnimal).animalPenSaveTag(valueOutput);

            extraData = valueOutput.buildResult();

            // load new variant
            this.storedAnimal.load(animalVariant);

            ValueInput valueInput =
                TagValueInput.create(scopedCollector, this.level.registryAccess(), extraData);

            // Apply data
            ((AnimalPenInterface) this.storedAnimal).animalPenLoadTag(valueInput);
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


    private Mob storedAnimal;

    private long displaySize = -1;

    private int tickCounter;

    private final List<Integer> deathTicker = new ArrayList<>();

    public static final String TAG_INVENTORY = "inventory";

    public static final String TAG_DEATH_TICKER = "death_ticker";

    public static final String TAG_DISPLAY_SIZE = "display_size";
}
