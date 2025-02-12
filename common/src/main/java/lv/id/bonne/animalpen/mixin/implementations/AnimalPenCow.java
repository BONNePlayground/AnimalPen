//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.implementations;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

import lv.id.bonne.animalpen.config.AnimalPenConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;


@Mixin(Cow.class)
public abstract class AnimalPenCow extends AnimalPenAnimal
{
    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick()
    {
        boolean value = super.animalPen$animalPenTick();

        if (this.milkCooldown > 0)
        {
            this.milkCooldown--;
            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);
        tag.putInt("milk_cooldown", this.milkCooldown);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        if (tag.contains("milk_cooldown", Tag.TAG_INT))
        {
            this.milkCooldown = tag.getInt("milk_cooldown");
        }
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
            if (this.milkCooldown > 0)
            {
                return false;
            }

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

            this.milkCooldown = AnimalPenConfiguration.getEntityCooldown(
                ((Animal) (Object) this).getType().arch$registryName(),
                Items.BUCKET,
                this.animalCount);

            return true;
        }

        return false;
    }


    @Intrinsic
    @Override
    public List<Pair<ItemStack, String>> animalPen$animalPenGetLines()
    {
        List<Pair<ItemStack, String>> lines = super.animalPen$animalPenGetLines();

        if (AnimalPenConfiguration.getEntityCooldown(
            ((Animal) (Object) this).getType().arch$registryName(),
            Items.BUCKET,
            this.animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        String text;

        if (this.milkCooldown == 0)
        {
            text = new TranslatableComponent("display.animal_pen.milk_ready").getString();
        }
        else
        {
            text = new TranslatableComponent("display.animal_pen.milk_cooldown", this.milkCooldown).getString();
        }

        lines.add(Pair.of(new ItemStack(Items.MILK_BUCKET), text));

        return lines;
    }


    @Unique
    private int milkCooldown;
}
