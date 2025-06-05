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
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(Armadillo.class)
public abstract class AnimalPenArmadillo extends AnimalPenAnimal
{
    protected AnimalPenArmadillo(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        boolean value = super.animalPen$animalPenTick(blockEntity);

        if (this.animalPen$scuteCooldown > 0)
        {
            this.animalPen$scuteCooldown--;
            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        if (this.animalPen$scuteCooldown > 0)
        {
            tag.putInt("scute_cooldown", this.animalPen$scuteCooldown);
        }
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        if (tag.contains("scute_cooldown", Tag.TAG_INT))
        {
            this.animalPen$scuteCooldown = tag.getInt("scute_cooldown");
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

        if (itemStack.is(Items.BRUSH))
        {
            if (this.animalPen$scuteCooldown > 0)
            {
                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            itemStack.hurtAndBreak(16, player, getSlotForHand(hand));
            Block.popResource(player.level(), position.above(), new ItemStack(Items.ARMADILLO_SCUTE));

            player.level().playSound(null,
                position,
                SoundEvents.ARMADILLO_SCUTE_DROP,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$scuteCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.BRUSH,
                this.animalPen$animalCount);

            return true;
        }

        return false;
    }


    @Intrinsic
    @Override
    public ItemStack animalPen$animalPenInteract(ServerLevel level, ItemStack itemStack, BlockPos position)
    {
        if (itemStack.is(Items.BRUSH))
        {
            if (this.animalPen$scuteCooldown > 0)
            {
                return ItemStack.EMPTY;
            }

            itemStack.hurtAndBreak(1, level, null, item -> {});

            Block.popResource(level, position.above(), new ItemStack(Items.ARMADILLO_SCUTE));

            level.playSound(null,
                position,
                SoundEvents.ARMADILLO_SCUTE_DROP,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$scuteCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.BRUSH,
                this.animalPen$animalCount);

            return ItemStack.EMPTY;
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
                Items.BRUSH,
                this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component;

        if (this.animalPen$scuteCooldown == 0)
        {
            component = Component.translatable(
                    shortLine ? "display.animal_pen.ready" : "display.animal_pen.full_ready",
                    Component.literal("\uE000"),
                    Component.literal("\uE001")).
                withStyle(ChatFormatting.GREEN);
        }
        else
        {
            component = Component.translatable(
                shortLine ? "display.animal_pen.cooldown" : "display.animal_pen.brush_cooldown",
                Component.literal("\uE000"),
                Component.literal("\uE001"),
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$scuteCooldown / 20).format(AnimalPen.DATE_FORMATTER));
        }

        lines.add(Pair.of(
            new ItemStack[]{Items.BRUSH.getDefaultInstance(), Items.ARMADILLO_SCUTE.getDefaultInstance()},
            component));

        return lines;
    }


    @Intrinsic
    public int animalPen$getRedStoneSignal()
    {
        if (this.animalPen$scuteCooldown > 0)
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
    private int animalPen$scuteCooldown;
}
