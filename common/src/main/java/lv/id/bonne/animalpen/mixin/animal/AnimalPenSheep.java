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

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

        tag.putInt("wool_cooldown", this.animalPen$woolCooldown);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        this.animalPen$woolCooldown = tag.getInt("wool_cooldown");
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

        if (itemStack.is(Items.SHEARS) ||
            itemStack.is(AnimalPenTags.COMMON_SHEARS))
        {
            if (this.animalPen$woolCooldown > 0)
            {
                AnimalPen.sendDebug("Under cooldown for " + this.animalPen$woolCooldown);

                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            this.setSheared(true);

            AnimalPenInterface.triggerItemUse(this, (ServerPlayer) player, itemStack, 1);
            itemStack.hurtAndBreak(1, player, getSlotForHand(hand));

            ItemLike itemLike = ITEM_BY_DYE.get(this.getColor());

            int woolCount = 1;

            int dropLimits = AnimalPen.config().getDropLimits(Items.WHITE_WOOL);

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

            this.animalPen$woolCooldown = AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.SHEARS,
                this.animalPen$animalCount);

            AnimalPen.sendDebug("Succeeded at using " + itemStack.getItem().arch$registryName());

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

            AnimalPen.sendDebug("Succeeded at using " + itemStack.getItem().arch$registryName());

            return true;
        }

        return false;
    }


    @Intrinsic
    @Override
    public ItemStack animalPen$animalPenInteract(ServerLevel level, ItemStack itemStack, BlockPos position)
    {
        if (itemStack.is(Items.SHEARS) ||
            itemStack.is(AnimalPenTags.COMMON_SHEARS))
        {
            if (this.animalPen$woolCooldown > 0)
            {
                AnimalPen.sendDebug("Under cooldown for " + this.animalPen$woolCooldown);

                return ItemStack.EMPTY;
            }

            this.setSheared(true);

            itemStack.hurtAndBreak(1, level.getRandom(), null, () -> itemStack.setCount(0));

            ItemLike itemLike = ITEM_BY_DYE.get(this.getColor());

            int woolCount = 1;

            int dropLimits = AnimalPen.config().getDropLimits(Items.WHITE_WOOL);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            for (int i = 0; i < this.animalPen$animalCount && woolCount < dropLimits; i++)
            {
                woolCount += level.getRandom().nextInt(3);
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

                Block.popResource(level, position.above(), woolStack);
            }

            level.playSound(null,
                position,
                SoundEvents.SHEEP_SHEAR,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$woolCooldown = AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.SHEARS,
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

        if (!AnimalPen.config().isShowAllInteractions() &&
            shortLine &&
            AnimalPen.config().getEntityCooldown(
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
            component = Component.translatable(
                shortLine ? "display.animal_pen.ready" : "display.animal_pen.full_ready",
                    Component.literal("\uE000"),
                    Component.literal("\uE001")).
                withStyle(ChatFormatting.GREEN);
        }
        else
        {
            component = Component.translatable(
                shortLine ? "display.animal_pen.cooldown" : "display.animal_pen.wool_cooldown",
                Component.literal("\uE000"),
                Component.literal("\uE001"),
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$woolCooldown / 20).format(AnimalPen.DATE_FORMATTER));
        }

        ItemLike itemLike = ITEM_BY_DYE.get(this.getColor());

        lines.add(Pair.of(
            new ItemStack[]{Items.SHEARS.getDefaultInstance(), itemLike.asItem().getDefaultInstance()},
            component));

        if (!AnimalPen.config().isShowAllInteractions() &&
            shortLine)
        {
            return lines;
        }

        Component text = Component.translatable(
            shortLine ? "display.animal_pen.ready" : "display.animal_pen.color_ready",
            Component.literal("\uE000"),
            Component.literal("\uE001")).
            withStyle(ChatFormatting.GREEN);

        ItemStack dyeItem;

        if (animal_pen$DYE.isEmpty())
        {
            return lines;
        }
        else if (animal_pen$DYE.size() == 1)
        {
            dyeItem = animal_pen$DYE.get(0);
        }
        else
        {
            int size = animal_pen$DYE.size();
            int index = (tick / 100) % size;

            dyeItem = animal_pen$DYE.get(index);
        }

        ItemStack woolItem = ITEM_BY_DYE.get(((DyeItem) dyeItem.getItem()).getDyeColor()).asItem().getDefaultInstance();
        lines.add(Pair.of(new ItemStack[]{dyeItem, woolItem}, text));

        return lines;
    }


    @Intrinsic
    public int animalPen$getRedStoneSignal()
    {
        if (this.animalPen$woolCooldown > 0)
        {
            return super.animalPen$getRedStoneSignal();
        }
        else
        {
            // signal | 4 as it is first interaction
            return super.animalPen$getRedStoneSignal() | 4;
        }
    }


    @Unique
    private int animalPen$woolCooldown;

    @Unique
    private final static List<ItemStack> animal_pen$DYE;

    static
    {
        animal_pen$DYE = BuiltInRegistries.ITEM.stream().
            filter(item -> item instanceof DyeItem).
            map(Item::getDefaultInstance).
            toList();
    }
}
