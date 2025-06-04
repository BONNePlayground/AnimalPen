//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.wateranimal;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;


@Mixin(AbstractFish.class)
public abstract class AnimalPenAbstractFish extends AnimalPenWaterAnimal
{
    @Shadow
    public abstract void saveToBucketTag(ItemStack itemStack);


    @Shadow
    protected abstract InteractionResult mobInteract(Player player, InteractionHand interactionHand);


    protected AnimalPenAbstractFish(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
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

        if (itemStack.is(Items.WATER_BUCKET))
        {
            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            if (this.animalPen$animalCount <= 1)
            {
                return false;
            }

            ItemStack bucket = this.pen$getFishBucket();

            if (bucket == null)
            {
                return false;
            }

            this.animalPen$animalCount--;

            player.setItemInHand(hand,
                ItemUtils.createFilledResult(itemStack, player, bucket, false));

            player.getLevel().playSound(null,
                position,
                SoundEvents.BUCKET_FILL_FISH,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            return true;
        }

        return false;
    }


    @Intrinsic
    @Override
    public ItemStack animalPen$animalPenInteract(ServerLevel level, ItemStack itemStack, BlockPos position)
    {
        if (itemStack.is(Items.WATER_BUCKET))
        {
            if (this.animalPen$animalCount <= 1)
            {
                return ItemStack.EMPTY;
            }

            ItemStack bucket = this.pen$getFishBucket();

            if (bucket == null)
            {
                return ItemStack.EMPTY;
            }

            itemStack.shrink(1);

            level.playSound(null,
                position,
                SoundEvents.BUCKET_FILL_FISH,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$animalCount--;

            return bucket;
        }

        return super.animalPen$animalPenInteract(level, itemStack, position);
    }


    @Intrinsic
    @Override
    public List<Pair<ItemStack[], Component>> animalPen$animalPenGetLines(int tick, boolean shortLine)
    {
        List<Pair<ItemStack[], Component>> lines = super.animalPen$animalPenGetLines(tick, shortLine);

        if (shortLine)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component = new TextComponent("");

        if (this.animalPen$animalCount > 1)
        {
            ItemStack bucket = this.pen$getFishBucket();

            if (bucket != null)
            {
                component.append(new TranslatableComponent(
                    "display.animal_pen.full_ready",
                    new TextComponent("\uE000"),
                    new TextComponent("\uE001")).
                    withStyle(ChatFormatting.GREEN));

                lines.add(Pair.of(
                    new ItemStack[]{Items.WATER_BUCKET.getDefaultInstance(), bucket},
                    component));
            }
        }

        return lines;
    }


    @Unique
    private ItemStack pen$getFishBucket()
    {
        ItemStack itemStack;

        if (this.getType() == EntityType.COD)
        {
            itemStack = new ItemStack(Items.COD_BUCKET);
        }
        else if (this.getType() == EntityType.PUFFERFISH)
        {
            itemStack = new ItemStack(Items.PUFFERFISH_BUCKET);
        }
        else if (this.getType() == EntityType.SALMON)
        {
            itemStack = new ItemStack(Items.SALMON_BUCKET);
        }
        else if (this.getType() == EntityType.TROPICAL_FISH)
        {
            itemStack = new ItemStack(Items.TROPICAL_FISH_BUCKET);
        }
        else
        {
            return null;
        }

        this.saveToBucketTag(itemStack);

        return itemStack;
    }
}
