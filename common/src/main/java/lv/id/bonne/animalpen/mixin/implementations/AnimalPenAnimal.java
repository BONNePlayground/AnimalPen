//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.implementations;


import org.apache.commons.lang3.tuple.Pair;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;

import lv.id.bonne.animalpen.config.AnimalPenConfiguration;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import lv.id.bonne.animalpen.mixin.accessors.MobInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


@Mixin(Animal.class)
@Implements(@Interface(iface = AnimalPenInterface.class, prefix = "animalPen$", unique = true))
public abstract class AnimalPenAnimal
{
    @Shadow
    public abstract boolean isFood(ItemStack itemStack);


    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void injectAddAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci)
    {
        this.animalPen$animalPenSaveTag(compoundTag);
    }


    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void injectReadAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci)
    {
        this.animalPen$animalPenLoadTag(compoundTag);
    }


    @Intrinsic(displace = false)
    public boolean animalPen$animalPenUpdateCount(long change)
    {
        if (change < 0 && this.animalCount + change < 0)
        {
            return false;
        }

        this.animalCount += change;
        return true;
    }


    @Intrinsic(displace = false)
    public long animalPen$animalPenGetCount()
    {
        return this.animalCount;
    }


    @Intrinsic(displace = false)
    public boolean animalPen$animalPenTick()
    {
        if (this.foodCooldown > 0)
        {
            this.foodCooldown--;
            return true;
        }

        return false;
    }


    @Intrinsic(displace = false)
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        tag.putInt("food_cooldown", this.foodCooldown);
        tag.putLong("animal_count", this.animalCount);
    }


    @Intrinsic(displace = false)
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


    @Intrinsic(displace = false)
    public boolean animalPen$animalPenInteract(Player player, InteractionHand hand, BlockPos position)
    {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.isFood(itemStack))
        {
            if (this.foodCooldown > 0)
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

            if (player.getLevel() instanceof ServerLevel level)
            {
                level.sendParticles(
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
                ((Animal) (Object) this).getEatingSound(itemStack),
                SoundSource.NEUTRAL,
                1.0F,
                Mth.randomBetween(player.getLevel().random, 0.8F, 1.2F));

            SoundEvent soundEvent = ((MobInvoker) this).invokeGetAmbientSound();

            if (soundEvent != null)
            {
                player.getLevel().playSound(null,
                    position,
                    soundEvent,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F);
            }

            this.foodCooldown = AnimalPenConfiguration.getEntityCooldown(
                ((Animal) (Object) this).getType().arch$registryName(),
                Items.APPLE,
                this.animalCount);

            return true;
        }

        return false;
    }


    @Intrinsic(displace = false)
    public List<Pair<ItemStack, String>> animalPen$animalPenGetLines()
    {
        List<Pair<ItemStack, String>> lines = new LinkedList<>();

        if (AnimalPenConfiguration.getEntityCooldown(
            ((Animal) (Object) this).getType().arch$registryName(),
            Items.APPLE,
            1) == 0)
        {
            // Nothing to return.
            return lines;
        }

        String text;

        if (this.foodCooldown == 0)
        {
            text = new TranslatableComponent("display.animal_pen.food_ready").getString();
        }
        else
        {
            text = new TranslatableComponent("display.animal_pen.food_cooldown", this.foodCooldown).getString();
        }

        lines.add(Pair.of(new ItemStack(Items.APPLE), text));

        return lines;
    }


    @Unique
    private int foodCooldown = 0;

    @Unique
    protected long animalCount = 1;
}
