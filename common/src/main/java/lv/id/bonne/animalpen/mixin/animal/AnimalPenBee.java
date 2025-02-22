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
import java.util.Map;

import dev.architectury.registry.registries.Registries;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(Bee.class)
public abstract class AnimalPenBee extends AnimalPenAnimal
{
    protected AnimalPenBee(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        boolean value = super.animalPen$animalPenTick(blockEntity);

        if (this.animalPen$pollenCooldown > 0)
        {
            this.animalPen$pollenCooldown--;
            return true;
        }

        if (this.animalPen$pollenCount < 5)
        {
            this.animalPen$pollenCount++;
            this.animalPen$pollenCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.HONEY_BLOCK,
                this.animalPen$animalCount);

            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        tag.putInt("pollen_cooldown", this.animalPen$pollenCooldown);
        tag.putInt("pollen_count", this.animalPen$pollenCount);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        this.animalPen$pollenCooldown = tag.getInt("pollen_cooldown");
        this.animalPen$pollenCount = tag.getInt("pollen_count");
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
            if (this.animalPen$pollenCount < 5)
            {
                return false;
            }

            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            itemStack.hurtAndBreak(1, player, (playerx) -> playerx.broadcastBreakEvent(hand));
            Block.popResource(player.getLevel(), position.above(), new ItemStack(Items.HONEYCOMB, 3));

            player.getLevel().playSound(null,
                position,
                SoundEvents.BEEHIVE_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$pollenCount = 0;
            this.animalPen$pollenCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.HONEY_BLOCK,
                this.animalPen$animalCount);

            return true;
        }
        else if (itemStack.is(Items.GLASS_BOTTLE))
        {
            if (this.animalPen$pollenCount < 5)
            {
                return false;
            }

            if (player.getLevel().isClientSide())
            {
                // Next is processed only for server side.
                return true;
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

            this.animalPen$pollenCount = 0;
            this.animalPen$pollenCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.HONEY_BLOCK,
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
            Items.HONEY_BLOCK,
            this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        if (this.animalPen$pollenCooldown != 0)
        {
            MutableComponent component = new TranslatableComponent(
                "display.animal_pen.pollen_cooldown",
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$pollenCooldown / 20).format(AnimalPen.DATE_FORMATTER));

            ItemStack itemStack;

            if ((tick / 100) % 2 == 0)
            {
                itemStack = Items.SHEARS.getDefaultInstance();
            }
            else
            {
                itemStack = Items.GLASS_BOTTLE.getDefaultInstance();
            }

            lines.add(Pair.of(itemStack, component));
        }

        if (this.animalPen$pollenCount >= 0)
        {
            MutableComponent component;

            if (this.animalPen$pollenCount == 5)
            {
                component = new TranslatableComponent("display.animal_pen.pollen_level_max",
                    this.animalPen$pollenCount);
            }
            else
            {
                component = new TranslatableComponent("display.animal_pen.pollen_level",
                    this.animalPen$pollenCount);
            }

            lines.add(Pair.of(Items.HONEY_BLOCK.getDefaultInstance(), component));
        }

        return lines;
    }


    @Intrinsic
    public List<ItemStack> animalPen$getFood()
    {
        if (ANIMAL_PEN$FOOD_LIST == null)
        {
            ANIMAL_PEN$FOOD_LIST = Registries.get(AnimalPen.MOD_ID).
                get(Registry.ITEM_REGISTRY).entrySet().stream().
                map(Map.Entry::getValue).
                map(Item::getDefaultInstance).
                filter(stack -> stack.is(ItemTags.SMALL_FLOWERS)).
                toList();
        }

        return ANIMAL_PEN$FOOD_LIST;
    }


    @Unique
    private int animalPen$pollenCooldown;

    @Unique
    private int animalPen$pollenCount = -1;

    @Unique
    private static List<ItemStack> ANIMAL_PEN$FOOD_LIST;
}
