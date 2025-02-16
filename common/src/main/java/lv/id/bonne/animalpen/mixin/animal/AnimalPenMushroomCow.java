//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import java.time.LocalTime;
import java.util.*;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(MushroomCow.class)
public abstract class AnimalPenMushroomCow extends AnimalPenAnimal
{
    protected AnimalPenMushroomCow(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Shadow
    @Nullable
    private MobEffect effect;


    @Shadow
    private int effectDuration;


    @Shadow
    public abstract MushroomCow.MushroomType getMushroomType();


    @Shadow
    protected abstract Optional<Pair<MobEffect, Integer>> getEffectFromItemStack(ItemStack itemStack);


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        boolean value = super.animalPen$animalPenTick(blockEntity);

        if (this.animalPen$supCooldown > 0)
        {
            this.animalPen$supCooldown--;
            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);
        tag.putInt("sup_cooldown", this.animalPen$supCooldown);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        if (tag.contains("sup_cooldown", Tag.TAG_INT))
        {
            this.animalPen$supCooldown = tag.getInt("sup_cooldown");
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

        if (itemStack.is(Items.BOWL))
        {
            if (this.animalPen$supCooldown > 0)
            {
                return false;
            }

            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            ItemStack bowlStack;
            boolean suspicious = this.effect != null;

            if (suspicious)
            {
                bowlStack = new ItemStack(Items.SUSPICIOUS_STEW);
                SuspiciousStewItem.saveMobEffect(bowlStack, this.effect, this.effectDuration);

                this.effect = null;
                this.effectDuration = 0;
            }
            else
            {
                bowlStack = new ItemStack(Items.MUSHROOM_STEW);
            }

            ItemStack remainingStack = ItemUtils.createFilledResult(itemStack, player, bowlStack, false);
            player.setItemInHand(hand, remainingStack);
            SoundEvent soundEvent;

            if (suspicious)
            {
                soundEvent = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
            }
            else
            {
                soundEvent = SoundEvents.MOOSHROOM_MILK;
            }

            player.getLevel().playSound(null,
                position,
                soundEvent,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$supCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.BOWL,
                this.animalPen$animalCount);

            return true;
        }
        else if (itemStack.is(ItemTags.SMALL_FLOWERS) &&
            this.getMushroomType() == MushroomCow.MushroomType.BROWN)
        {
            if (this.effect != null)
            {
                if (player.getLevel() instanceof ServerLevel serverLevel)
                {
                    serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        position.getX() + 0.5f,
                        position.getY() + 1.5,
                        position.getZ() + 0.5f,
                        2,
                        0.2, 0.2, 0.2,
                        0.05);
                }
            }
            else
            {
                Optional<Pair<MobEffect, Integer>> optional = this.getEffectFromItemStack(itemStack);

                if (optional.isEmpty())
                {
                    return false;
                }

                if (player.getLevel().isClientSide())
                {
                    // Next is processed only for server side.
                    return true;
                }

                Pair<MobEffect, Integer> pair = optional.get();

                if (player.getLevel() instanceof ServerLevel serverLevel)
                {
                    serverLevel.sendParticles(
                        ParticleTypes.EFFECT,
                        position.getX() + 0.5f,
                        position.getY() + 1.5,
                        position.getZ() + 0.5f,
                        4,
                        0.2, 0.2, 0.2,
                        0.05);
                }

                this.effect = pair.getLeft();
                this.effectDuration = pair.getRight();

                if (!player.getAbilities().instabuild)
                {
                    itemStack.shrink(1);
                    player.setItemInHand(hand, itemStack);
                }

                if (player.getLevel() instanceof ServerLevel serverLevel)
                {
                    serverLevel.playSound(null,
                        position,
                        SoundEvents.MOOSHROOM_EAT,
                        SoundSource.NEUTRAL,
                        2.0F,
                        1.0F);
                }

                return true;
            }
        }

        return false;
    }


    @Intrinsic
    @Override
    public List<Pair<ItemStack, Component>> animalPen$animalPenGetLines(int tick)
    {
        List<Pair<ItemStack, Component>> lines = super.animalPen$animalPenGetLines(tick);

        if (AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
            this.getType(),
            Items.BOWL,
            this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component = new TextComponent("");

        if (this.animalPen$supCooldown == 0)
        {
            component.append(new TranslatableComponent("display.animal_pen.sup_ready").
                withStyle(ChatFormatting.GREEN));
        }
        else
        {
            component.append(new TranslatableComponent("display.animal_pen.sup_cooldown",
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$supCooldown / 20).format(AnimalPen.DATE_FORMATTER)));
        }

        ItemStack itemStack;

        if (this.effect == null)
        {
            itemStack = Items.MUSHROOM_STEW.getDefaultInstance();
        }
        else
        {
            itemStack = Items.SUSPICIOUS_STEW.getDefaultInstance();
        }

        lines.add(Pair.of(itemStack, component));

        return lines;
    }


    @Unique
    private int animalPen$supCooldown;
}
