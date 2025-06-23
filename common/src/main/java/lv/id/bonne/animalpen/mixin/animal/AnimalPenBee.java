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
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(Bee.class)
public abstract class AnimalPenBee extends AnimalPenAnimal
{
    protected AnimalPenBee(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        boolean value = super.animalPen$animalPenTick(blockEntity);

        if (this.animalPen$pollenCooldown > 0)
        {
            this.animalPen$pollenCooldown--;
            return true;
        }

        if (this.animalPen$pollenCount < 5)
        {
            this.animalPen$pollenCount++;

            if (this.animalPen$pollenCount != 5)
            {
                this.animalPen$pollenCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                    this.getType(),
                    Items.HONEY_BLOCK,
                    this.animalPen$animalCount);
            }

            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        tag.putInt("pollen_cooldown", this.animalPen$pollenCooldown);
        tag.putInt("pollen_count", this.animalPen$pollenCount);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        this.animalPen$pollenCooldown = tag.getInt("pollen_cooldown");
        this.animalPen$pollenCount = tag.getInt("pollen_count");
    }


    @Intrinsic
    @Override
    public boolean animalPen$animalPenInteract(Player player, InteractionHand hand, BlockPos position)
    {
        if (super.animalPen$animalPenInteract(player, hand, position))
        {
            return true;
        }

        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(Items.SHEARS) ||
            itemStack.is(AnimalPenTags.COMMON_SHEARS))
        {
            if (this.animalPen$pollenCount < 5)
            {
                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            itemStack.hurtAndBreak(1, player, getSlotForHand(hand));
            Block.popResource(player.level(), position.above(), new ItemStack(Items.HONEYCOMB, 3));

            player.level().playSound(null,
                position,
                SoundEvents.BEEHIVE_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$pollenCount = 0;
            this.animalPen$pollenCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.HONEY_BLOCK,
                this.animalPen$animalCount);

            return true;
        }
        else if (itemStack.is(Items.GLASS_BOTTLE))
        {
            if (this.animalPen$pollenCount < 5)
            {
                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            ItemStack remainingStack = ItemUtils.createFilledResult(itemStack,
                player,
                Items.HONEY_BOTTLE.getDefaultInstance());
            player.setItemInHand(hand, remainingStack);

            player.level().playSound(null,
                position,
                SoundEvents.BOTTLE_FILL,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$pollenCount = 0;
            this.animalPen$pollenCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.HONEY_BLOCK,
                this.animalPen$animalCount);

            return true;
        }

        return false;
    }


    @Intrinsic
    @Override
    public ItemStack animalPen$animalPenInteract(ServerLevel level, ItemStack itemStack, BlockPos position)
    {
        if (this.animalPen$pollenCount < 5)
        {
            return ItemStack.EMPTY;
        }

        if (itemStack.is(Items.SHEARS) ||
            itemStack.is(AnimalPenTags.COMMON_SHEARS))
        {
            itemStack.hurtAndBreak(1, level, null, item -> {});

            Block.popResource(level, position.above(), new ItemStack(Items.HONEYCOMB, 3));

            level.playSound(null,
                position,
                SoundEvents.BEEHIVE_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$pollenCount = 0;
            this.animalPen$pollenCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.HONEY_BLOCK,
                this.animalPen$animalCount);

            return ItemStack.EMPTY;
        }
        else if (itemStack.is(Items.GLASS_BOTTLE))
        {
            itemStack.shrink(1);

            level.playSound(null,
                position,
                SoundEvents.BOTTLE_FILL,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$pollenCount = 0;
            this.animalPen$pollenCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.HONEY_BLOCK,
                this.animalPen$animalCount);

            return new ItemStack(Items.HONEY_BOTTLE);
        }

        return super.animalPen$animalPenInteract(level, itemStack, position);
    }


    @Intrinsic
    @Override
    public List<Pair<ItemStack[], Component>> animalPen$animalPenGetLines(int tick, boolean shortLine)
    {
        List<Pair<ItemStack[], Component>> lines = super.animalPen$animalPenGetLines(tick, shortLine);

        if (shortLine &&
            AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.HONEY_BLOCK,
                this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        if (this.animalPen$pollenCooldown != 0)
        {
            MutableComponent component = Component.translatable(
                shortLine ? "display.animal_pen.cooldown" : "display.animal_pen.pollen_cooldown",
                Component.literal("\uE000"),
                Component.literal("\uE001"),
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$pollenCooldown / 20).format(AnimalPen.DATE_FORMATTER));

            lines.add(Pair.of(
                new ItemStack[]{Items.HONEY_BLOCK.getDefaultInstance(), Items.HONEY_BLOCK.getDefaultInstance()},
                component));
        }

        if (this.animalPen$pollenCount == 5)
        {
            MutableComponent component = Component.translatable("display.animal_pen.pollen_level_max",
                Component.literal("\uE000"),
                this.animalPen$pollenCount);

            lines.add(Pair.of(new ItemStack[]{Items.HONEY_BLOCK.getDefaultInstance()}, component));

            component = Component.translatable(shortLine ? "display.animal_pen.ready" : "display.animal_pen.full_ready",
                Component.literal("\uE000"),
                Component.literal("\uE001"));

            ItemStack toolStack;
            ItemStack resultStack;

            if ((tick / 100) % 2 == 0)
            {
                toolStack = Items.SHEARS.getDefaultInstance();
                resultStack = Items.HONEYCOMB.getDefaultInstance();
            }
            else
            {
                toolStack = Items.GLASS_BOTTLE.getDefaultInstance();
                resultStack = Items.HONEY_BOTTLE.getDefaultInstance();
            }

            lines.add(Pair.of(new ItemStack[]{toolStack, resultStack}, component));
        }
        else
        {
            MutableComponent component = Component.translatable("display.animal_pen.pollen_level",
                Component.literal("\uE000"),
                this.animalPen$pollenCount);

            lines.add(Pair.of(new ItemStack[]{Items.HONEY_BLOCK.getDefaultInstance()}, component));
        }

        return lines;
    }


    @Intrinsic
    public int animalPen$getRedStoneSignal()
    {
        if (this.animalPen$pollenCount < 5)
        {
            return super.animalPen$getRedStoneSignal();
        }
        else
        {
            // signal | 4 as it is first interaction
            return super.animalPen$getRedStoneSignal() | 4;
        }
    }


    @Unique
    private int animalPen$pollenCooldown;

    @Unique
    private int animalPen$pollenCount = -1;
}
