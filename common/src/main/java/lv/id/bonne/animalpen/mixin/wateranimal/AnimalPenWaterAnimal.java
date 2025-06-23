//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.wateranimal;


import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import lv.id.bonne.animalpen.registries.AnimalPenFoodRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;


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
    public void addAdditionalSaveData(ValueOutput compoundTag)
    {
        super.addAdditionalSaveData(compoundTag);
        this.animalPen$animalPenSaveTag(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(ValueInput compoundTag)
    {
        super.readAdditionalSaveData(compoundTag);
        this.animalPen$animalPenLoadTag(compoundTag);
    }


    @Intrinsic
    public boolean animalPen$animalPenUpdateCount(long change)
    {
        if (change < 0 && this.animalPen$animalCount + change < 0)
        {
            return false;
        }

        long maxCount = AnimalPen.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

        if (maxCount > 0 && this.animalPen$animalCount + change > maxCount)
        {
            return false;
        }

        this.animalPen$animalCount += change;
        return true;
    }


    @Intrinsic
    public long animalPen$animalPenGetCount()
    {
        return this.animalPen$animalCount;
    }


    @Intrinsic
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        if (this.animalPen$foodCooldown > 0)
        {
            this.animalPen$foodCooldown--;
            return true;
        }

        return false;
    }


    @Intrinsic
    public void animalPen$animalPenSaveTag(ValueOutput tag)
    {
        // this tag is necessary for animal pickups
        tag.putInt("food_cooldown", this.animalPen$foodCooldown);
        tag.putLong("animal_count", this.animalPen$animalCount);
    }


    @Intrinsic
    public void animalPen$animalPenLoadTag(ValueInput tag)
    {
        this.animalPen$foodCooldown = tag.getIntOr("food_cooldown", 0);
        this.animalPen$animalCount = tag.getLongOr("animal_count", 0);
    }


    @Intrinsic
    public boolean animalPen$animalPenInteract(Player player, InteractionHand hand, BlockPos position)
    {
        ItemStack itemStack = player.getItemInHand(hand);

        if (AnimalPenFoodRegistry.isFood(this.getType().arch$registryName(), itemStack))
        {
            if (this.animalPen$foodCooldown > 0)
            {
                return false;
            }

            long maxCount = AnimalPen.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

            if (maxCount > 0 && this.animalPen$animalCount >= maxCount)
            {
                return false;
            }

            int stackSize = itemStack.getCount();
            stackSize = (int) Math.min(this.animalPen$animalCount, stackSize);

            if (stackSize < 2)
            {
                // Cannot feed 1 animal only for breeding.
                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            stackSize = (int) Math.min((maxCount - this.animalPen$animalCount) * 2, stackSize);

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

            this.animalPen$animalCount += stackSize / 2;

            if (player.level() instanceof ServerLevel serverLevel)
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

            player.level().playSound(null,
                position,
                SoundEvents.GENERIC_EAT.value(),
                SoundSource.NEUTRAL,
                1.0F,
                Mth.randomBetween(player.level().random, 0.8F, 1.2F));

            SoundEvent soundEvent = this.getAmbientSound();

            if (soundEvent != null)
            {
                player.level().playSound(null,
                    position,
                    soundEvent,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F);
            }

            this.animalPen$foodCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.APPLE,
                stackSize);

            return true;
        }

        return false;
    }


    @Intrinsic
    public ItemStack animalPen$animalPenInteract(ServerLevel level, ItemStack itemStack, BlockPos position)
    {
        return ItemStack.EMPTY;
    }


    @Intrinsic
    public List<Pair<ItemStack[], Component>> animalPen$animalPenGetLines(int tick, boolean shortLine)
    {
        List<Pair<ItemStack[], Component>> lines = new LinkedList<>();

        if (shortLine &&
            AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.APPLE,
                this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component;

        if (this.animalPen$foodCooldown == 0)
        {
            component = Component.translatable(
                shortLine ? "display.animal_pen.ready" : "display.animal_pen.food_ready",
                    Component.literal("\uE000"),
                    Component.literal("\uE001")).
                withStyle(ChatFormatting.GREEN);
        }
        else
        {
            component = Component.translatable(
                shortLine ? "display.animal_pen.cooldown" : "display.animal_pen.food_cooldown",
                Component.literal("\uE000"),
                Component.literal("\uE001"),
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$foodCooldown / 20).format(AnimalPen.DATE_FORMATTER));
        }

        ItemStack[] food = this.animalPen$getFood();
        ItemStack foodItem;

        if (food == null || food.length == 0)
        {
            // No food item for this entity.
            return lines;
        }
        else if (food.length == 1)
        {
            foodItem = food[0];
        }
        else
        {
            int size = food.length;
            int index = (tick / 100) % size;

            foodItem = food[index];
        }

        lines.add(Pair.of(new ItemStack[]{foodItem, foodItem}, component));

        return lines;
    }


    @Intrinsic
    @Nullable
    public ItemStack[] animalPen$getFood()
    {
        return AnimalPenFoodRegistry.getFood(this.getType().arch$registryName());
    }


    @Intrinsic
    public int animalPen$getRedStoneSignal()
    {
        // Default value if animals are here.
        int value = 1;

        if (this.animalPen$foodCooldown > 0 || this.animalPen$getFood() == null || this.animalPen$getFood().length == 0)
        {
            // if cooldown or cooldown is not applicable return existing value
            return value;
        }

        // second bit value
        return value | 2;
    }


    @Unique
    protected int animalPen$foodCooldown = 0;

    @Unique
    protected long animalPen$animalCount = 0;
}
