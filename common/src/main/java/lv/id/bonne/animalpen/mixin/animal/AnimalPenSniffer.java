//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import dev.architectury.registry.registries.RegistrarManager;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(Sniffer.class)
public abstract class AnimalPenSniffer extends AnimalPenAnimal
{
    protected AnimalPenSniffer(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }

    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        boolean value = super.animalPen$animalPenTick(blockEntity);

        if (this.animalPen$sniffingCooldown > 0)
        {
            this.animalPen$sniffingCooldown--;
            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        if (this.animalPen$sniffingCooldown > 0)
        {
            tag.putInt("sniff_cooldown", this.animalPen$sniffingCooldown);
        }
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        if (tag.contains("sniff_cooldown", Tag.TAG_INT))
        {
            this.animalPen$sniffingCooldown = tag.getInt("sniff_cooldown");
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
            if (this.animalPen$sniffingCooldown > 0)
            {
                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            int dropLimits = AnimalPen.CONFIG_MANAGER.getConfiguration().getDropLimits(Items.TORCHFLOWER_SEEDS);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            int seedCount = (int) Math.min(this.animalPen$animalCount, dropLimits);

            while (seedCount > 0)
            {
                ItemStack seedStack = new ItemStack(Items.TORCHFLOWER_SEEDS);
                int stackSize = seedStack.getMaxStackSize();

                if (seedCount > stackSize)
                {
                    seedStack.setCount(stackSize);
                    seedCount -= stackSize;
                }
                else
                {
                    seedStack.setCount(seedCount);
                    seedCount = 0;
                }

                Block.popResource(player.level(), position.above(), seedStack);
            }

            player.level().playSound(null,
                position,
                SoundEvents.SNIFFER_DIGGING,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$sniffingCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
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

        MutableComponent component;

        if (this.animalPen$sniffingCooldown == 0)
        {
            component = Component.translatable("display.animal_pen.sniff_ready").
                withStyle(ChatFormatting.GREEN);
        }
        else
        {
            component = Component.translatable("display.animal_pen.sniff_cooldown",
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$sniffingCooldown / 20).format(AnimalPen.DATE_FORMATTER));
        }

        List<ItemStack> food = List.of(Items.TORCHFLOWER_SEEDS.getDefaultInstance());
        ItemStack foodItem;

        if (food.size() == 1)
        {
            foodItem = food.get(0);
        }
        else
        {
            int size = food.size();
            int index = (tick / 100) % size;

            foodItem = food.get(index);
        }

        lines.add(Pair.of(foodItem, component));

        return lines;
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        if (ANIMAL_PEN$FOOD_LIST == null)
        {
            ANIMAL_PEN$FOOD_LIST = RegistrarManager.get(AnimalPen.MOD_ID).
                get(Registries.ITEM).entrySet().stream().
                map(Map.Entry::getValue).
                map(Item::getDefaultInstance).
                filter(stack -> stack.is(ItemTags.SNIFFER_FOOD)).
                toList();
        }

        return ANIMAL_PEN$FOOD_LIST;
    }


    @Unique
    private static List<ItemStack> ANIMAL_PEN$FOOD_LIST;

    @Unique
    private int animalPen$sniffingCooldown;
}
