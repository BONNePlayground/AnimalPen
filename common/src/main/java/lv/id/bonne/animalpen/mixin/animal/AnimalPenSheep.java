//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;


@Mixin(Sheep.class)
public abstract class AnimalPenSheep extends AnimalPenAnimal
{
    protected AnimalPenSheep(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


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

        if (itemStack.is(Items.SHEARS))
        {
            if (this.animalPen$woolCooldown > 0)
            {
                return false;
            }

            if (!(player.level() instanceof ServerLevel serverLevel))
            {
                // Next is processed only for server side.
                return true;
            }

            itemStack.hurtAndBreak(1, player, getSlotForHand(hand));

            LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.SHEAR_SHEEP);
            LootParams lootParams = new LootParams.Builder(serverLevel).
                withParameter(LootContextParams.ORIGIN, position.getCenter()).
                withParameter(LootContextParams.THIS_ENTITY, (Sheep) (Object) this).
                withParameter(LootContextParams.TOOL, itemStack).
                create(LootContextParamSets.SHEARING);

            int dropLimits = AnimalPen.CONFIG_MANAGER.getConfiguration().getDropLimits(Items.WHITE_WOOL);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            List<ItemStack> itemStackList = new ArrayList<>();

            int woolCount = 0;
            int animalCounter = 0;

            while (woolCount <= dropLimits && animalCounter++ < this.animalPen$animalCount)
            {
                List<ItemStack> randomItems = lootTable.getRandomItems(lootParams);

                if (randomItems.isEmpty())
                {
                    // Just a stop on infinite loop
                    break;
                }

                woolCount += randomItems.stream().mapToInt(ItemStack::getCount).sum();

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

            this.setSheared(true);

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


    @Unique
    private int animalPen$woolCooldown;
}
