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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;


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
            value = true;
        }

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
        tag.putInt("sniff_cooldown", this.animalPen$sniffingCooldown);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        this.animalPen$sniffingCooldown = tag.getInt("sniff_cooldown");
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

        if (itemStack.is(Items.BOWL))
        {
            if (this.animalPen$sniffingCooldown > 0)
            {
                AnimalPen.sendDebug("Under cooldown for " + this.animalPen$sniffingCooldown);
                return false;
            }

            if (!(player.level() instanceof ServerLevel serverLevel))
            {
                // Next is processed only for server side.
                return true;
            }

            LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SNIFFER_DIGGING);
            LootParams lootParams = new LootParams.Builder(serverLevel).
                withParameter(LootContextParams.ORIGIN, position.getCenter()).
                withParameter(LootContextParams.THIS_ENTITY, this).
                create(LootContextParamSets.GIFT);

            int dropLimits = AnimalPen.config().getDropLimits(Items.TORCHFLOWER_SEEDS);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            List<ItemStack> itemStackList = new ArrayList<>();

            int seedCount = (int) Math.min(this.animalPen$animalCount, dropLimits);

            while (seedCount > 0)
            {
                List<ItemStack> randomItems = lootTable.getRandomItems(lootParams);

                if (randomItems.isEmpty())
                {
                    // Just a stop on infinite loop
                    break;
                }

                seedCount -= randomItems.stream().mapToInt(ItemStack::getCount).sum();

                randomItems.forEach(item -> {
                    boolean added = false;

                    for (ItemStack stack : itemStackList)
                    {
                        if (ItemStack.isSameItemSameComponents(item, stack) &&
                            stack.getCount() < stack.getMaxStackSize())
                        {
                            stack.grow(item.getCount());
                            added = true;
                            break;
                        }
                    }

                    if (!added)
                    {
                        itemStackList.add(item);
                    }
                });
            }

            itemStackList.forEach(seedStack ->
                Block.popResource(player.level(), position.above(), seedStack));

            serverLevel.playSound(null,
                position,
                SoundEvents.SNIFFER_DROP_SEED,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$sniffingCooldown = AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BOWL,
                this.animalPen$animalCount);

            return true;
        }
        else if (itemStack.is(Items.BUCKET))
        {
            if (this.animalPen$eggCooldown > 0)
            {
                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            int dropLimits = AnimalPen.config().getDropLimits(Items.SNIFFER_EGG);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            int eggCount = (int) Math.min(this.animalPen$animalCount, dropLimits);

            while (eggCount > 0)
            {
                ItemStack eggStack = new ItemStack(Items.SNIFFER_EGG);

                if (eggCount > 64)
                {
                    eggStack.setCount(64);
                    eggCount -= 64;
                }
                else
                {
                    eggStack.setCount(eggCount);
                    eggCount = 0;
                }

                Block.popResource(player.level(), position.above(), eggStack);
            }

            player.level().playSound(null,
                position,
                SoundEvents.SNIFFER_EGG_PLOP,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$eggCooldown = AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BUCKET,
                this.animalPen$animalCount);

            AnimalPen.sendDebug("Succeeded at using " + itemStack.getItem().arch$registryName());

            return true;
        }

        return false;
    }


    @Intrinsic
    @Override
    public ItemStack animalPen$animalPenInteract(ServerLevel level, ItemStack itemStack, BlockPos position)
    {
        if (itemStack.is(Items.BOWL))
        {
            if (this.animalPen$sniffingCooldown > 0)
            {
                AnimalPen.sendDebug("Under cooldown for " + this.animalPen$sniffingCooldown);
                return ItemStack.EMPTY;
            }

            LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SNIFFER_DIGGING);
            LootParams lootParams = new LootParams.Builder(level).
                withParameter(LootContextParams.ORIGIN, position.getCenter()).
                withParameter(LootContextParams.THIS_ENTITY, this).
                create(LootContextParamSets.GIFT);

            int dropLimits = AnimalPen.config().getDropLimits(Items.TORCHFLOWER_SEEDS);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            List<ItemStack> itemStackList = new ArrayList<>();

            int seedCount = (int) Math.min(this.animalPen$animalCount, dropLimits);

            while (seedCount > 0)
            {
                List<ItemStack> randomItems = lootTable.getRandomItems(lootParams);

                if (randomItems.isEmpty())
                {
                    // Just a stop on infinite loop
                    break;
                }

                seedCount -= randomItems.stream().mapToInt(ItemStack::getCount).sum();

                randomItems.forEach(item -> {
                    boolean added = false;

                    for (ItemStack stack : itemStackList)
                    {
                        if (ItemStack.isSameItemSameComponents(item, stack) &&
                            stack.getCount() < stack.getMaxStackSize())
                        {
                            stack.grow(item.getCount());
                            added = true;
                            break;
                        }
                    }

                    if (!added)
                    {
                        itemStackList.add(item);
                    }
                });
            }

            itemStackList.forEach(seedStack ->
                Block.popResource(level, position.above(), seedStack));

            level.playSound(null,
                position,
                SoundEvents.SNIFFER_DROP_SEED,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$sniffingCooldown = AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BOWL,
                this.animalPen$animalCount);

            return ItemStack.EMPTY;
        }
        else if (itemStack.is(Items.BUCKET))
        {
            if (this.animalPen$eggCooldown > 0)
            {
                return ItemStack.EMPTY;
            }

            int dropLimits = AnimalPen.config().getDropLimits(Items.SNIFFER_EGG);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            int eggCount = (int) Math.min(this.animalPen$animalCount, dropLimits);

            while (eggCount > 0)
            {
                ItemStack eggStack = new ItemStack(Items.SNIFFER_EGG);

                if (eggCount > 64)
                {
                    eggStack.setCount(64);
                    eggCount -= 64;
                }
                else
                {
                    eggStack.setCount(eggCount);
                    eggCount = 0;
                }

                Block.popResource(level, position.above(), eggStack);
            }

            level.playSound(null,
                position,
                SoundEvents.SNIFFER_EGG_PLOP,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$eggCooldown = AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BUCKET,
                this.animalPen$animalCount);

            AnimalPen.sendDebug("Succeeded at using " + itemStack.getItem().arch$registryName());
            return ItemStack.EMPTY;
        }

        return super.animalPen$animalPenInteract(level, itemStack, position);
    }


    @Intrinsic
    @Override
    public List<Pair<ItemStack[], Component>> animalPen$animalPenGetLines(int tick, boolean shortLine)
    {
        List<Pair<ItemStack[], Component>> lines = super.animalPen$animalPenGetLines(tick, shortLine);

        if (AnimalPen.config().isShowAllInteractions() ||
            !shortLine ||
            AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BOWL,
                this.animalPen$animalCount) != 0)
        {
            MutableComponent component;

            if (this.animalPen$sniffingCooldown == 0)
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
                    shortLine ? "display.animal_pen.cooldown" : "display.animal_pen.sniff_cooldown",
                    Component.literal("\uE000"),
                    Component.literal("\uE001"),
                    LocalTime.of(0, 0, 0).
                        plusSeconds(this.animalPen$sniffingCooldown / 20).format(AnimalPen.DATE_FORMATTER));
            }

            List<ItemStack> food = List.of(Items.TORCHFLOWER_SEEDS.getDefaultInstance(),
                Items.PITCHER_POD.getDefaultInstance());

            ItemStack foodItem;

            int size = food.size();
            int index = (tick / 100) % size;

            foodItem = food.get(index);

            lines.add(Pair.of(
                new ItemStack[]{Items.BOWL.getDefaultInstance(), foodItem},
                component));
        }

        if (AnimalPen.config().isShowAllInteractions() ||
            !shortLine ||
            AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BUCKET,
                this.animalPen$animalCount) != 0)
        {
            MutableComponent component;

            if (this.animalPen$eggCooldown == 0)
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
                    shortLine ? "display.animal_pen.cooldown" : "display.animal_pen.egg_cooldown",
                    Component.literal("\uE000"),
                    Component.literal("\uE001"),
                    LocalTime.of(0, 0, 0).
                        plusSeconds(this.animalPen$eggCooldown / 20).format(AnimalPen.DATE_FORMATTER));
            }

            lines.add(Pair.of(
                new ItemStack[]{Items.BUCKET.getDefaultInstance(), Items.SNIFFER_EGG.getDefaultInstance()},
                component));
        }

        return lines;
    }


    @Intrinsic
    public int animalPen$getRedStoneSignal()
    {
        int returnValue = super.animalPen$getRedStoneSignal();

        if (this.animalPen$sniffingCooldown <= 0)
        {
            // signal | 4 as it is first interaction
            returnValue |= 4;
        }

        if (this.animalPen$eggCooldown <= 0)
        {
            // signal | 8 as it is second interaction
            returnValue |=  8;
        }

        return returnValue;
    }


    @Unique
    private int animalPen$sniffingCooldown;

    @Unique
    private int animalPen$eggCooldown;
}
