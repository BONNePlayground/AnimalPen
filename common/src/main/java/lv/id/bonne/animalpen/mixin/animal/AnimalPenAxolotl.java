//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import lv.id.bonne.animalpen.registries.AnimalPenFoodRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;


@Mixin(Axolotl.class)
public abstract class AnimalPenAxolotl extends AnimalPenAnimal
{
    @Shadow
    public abstract void saveToBucketTag(ItemStack arg);


    protected AnimalPenAxolotl(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);
        tag.putInt("stored_food", this.animalPen$storedFood);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);
        this.animalPen$storedFood = tag.getInt("stored_food");
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
                AnimalPen.sendDebug("Need at least 2 axolotls in pen");
                return false;
            }

            AnimalPenInterface.triggerItemUse(this, (ServerPlayer) player, itemStack, 1);

            ItemStack bucket = new ItemStack(Items.AXOLOTL_BUCKET);
            this.saveToBucketTag(bucket);

            this.animalPen$animalCount--;

            player.setItemInHand(hand,
                ItemUtils.createFilledResult(itemStack, player, bucket, false));

            player.getLevel().playSound(null,
                position,
                SoundEvents.BUCKET_FILL_AXOLOTL,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            if (AnimalPen.config().isTriggerAdvancements())
            {
                // Trigger bucket filling
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, bucket);
            }

            AnimalPen.sendDebug("Succeeded at using " + itemStack.getItem().arch$registryName());

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
                AnimalPen.sendDebug("Need at least 2 axolotls in pen");
                return ItemStack.EMPTY;
            }

            ItemStack bucket = new ItemStack(Items.AXOLOTL_BUCKET);
            this.saveToBucketTag(bucket);

            itemStack.shrink(1);

            level.playSound(null,
                position,
                SoundEvents.BUCKET_FILL_AXOLOTL,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$animalCount--;

            AnimalPen.sendDebug("Succeeded at using " + itemStack.getItem().arch$registryName());

            return bucket;
        }

        return super.animalPen$animalPenInteract(level, itemStack, position);
    }


    @Intrinsic
    @Override
    public List<Pair<ItemStack[], Component>> animalPen$animalPenGetLines(int tick, boolean shortLine)
    {
        List<Pair<ItemStack[], Component>> lines = super.animalPen$animalPenGetLines(tick, shortLine);

        if (this.animalPen$getFood() == null ||
            this.animalPen$getFood().length == 0 ||
            AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.APPLE,
                this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component;

        if (!shortLine && this.animalPen$animalCount > 1)
        {
            component = Component.translatable(
                "display.animal_pen.full_ready",
                Component.literal("\uE000"),
                Component.literal("\uE001")).
                withStyle(ChatFormatting.GREEN);

            ItemStack bucket = new ItemStack(Items.AXOLOTL_BUCKET);
            this.saveToBucketTag(bucket);

            lines.add(Pair.of(
                new ItemStack[]{Items.WATER_BUCKET.getDefaultInstance(), bucket},
                component));
        }

        return lines;
    }


    @Unique
    private int animalPen$storedFood = 0;
}
