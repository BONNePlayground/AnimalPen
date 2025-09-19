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
import java.util.*;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(Cow.class)
public abstract class AnimalPenCow extends AnimalPenAnimal
{
    protected AnimalPenCow(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        boolean value = super.animalPen$animalPenTick(blockEntity);

        if (this.animalPen$milkCooldown > 0)
        {
            this.animalPen$milkCooldown--;
            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        tag.putInt("milk_cooldown", this.animalPen$milkCooldown);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        this.animalPen$milkCooldown = tag.getInt("milk_cooldown");
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

        if (itemStack.is(Items.BUCKET))
        {
            if (this.animalPen$milkCooldown > 0)
            {
                return false;
            }

            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            AnimalPenInterface.triggerItemUse(this, (ServerPlayer) player, itemStack, 1);

            ItemStack remainingStack = ItemUtils.createFilledResult(itemStack,
                player,
                Items.MILK_BUCKET.getDefaultInstance());

            player.setItemInHand(hand, remainingStack);

            player.getLevel().playSound(null,
                position,
                SoundEvents.COW_MILK,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$milkCooldown = AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BUCKET,
                this.animalPen$animalCount);

            return true;
        }

        return false;
    }


    @Intrinsic
    @Override
    public ItemStack animalPen$animalPenInteract(ServerLevel level, ItemStack itemStack, BlockPos position)
    {
        if (itemStack.is(Items.BUCKET))
        {
            if (this.animalPen$milkCooldown > 0)
            {
                return ItemStack.EMPTY;
            }

            itemStack.shrink(1);

            level.playSound(null,
                position,
                SoundEvents.COW_MILK,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$milkCooldown = AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BUCKET,
                this.animalPen$animalCount);

            return Items.MILK_BUCKET.getDefaultInstance();
        }

        return super.animalPen$animalPenInteract(level, itemStack, position);
    }


    @Intrinsic
    @Override
    public List<Pair<ItemStack[], Component>> animalPen$animalPenGetLines(int tick, boolean shortLine)
    {
        List<Pair<ItemStack[], Component>> lines = super.animalPen$animalPenGetLines(tick, shortLine);

        if (shortLine &&
            AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BUCKET,
                this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component = new TextComponent("");

        if (this.animalPen$milkCooldown == 0)
        {
            component.append(new TranslatableComponent(
                shortLine ? "display.animal_pen.ready" : "display.animal_pen.full_ready",
                new TextComponent("\uE000"),
                new TextComponent("\uE001")).
                withStyle(ChatFormatting.GREEN));
        }
        else
        {
            component.append(new TranslatableComponent(
                shortLine ? "display.animal_pen.cooldown" : "display.animal_pen.milk_cooldown",
                new TextComponent("\uE000"),
                new TextComponent("\uE001"),
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$milkCooldown / 20).format(AnimalPen.DATE_FORMATTER)));
        }

        lines.add(Pair.of(
            new ItemStack[]{Items.BUCKET.getDefaultInstance(), Items.MILK_BUCKET.getDefaultInstance()},
            component));

        return lines;
    }


    @Intrinsic
    public int animalPen$getRedStoneSignal()
    {
        if (this.animalPen$milkCooldown > 0)
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
    private int animalPen$milkCooldown;
}
