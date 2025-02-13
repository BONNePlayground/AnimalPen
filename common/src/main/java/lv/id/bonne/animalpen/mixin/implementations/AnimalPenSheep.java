//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.implementations;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lv.id.bonne.animalpen.config.AnimalPenConfiguration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;


@Mixin(Sheep.class)
public abstract class AnimalPenSheep extends AnimalPenAnimal
{
    @Shadow
    @Final
    private static Map<DyeColor, ItemLike> ITEM_BY_DYE;


    @Shadow
    public abstract DyeColor getColor();


    @Shadow
    public abstract void setColor(DyeColor dyeColor);


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick()
    {
        boolean value = super.animalPen$animalPenTick();

        if (this.woolCooldown > 0)
        {
            this.woolCooldown--;
            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);
        tag.putInt("wool_cooldown", this.woolCooldown);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        if (tag.contains("wool_cooldown", Tag.TAG_INT))
        {
            this.woolCooldown = tag.getInt("wool_cooldown");
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
            if (this.woolCooldown > 0)
            {
                return false;
            }

            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            itemStack.hurtAndBreak(1, player, (playerx) -> playerx.broadcastBreakEvent(hand));

            ItemLike itemLike = ITEM_BY_DYE.get(this.getColor());

            int woolCount = 1;

            int dropLimits = AnimalPenConfiguration.getDropLimits(
                ((Animal) (Object) this).getType().arch$registryName(),
                Items.WHITE_WOOL,
                this.animalCount);

            for (int i = 0; i < this.animalCount && woolCount < dropLimits; i++)
            {
                woolCount += player.getLevel().getRandom().nextInt(3);
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

                Block.popResource(player.getLevel(), position.above(), woolStack);
            }

            player.getLevel().playSound(null,
                position,
                SoundEvents.SHEEP_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.woolCooldown = AnimalPenConfiguration.getEntityCooldown(
                ((Animal) (Object) this).getType().arch$registryName(),
                Items.SHEARS,
                this.animalCount);

            return true;
        }
        else if (itemStack.getItem() instanceof DyeItem dye)
        {
            if (player.getLevel().isClientSide())
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

            player.getLevel().playSound(null,
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

        if (AnimalPenConfiguration.getEntityCooldown(
            ((Animal) (Object) this).getType().arch$registryName(),
            Items.SHEARS,
            this.animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component = new TextComponent("");

        if (this.woolCooldown == 0)
        {
            component.append(new TranslatableComponent("display.animal_pen.wool_ready").
                withStyle(ChatFormatting.GREEN));
        }
        else
        {
            component.append(new TranslatableComponent("display.animal_pen.wool_cooldown",
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.woolCooldown / 20).format(FORMATTER)));
        }

        lines.add(Pair.of(Items.SHEARS.getDefaultInstance(), component));

        return lines;
    }


    @Intrinsic(displace = false)
    public List<ItemStack> animalPen$getFood()
    {
        return Collections.singletonList(Items.WHEAT.getDefaultInstance());
    }


    @Unique
    private int woolCooldown;
}
