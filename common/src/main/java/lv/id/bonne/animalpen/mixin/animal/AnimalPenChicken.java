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
import java.util.stream.Collectors;

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
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(Chicken.class)
public abstract class AnimalPenChicken extends AnimalPenAnimal
{
    protected AnimalPenChicken(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Shadow
    @Final
    private static Ingredient FOOD_ITEMS;


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
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
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        this.animalPen$eggCooldown = tag.getInt("egg_cooldown");
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
            if (this.animalPen$eggCooldown > 0)
            {
                return false;
            }

            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            int dropLimits = AnimalPen.CONFIG_MANAGER.getConfiguration().getDropLimits(Items.EGG);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            int eggCount = (int) Math.min(this.animalPen$animalCount, dropLimits);

            while (eggCount > 0)
            {
                ItemStack eggStack = new ItemStack(Items.EGG);

                if (eggCount > 16)
                {
                    eggStack.setCount(16);
                    eggCount -= 16;
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
                SoundEvents.CHICKEN_EGG,
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

        lines.add(Pair.of(Items.EGG.getDefaultInstance(), component));

        return lines;
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        return Arrays.stream(FOOD_ITEMS.getItems()).collect(Collectors.toList());
    }


    @Unique
    private int animalPen$eggCooldown;
}
