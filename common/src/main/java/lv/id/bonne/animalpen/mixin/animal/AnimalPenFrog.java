//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(Frog.class)
public abstract class AnimalPenFrog extends AnimalPenAnimal
{
    protected AnimalPenFrog(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Shadow
    @Final
    public static Ingredient TEMPTATION_ITEM;


    @Shadow
    public abstract FrogVariant getVariant();


    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        boolean value = super.animalPen$animalPenTick(blockEntity);

        if (this.animalPen$frogLightCooldown > 0)
        {
            this.animalPen$frogLightCooldown--;
            return true;
        }

        return value;
    }


    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        if (this.animalPen$frogLightCooldown > 0)
        {
            tag.putInt("frog_light_cooldown", this.animalPen$frogLightCooldown);
        }
    }


    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        if (tag.contains("frog_light_cooldown"))
        {
            this.animalPen$frogLightCooldown = tag.getInt("frog_light_cooldown");
        }
    }


    @Override
    public boolean animalPen$animalPenInteract(Player player, InteractionHand hand, BlockPos position)
    {
        if (super.animalPen$animalPenInteract(player, hand, position))
        {
            return true;
        }

        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(Items.MAGMA_BLOCK))
        {
            if (this.animalPen$frogLightCooldown > 0)
            {
                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            int froglightCount = (int) Math.min(this.animalPen$animalCount, itemStack.getCount());

            int dropLimits = AnimalPen.CONFIG_MANAGER.getConfiguration().getDropLimits(Items.PEARLESCENT_FROGLIGHT);

            if (dropLimits > 0)
            {
                froglightCount = Math.min(froglightCount, dropLimits);
            }

            FrogVariant variant = this.getVariant();

            Item frogLightItem;

            if (variant == FrogVariant.WARM)
            {
                frogLightItem = Items.PEARLESCENT_FROGLIGHT;
            }
            else if (variant == FrogVariant.COLD)
            {
                frogLightItem = Items.VERDANT_FROGLIGHT;
            }
            else if (variant == FrogVariant.TEMPERATE)
            {
                frogLightItem = Items.OCHRE_FROGLIGHT;
            }
            else
            {
                return false;
            }

            itemStack.shrink(froglightCount);

            if (itemStack.getCount() <= 0)
            {
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            else
            {
                player.setItemInHand(hand, itemStack);
            }

            while (froglightCount > 0)
            {
                ItemStack frogLight = new ItemStack(frogLightItem);

                if (froglightCount > 64)
                {
                    frogLight.setCount(64);
                    froglightCount -= 64;
                }
                else
                {
                    frogLight.setCount(froglightCount);
                    froglightCount = 0;
                }

                Block.popResource(player.level(), position.above(), frogLight);
            }

            player.level().playSound(null,
                position,
                SoundEvents.FROG_EAT,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$frogLightCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.MAGMA_BLOCK,
                this.animalPen$animalCount);

            return true;
        }

        return false;
    }


    @Override
    public List<Pair<ItemStack, Component>> animalPen$animalPenGetLines(int tick)
    {
        List<Pair<ItemStack, Component>> lines = super.animalPen$animalPenGetLines(tick);

        if (AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
            this.getType(),
            Items.MAGMA_BLOCK,
            this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component;

        if (this.animalPen$frogLightCooldown == 0)
        {
            component = Component.translatable("display.animal_pen.frog_light_ready").
                withStyle(ChatFormatting.GREEN);
        }
        else
        {
            component = Component.translatable("display.animal_pen.frog_light_cooldown",
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$frogLightCooldown / 20).format(AnimalPen.DATE_FORMATTER));
        }

        lines.add(Pair.of(Items.MAGMA_BLOCK.getDefaultInstance(), component));

        return lines;
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        return Arrays.stream(TEMPTATION_ITEM.getItems()).toList();
    }

    @Unique
    private int animalPen$frogLightCooldown;
}
