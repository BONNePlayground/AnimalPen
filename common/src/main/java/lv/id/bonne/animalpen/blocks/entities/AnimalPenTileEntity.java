//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

import lv.id.bonne.animalpen.items.AnimalContainerItem;
import lv.id.bonne.animalpen.mixin.AnimalInvoker;
import lv.id.bonne.animalpen.mixin.MobInvoker;
import lv.id.bonne.animalpen.mixin.MushroomCowAccessor;
import lv.id.bonne.animalpen.mixin.SheepAccessor;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;


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

        this.cooldowns.clear();
        this.inventory.clearContent();
        this.storedAnimal = null;

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
     * @return Animal instance stored in block entity.
     */
    public Animal getStoredAnimal()
    {
        if (this.storedAnimal == null && !this.inventory.getItem(0).isEmpty())
        {
            AnimalContainerItem.getStoredAnimal(this.level, this.inventory.getItem(0)).
                ifPresent(animal -> this.storedAnimal = animal);
        }
        else if (this.storedAnimal != null && this.inventory.getItem(0).isEmpty())
        {
            this.storedAnimal = null;
        }

        return this.storedAnimal;
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
            this.triggerUpdate();
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

                this.inventory.setChanged();

                return true;
            }
        }
        else
        {
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

                if (!this.updateStoredAnimal(this.getStoredAnimal(), -newCount))
                {
                    return false;
                }

                AnimalContainerItem.setStoredAnimal(itemInHand, this.getStoredAnimal(), newCount);
                player.setItemInHand(interactionHand, itemInHand);

                this.inventory.setChanged();

                // Remove half of animals.
                return true;
            }
            else
            {
                if (this.getStoredAnimal().getType() != storedAnimal.get().getType())
                {
                    // Cannot do with different animal types.
                    return false;
                }

                long newCount = AnimalContainerItem.getStoredAnimalAmount(itemInHand);

                if (!this.updateStoredAnimal(this.getStoredAnimal(), newCount))
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
     * @param itemInHand the item in hand
     * @return {@code true} if interaction was successful, {@code false} otherwise.
     */
    public boolean interactWithPen(Player player, InteractionHand interactionHand, ItemStack itemInHand)
    {
        if (itemInHand.isEmpty() && !this.inventory.isEmpty())
        {
            player.setItemInHand(interactionHand, this.inventory.getItem(0));
            this.inventory.setItem(0, ItemStack.EMPTY);
            this.triggerUpdate();

            return true;
        }

        int count = itemInHand.getCount();
        long animalCount = AnimalContainerItem.getStoredAnimalAmount(this.inventory.getItem(0));

        Animal animal = this.getStoredAnimal();

        if (animal == null)
        {
            return false;
        }

        Random random = this.level.getRandom();

        if (animal.isFood(itemInHand))
        {
            if (count < 2)
            {
                // Cannot feed 1 animal only for breeding.
                return false;
            }

            count = (int) Math.min(animalCount, count);

            if (!this.updateStoredAnimal(animal, count / 2))
            {
                // Could not change the count.
                return false;
            }

            if (!player.getAbilities().instabuild)
            {
                if (count % 2 == 1)
                {
                    itemInHand.shrink(count - 1);
                    player.setItemInHand(interactionHand, itemInHand);
                }
                else
                {
                    itemInHand.shrink(count);
                    player.setItemInHand(interactionHand, itemInHand);
                }
            }

            ((ServerLevel) level).sendParticles(
                ParticleTypes.HEART,
                this.worldPosition.getX() + 0.5f,
                this.worldPosition.getY() + 1.5,
                this.worldPosition.getZ() + 0.5f,
                5,
                0.2, 0.2, 0.2,
                0.05);

            this.level.playSound(null,
                this.getBlockPos(),
                animal.getEatingSound(itemInHand),
                SoundSource.NEUTRAL,
                1.0F,
                Mth.randomBetween(this.level.random, 0.8F, 1.2F));

            SoundEvent soundEvent = ((MobInvoker) animal).invokeGetAmbientSound();

            if (soundEvent != null)
            {
                this.level.playSound(null,
                    this.getBlockPos(),
                    soundEvent,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F);
            }

            return true;
        }
        else if (itemInHand.is(Items.BUCKET) && (animal instanceof Cow || animal instanceof Goat))
        {
            ItemStack itemStack2 = ItemUtils.createFilledResult(itemInHand, player, Items.MILK_BUCKET.getDefaultInstance());
            player.setItemInHand(interactionHand, itemStack2);

            this.level.playSound(null,
                this.getBlockPos(),
                animal instanceof Cow ? SoundEvents.COW_MILK : SoundEvents.GOAT_MILK,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            return true;
        }
        else if (itemInHand.is(Items.SHEARS) && animal instanceof Sheep sheep)
        {
            itemInHand.hurtAndBreak(1, player, (playerx) -> playerx.broadcastBreakEvent(interactionHand));

            ItemLike itemLike = ((SheepAccessor) sheep).getITEM_BY_DYE().get(sheep.getColor());

            int woolCount = 1;

            for (int i = 0; i < animalCount && woolCount < 64 * 5; i++)
            {
                woolCount += random.nextInt(3);
            }

            while (woolCount > 0)
            {
                ItemStack woolStack = new ItemStack(itemLike);

                if (woolCount > 64)
                {
                    woolStack.setCount(64);
                    woolCount -= 64;
                }
                else
                {
                    woolStack.setCount(woolCount);
                    woolCount = 0;
                }

                Block.popResource(this.level, this.getBlockPos().above(), woolStack);
            }

            this.level.playSound(null,
                this.getBlockPos(),
                SoundEvents.SHEEP_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            return true;
        }
        else if (itemInHand.is(Items.SHEARS) && animal instanceof Bee)
        {
            int pollenCount = 5;

            if (pollenCount != 5)
            {
                return false;
            }

            pollenCount = 0;

            itemInHand.hurtAndBreak(1, player, (playerx) -> playerx.broadcastBreakEvent(interactionHand));
            Block.popResource(this.level, this.getBlockPos().above(), new ItemStack(Items.HONEYCOMB, 3));

            this.level.playSound(null,
                this.getBlockPos(),
                SoundEvents.BEEHIVE_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            return true;
        }
        else if (itemInHand.is(Items.GLASS_BOTTLE) && animal instanceof Bee)
        {
            int pollenCount = 5;

            if (pollenCount != 5)
            {
                return false;
            }

            pollenCount = 0;

            itemInHand.shrink(1);

            if (itemInHand.isEmpty())
            {
                player.setItemInHand(interactionHand, new ItemStack(Items.HONEY_BOTTLE));
            }
            else if (!player.getInventory().add(new ItemStack(Items.HONEY_BOTTLE)))
            {
                player.drop(new ItemStack(Items.HONEY_BOTTLE), false);
            }

            this.level.playSound(null,
                this.getBlockPos(),
                SoundEvents.BOTTLE_FILL,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            return true;
        }
        else if (itemInHand.is(Items.BUCKET) && animal instanceof Turtle)
        {
            int eggCount = 1;

            for (int i = 0; i < animalCount && eggCount < 64 * 5; i++)
            {
                eggCount += random.nextInt(4);
            }

            while (eggCount > 0)
            {
                ItemStack eggStack = new ItemStack(Items.TURTLE_EGG);

                if (eggCount > 64)
                {
                    eggStack.setCount(64);
                    eggCount -= 64;
                }
                else
                {
                    eggStack.setCount(eggCount);
                    eggCount = 0;
                }

                Block.popResource(this.level, this.getBlockPos().above(), eggStack);
            }

            this.level.playSound(null,
                this.getBlockPos(),
                SoundEvents.TURTLE_LAY_EGG,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            return true;
        }
        else if (itemInHand.is(Items.BUCKET) && animal instanceof Chicken)
        {
            int eggCount = (int) Math.min(animalCount, 16 * 5);

            while (eggCount > 0)
            {
                ItemStack eggStack = new ItemStack(Items.EGG);

                if (eggCount > 16)
                {
                    eggStack.setCount(16);
                    eggCount -= 16;
                }
                else
                {
                    eggStack.setCount(eggCount);
                    eggCount = 0;
                }

                Block.popResource(this.level, this.getBlockPos().above(), eggStack);
            }

            this.level.playSound(null,
                this.getBlockPos(),
                SoundEvents.CHICKEN_EGG,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            return true;
        }
        else if (itemInHand.is(Items.BOWL) && animal instanceof MushroomCow cow)
        {
            MushroomCowAccessor cowAccessor = (MushroomCowAccessor) cow;

            ItemStack bowlStack;
            boolean suspicious = false;

            if (cowAccessor.getEffect() != null)
            {
                suspicious = true;
                bowlStack = new ItemStack(Items.SUSPICIOUS_STEW);
                SuspiciousStewItem.saveMobEffect(bowlStack, cowAccessor.getEffect(), cowAccessor.getEffectDuration());
                cowAccessor.setEffect(null);
                cowAccessor.setEffectDuration(0);

                if (!this.updateStoredAnimal(animal))
                {
                    return false;
                }
            }
            else
            {
                bowlStack = new ItemStack(Items.MUSHROOM_STEW);
            }

            ItemStack remainingStack = ItemUtils.createFilledResult(itemInHand, player, bowlStack, false);
            player.setItemInHand(interactionHand, remainingStack);
            SoundEvent soundEvent;

            if (suspicious)
            {
                soundEvent = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
            }
            else
            {
                soundEvent = SoundEvents.MOOSHROOM_MILK;
            }

            this.level.playSound(null,
                this.getBlockPos(),
                soundEvent,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            return true;
        }
        else if (itemInHand.is(ItemTags.SMALL_FLOWERS) && animal instanceof MushroomCow cow &&
            cow.getMushroomType() == MushroomCow.MushroomType.BROWN)
        {
            MushroomCowAccessor cowAccessor = (MushroomCowAccessor) cow;

            if (cowAccessor.getEffect() != null)
            {
                ((ServerLevel) level).sendParticles(
                    ParticleTypes.SMOKE,
                    this.worldPosition.getX() + 0.5f,
                    this.worldPosition.getY() + 1.5,
                    this.worldPosition.getZ() + 0.5f,
                    2,
                    0.2, 0.2, 0.2,
                    0.05);
            }
            else
            {
                Optional<Pair<MobEffect, Integer>> optional = cowAccessor.invokeGetEffectFromItemStack(itemInHand);

                if (optional.isEmpty())
                {
                    return false;
                }

                Pair<MobEffect, Integer> pair = optional.get();

                ((ServerLevel) level).sendParticles(
                    ParticleTypes.EFFECT,
                    this.worldPosition.getX() + 0.5f,
                    this.worldPosition.getY() + 1.5,
                    this.worldPosition.getZ() + 0.5f,
                    4,
                    0.2, 0.2, 0.2,
                    0.05);

                cowAccessor.setEffect(pair.getLeft());
                cowAccessor.setEffectDuration(pair.getRight());

                if (!this.updateStoredAnimal(animal))
                {
                    return false;
                }

                if (!player.getAbilities().instabuild)
                {
                    itemInHand.shrink(1);
                    player.setItemInHand(interactionHand, itemInHand);
                }

                this.level.playSound(null,
                    this.getBlockPos(),
                    SoundEvents.MOOSHROOM_EAT,
                    SoundSource.NEUTRAL,
                    2.0F,
                    1.0F);

                return true;
            }
        }
        else if (itemInHand.getItem() instanceof DyeItem dye && animal instanceof Sheep sheep)
        {
            sheep.setColor(dye.getDyeColor());

            if (!this.updateStoredAnimal(animal))
            {
                return false;
            }

            itemInHand.shrink(1);
            player.setItemInHand(interactionHand, itemInHand);

            this.level.playSound(null,
                this.getBlockPos(),
                SoundEvents.DYE_USE,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

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

        Animal animal = this.getStoredAnimal();

        if (animal == null)
        {
            return;
        }

        if (!this.updateStoredAnimal(animal, -1))
        {
            return;
        }

        if (AnimalContainerItem.getStoredAnimalAmount(this.inventory.getItem(0)) <= 0)
        {
            Block.popResource(level, this.getBlockPos().above(), this.inventory.getItem(0));
            this.inventory.setItem(0, ItemStack.EMPTY);
            this.triggerUpdate();
        }

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

        int reward = ((AnimalInvoker) animal).invokeGetExperienceReward(player);
        ExperienceOrb.award((ServerLevel)this.level, position, reward);
    }


    private boolean updateStoredAnimal(Animal animal)
    {
        if (AnimalContainerItem.setStoredAnimal(this.inventory.getItem(0), animal))
        {
            this.storedAnimal = null;
            this.triggerUpdate();

            return true;
        }

        return false;
    }


    private boolean updateStoredAnimal(Animal animal, long animalCount)
    {
        if (AnimalContainerItem.increaseAnimalCount(this.inventory.getItem(0), animal, animalCount))
        {
            this.storedAnimal = null;
            this.triggerUpdate();

            return true;
        }

        return false;
    }


    private void triggerUpdate()
    {
        this.setChanged();

        if (this.level != null && !this.level.isClientSide())
        {
            this.level.sendBlockUpdated(this.getBlockPos(),
                this.getBlockState(),
                this.getBlockState(),
                Block.UPDATE_CLIENTS);
        }
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
            AnimalPenTileEntity.this.triggerUpdate();
        }
    };


    private Animal storedAnimal;

    private int tickCounter;

    private final Map<String, Integer> cooldowns = new HashMap<>();

    public static final String TAG_COOLDOWNS = "Cooldowns";

    public static final String TAG_INVENTORY = "Inventory";
}
