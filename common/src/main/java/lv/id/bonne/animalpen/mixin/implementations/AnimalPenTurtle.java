//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.implementations;


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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;


@Mixin(Turtle.class)
public abstract class AnimalPenTurtle extends AnimalPenAnimal
{
    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick()
    {
        boolean value = super.animalPen$animalPenTick();

        if (this.eggCooldown > 0)
        {
            this.eggCooldown--;
            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        tag.putInt("egg_cooldown", this.eggCooldown);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        if (tag.contains("egg_cooldown", Tag.TAG_INT))
        {
            this.eggCooldown = tag.getInt("egg_cooldown");
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
            if (this.eggCooldown > 0)
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

            int eggCount = (int) Math.min(this.animalCount, dropLimits);

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

            this.eggCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                ((Animal) (Object) this).getType(),
                Items.BUCKET,
                this.animalCount);

            return true;
        }

        return false;
    }


    @Intrinsic
    @Override
    public List<Pair<ItemStack, Component>> animalPen$animalPenGetLines(int tick)
    {
        List<Pair<ItemStack, Component>> lines = super.animalPen$animalPenGetLines(tick);

        if (AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
            ((Animal) (Object) this).getType(),
            Items.BUCKET,
            this.animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component = new TextComponent("");

        if (this.eggCooldown == 0)
        {
            component.append(new TranslatableComponent("display.animal_pen.egg_ready").
                withStyle(ChatFormatting.GREEN));
        }
        else
        {
            component.append(new TranslatableComponent("display.animal_pen.egg_cooldown",
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.eggCooldown / 20).format(FORMATTER)));
        }

        lines.add(Pair.of(Items.TURTLE_EGG.getDefaultInstance(), component));

        return lines;
    }


    @Intrinsic(displace = false)
    public List<ItemStack> animalPen$getFood()
    {
        return Collections.singletonList(Items.SEAGRASS.getDefaultInstance());
    }


    @Unique
    private int eggCooldown;
}
