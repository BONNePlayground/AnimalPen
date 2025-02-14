//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.water;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;


@Mixin(WaterAnimal.class)
@Implements(@Interface(iface = AnimalPenInterface.class, prefix = "animalPen$", unique = true))
public abstract class AnimalPenWaterAnimal extends Mob
{
    protected AnimalPenWaterAnimal(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag)
    {
        super.addAdditionalSaveData(compoundTag);
        this.animalPen$animalPenSaveTag(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag)
    {
        super.readAdditionalSaveData(compoundTag);
        this.animalPen$animalPenLoadTag(compoundTag);
    }


    @Intrinsic
    public boolean animalPen$animalPenUpdateCount(long change)
    {
        if (change < 0 && this.animalCount + change < 0)
        {
            return false;
        }

        long maxCount = AnimalPen.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

        if (maxCount > 0 && this.animalCount + change > maxCount)
        {
            return false;
        }

        this.animalCount += change;
        return true;
    }


    @Intrinsic
    public long animalPen$animalPenGetCount()
    {
        return this.animalCount;
    }


    @Intrinsic
    public boolean animalPen$animalPenTick()
    {
        if (this.foodCooldown > 0)
        {
            this.foodCooldown--;
            return true;
        }

        return false;
    }


    @Intrinsic
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        tag.putInt("food_cooldown", this.foodCooldown);
        tag.putLong("animal_count", this.animalCount);
    }


    @Intrinsic
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        if (tag.contains("food_cooldown", Tag.TAG_INT))
        {
            this.foodCooldown = tag.getInt("food_cooldown");
        }

        if (tag.contains("animal_count", Tag.TAG_LONG))
        {
            this.animalCount = tag.getLong("animal_count");
        }
    }


    @Intrinsic
    public boolean animalPen$animalPenInteract(Player player, InteractionHand hand, BlockPos position)
    {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.animal$isFood(itemStack))
        {
            if (this.foodCooldown > 0)
            {
                return false;
            }

            long maxCount = AnimalPen.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

            if (maxCount > 0 && this.animalCount >= maxCount)
            {
                return false;
            }

            int stackSize = itemStack.getCount();
            stackSize = (int) Math.min(this.animalCount, stackSize);

            if (stackSize < 2)
            {
                // Cannot feed 1 animal only for breeding.
                return false;
            }

            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            stackSize = (int) Math.min((maxCount - this.animalCount) * 2, stackSize);

            if (!player.getAbilities().instabuild)
            {
                if (stackSize % 2 == 1)
                {
                    itemStack.shrink(stackSize - 1);
                    player.setItemInHand(hand, itemStack);
                }
                else
                {
                    itemStack.shrink(stackSize);
                    player.setItemInHand(hand, itemStack);
                }
            }

            this.animalCount += stackSize / 2;

            if (player.getLevel() instanceof ServerLevel serverLevel)
            {
                serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    position.getX() + 0.5f,
                    position.getY() + 1.5,
                    position.getZ() + 0.5f,
                    5,
                    0.2, 0.2, 0.2,
                    0.05);
            }

            player.getLevel().playSound(null,
                position,
                this.getEatingSound(itemStack),
                SoundSource.NEUTRAL,
                1.0F,
                Mth.randomBetween(player.getLevel().random, 0.8F, 1.2F));

            SoundEvent soundEvent = this.getAmbientSound();

            if (soundEvent != null)
            {
                player.getLevel().playSound(null,
                    position,
                    soundEvent,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F);
            }

            this.foodCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.APPLE,
                this.animalCount);

            return true;
        }

        return false;
    }


    @Intrinsic
    public List<Pair<ItemStack, Component>> animalPen$animalPenGetLines(int tick)
    {
        List<Pair<ItemStack, Component>> lines = new LinkedList<>();

        if (AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
            this.getType(),
            Items.APPLE,
            this.animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component = new TextComponent("");

        if (this.foodCooldown == 0)
        {
            component.append(new TranslatableComponent("display.animal_pen.food_ready").
                withStyle(ChatFormatting.GREEN));
        }
        else
        {
            component.append(new TranslatableComponent("display.animal_pen.food_cooldown",
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.foodCooldown / 20).format(FORMATTER)));
        }

        List<ItemStack> food = this.animalPen$getFood();
        ItemStack foodItem;

        if (food.isEmpty())
        {
            // No food item for this entity.
            return lines;
        }
        else if (food.size() == 1)
        {
            foodItem = food.get(0);
        }
        else
        {
            int size = food.size();
            int index = (tick / 100) % size;

            foodItem = food.get(index);
        }

        lines.add(Pair.of(foodItem, component));

        return lines;
    }



    @Unique
    public boolean animal$isFood(ItemStack itemStack)
    {
        return false;
    }


    @Intrinsic(displace = false)
    public List<ItemStack> animalPen$getFood()
    {
        return Collections.emptyList();
    }


    @Unique
    protected int foodCooldown = 0;

    @Unique
    protected long animalCount = 0;

    @Unique
    protected DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().
        appendValue(MINUTE_OF_HOUR, 2).
        optionalStart().
        appendLiteral(':').
        appendValue(SECOND_OF_MINUTE, 2).
        toFormatter();
}
