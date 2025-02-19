//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(Sheep.class)
public abstract class AnimalPenSheep extends AnimalPenAnimal
{
    protected AnimalPenSheep(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Shadow
    @Final
    private static Map<DyeColor, ItemLike> ITEM_BY_DYE;


    @Shadow
    public abstract DyeColor getColor();


    @Shadow
    public abstract void setColor(DyeColor dyeColor);


    @Shadow
    public abstract void setSheared(boolean bl);


    @Shadow
    public abstract boolean isSheared();


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        boolean value = super.animalPen$animalPenTick(blockEntity);

        if (this.animalPen$woolCooldown > 0)
        {
            this.animalPen$woolCooldown--;
            return true;
        }

        if (this.isSheared())
        {
            this.setSheared(false);
            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        if (this.animalPen$woolCooldown > 0)
        {
            tag.putInt("wool_cooldown", this.animalPen$woolCooldown);
        }
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        if (tag.contains("wool_cooldown", Tag.TAG_INT))
        {
            this.animalPen$woolCooldown = tag.getInt("wool_cooldown");
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

        if (itemStack.is(Items.SHEARS))
        {
            if (this.animalPen$woolCooldown > 0)
            {
                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            this.setSheared(true);

            itemStack.hurtAndBreak(1, player, (playerx) -> playerx.broadcastBreakEvent(hand));

            ItemLike itemLike = ITEM_BY_DYE.get(this.getColor());

            int woolCount = 1;

            int dropLimits = AnimalPen.CONFIG_MANAGER.getConfiguration().getDropLimits(Items.WHITE_WOOL);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            for (int i = 0; i < this.animalPen$animalCount && woolCount < dropLimits; i++)
            {
                woolCount += player.level().getRandom().nextInt(3);
            }

            while (woolCount > 0)
            {
                ItemStack woolStack = new ItemStack(itemLike);

                if (woolCount > 64)
                {
                    woolStack.setCount(64);
                    woolCount -= 64;
                }
                else
                {
                    woolStack.setCount(woolCount);
                    woolCount = 0;
                }

                Block.popResource(player.level(), position.above(), woolStack);
            }

            player.level().playSound(null,
                position,
                SoundEvents.SHEEP_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$woolCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.SHEARS,
                this.animalPen$animalCount);

            return true;
        }
        else if (itemStack.getItem() instanceof DyeItem dye)
        {
            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            this.setColor(dye.getDyeColor());

            if (!player.getAbilities().instabuild)
            {
                itemStack.shrink(1);
                player.setItemInHand(hand, itemStack);
            }

            player.level().playSound(null,
                position,
                SoundEvents.DYE_USE,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

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
            Items.SHEARS,
            this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component;

        if (this.animalPen$woolCooldown == 0)
        {
            component = Component.translatable("display.animal_pen.wool_ready").
                withStyle(ChatFormatting.GREEN);
        }
        else
        {
            component = Component.translatable("display.animal_pen.wool_cooldown",
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$woolCooldown / 20).format(AnimalPen.DATE_FORMATTER));
        }

        lines.add(Pair.of(Items.SHEARS.getDefaultInstance(), component));

        return lines;
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        return Collections.singletonList(Items.WHEAT.getDefaultInstance());
    }


    @Unique
    private int animalPen$woolCooldown;
}
