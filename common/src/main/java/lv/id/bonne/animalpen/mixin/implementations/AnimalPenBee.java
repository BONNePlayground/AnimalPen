//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.implementations;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import java.util.List;

import lv.id.bonne.animalpen.config.AnimalPenConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;


@Mixin(Bee.class)
public abstract class AnimalPenBee extends AnimalPenAnimal
{
    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick()
    {
        boolean value = super.animalPen$animalPenTick();

        if (this.pollenCooldown > 0)
        {
            this.pollenCooldown--;
            return true;
        }

        if (this.pollenCount < 5)
        {
            this.pollenCount++;
            this.pollenCooldown = AnimalPenConfiguration.getEntityCooldown(
                ((Animal) (Object) this).getType().arch$registryName(),
                Items.HONEY_BLOCK,
                this.animalCount);

            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);
        tag.putInt("pollen_cooldown", this.pollenCooldown);
        tag.putInt("pollen_count", this.pollenCount);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        if (tag.contains("pollen_cooldown", Tag.TAG_INT))
        {
            this.pollenCooldown = tag.getInt("pollen_cooldown");
        }

        if (tag.contains("pollen_count", Tag.TAG_INT))
        {
            this.pollenCount = tag.getInt("pollen_count");
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
            if (this.pollenCount < 5)
            {
                return false;
            }

            itemStack.hurtAndBreak(1, player, (playerx) -> playerx.broadcastBreakEvent(hand));
            Block.popResource(player.getLevel(), position.above(), new ItemStack(Items.HONEYCOMB, 3));

            player.getLevel().playSound(null,
                position,
                SoundEvents.BEEHIVE_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.pollenCount = 0;
            this.pollenCooldown = AnimalPenConfiguration.getEntityCooldown(
                ((Animal) (Object) this).getType().arch$registryName(),
                Items.HONEY_BLOCK,
                this.animalCount);

            return true;
        }
        else if (itemStack.is(Items.GLASS_BOTTLE))
        {
            if (this.pollenCount < 5)
            {
                return false;
            }

            ItemStack remainingStack = ItemUtils.createFilledResult(itemStack,
                player,
                Items.HONEY_BOTTLE.getDefaultInstance());
            player.setItemInHand(hand, remainingStack);

            player.getLevel().playSound(null,
                position,
                SoundEvents.BOTTLE_FILL,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.pollenCount = 0;
            this.pollenCooldown = AnimalPenConfiguration.getEntityCooldown(
                ((Animal) (Object) this).getType().arch$registryName(),
                Items.HONEY_BLOCK,
                this.animalCount);

            return true;
        }

        return false;
    }


    @Intrinsic
    @Override
    public List<Pair<ItemStack, String>> animalPen$animalPenGetLines()
    {
        List<Pair<ItemStack, String>> lines = super.animalPen$animalPenGetLines();

        if (AnimalPenConfiguration.getEntityCooldown(
            ((Animal) (Object) this).getType().arch$registryName(),
            Items.HONEY_BLOCK,
            this.animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        if (this.pollenCount >= 0)
        {
            lines.add(Pair.of(new ItemStack(Items.HONEY_BOTTLE),
                new TranslatableComponent("display.animal_pen.pollen_level", this.pollenCount).getString()));
        }

        if (this.pollenCooldown != 0)
        {
            lines.add(Pair.of(new ItemStack(Items.SHEARS),
                new TranslatableComponent("display.animal_pen.pollen_cooldown", this.pollenCooldown).getString()));
        }

        return lines;
    }


    @Unique
    private int pollenCooldown;

    @Unique
    private int pollenCount = -1;
}
