//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(Turtle.class)
public abstract class AnimalPenTurtle extends AnimalPenAnimal
{
    protected AnimalPenTurtle(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        this.animalPen$processScute(blockEntity.getLevel(), blockEntity.getBlockPos());

        boolean value = super.animalPen$animalPenTick(blockEntity);

        if (this.animalPen$eggCooldown > 0)
        {
            this.animalPen$eggCooldown--;
            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        tag.putInt("egg_cooldown", this.animalPen$eggCooldown);
        tag.putInt("scute_count", this.animalPen$scuteCount);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        this.animalPen$eggCooldown = tag.getInt("egg_cooldown");
        this.animalPen$scuteCount = tag.getInt("scute_count");
    }


    @Intrinsic
    @Override
    public boolean animalPen$animalPenInteract(Player player, InteractionHand hand, BlockPos position)
    {
        // Scute preprocessing
        this.animal$preProcesScute(player, hand, position);

        // food processing

        if (super.animalPen$animalPenInteract(player, hand, position))
        {
            return true;
        }

        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(Items.BUCKET))
        {
            if (this.animalPen$eggCooldown > 0)
            {
                return false;
            }

            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            int dropLimits = AnimalPen.CONFIG_MANAGER.getConfiguration().getDropLimits(Items.TURTLE_EGG);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            int eggCount = (int) Math.min(this.animalPen$animalCount, dropLimits);

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

                Block.popResource(player.getLevel(), position.above(), eggStack);
            }

            player.getLevel().playSound(null,
                position,
                SoundEvents.TURTLE_LAY_EGG,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$eggCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.BUCKET,
                this.animalPen$animalCount);

            return true;
        }

        return false;
    }


    @Unique
    private void animalPen$processScute(Level level, BlockPos position)
    {
        if (this.animalPen$scuteCount <= 0)
        {
            return;
        }

        if (level.isClientSide())
        {
            // Next is processed only for server side.
            return;
        }

        boolean dropScuteAtStart = AnimalPen.CONFIG_MANAGER.getConfiguration().isDropScuteAtStart();

        if (!dropScuteAtStart && this.animalPen$foodCooldown != 0)
        {
            return;
        }

        int dropLimits = AnimalPen.CONFIG_MANAGER.getConfiguration().getDropLimits(Items.SCUTE);

        if (dropLimits <= 0)
        {
            dropLimits = Integer.MAX_VALUE;
        }

        int scuteCount = Math.min(this.animalPen$scuteCount, dropLimits);

        while (scuteCount > 0)
        {
            ItemStack scuteStack = new ItemStack(Items.SCUTE);

            if (scuteCount > 64)
            {
                scuteStack.setCount(64);
                scuteCount -= 64;
            }
            else
            {
                scuteStack.setCount(scuteCount);
                scuteCount = 0;
            }

            Block.popResource(level, position.above(), scuteStack);
        }

        level.playSound(null,
            position,
            SoundEvents.TURTLE_SHAMBLE_BABY,
            SoundSource.NEUTRAL,
            1.0F,
            1.0F);

        this.animalPen$scuteCount = 0;
    }


    @Unique
    private void animal$preProcesScute(Player player, InteractionHand hand, BlockPos position)
    {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.isFood(itemStack))
        {
            if (this.animalPen$foodCooldown > 0)
            {
                return;
            }

            long maxCount = AnimalPen.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

            if (maxCount > 0 && this.animalPen$animalCount >= maxCount)
            {
                return;
            }

            int stackSize = itemStack.getCount();
            stackSize = (int) Math.min(this.animalPen$animalCount, stackSize);

            if (stackSize < 2)
            {
                // Cannot feed 1 animal only for breeding.
                return;
            }

            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return;
            }

            stackSize = (int) Math.min((maxCount - this.animalPen$animalCount) * 2, stackSize);
            this.animalPen$scuteCount += stackSize / 2;
        }
    }


    @Intrinsic
    @Override
    public List<Pair<ItemStack, Component>> animalPen$animalPenGetLines(int tick)
    {
        List<Pair<ItemStack, Component>> lines = super.animalPen$animalPenGetLines(tick);

        if (AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
            this.getType(),
            Items.BUCKET,
            this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component = new TextComponent("");

        if (this.animalPen$eggCooldown == 0)
        {
            component.append(new TranslatableComponent("display.animal_pen.egg_ready").
                withStyle(ChatFormatting.GREEN));
        }
        else
        {
            component.append(new TranslatableComponent("display.animal_pen.egg_cooldown",
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$eggCooldown / 20).format(AnimalPen.DATE_FORMATTER)));
        }

        lines.add(Pair.of(Items.TURTLE_EGG.getDefaultInstance(), component));

        return lines;
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        return Collections.singletonList(Items.SEAGRASS.getDefaultInstance());
    }


    @Unique
    private int animalPen$eggCooldown;

    @Unique
    private int animalPen$scuteCount;
}
