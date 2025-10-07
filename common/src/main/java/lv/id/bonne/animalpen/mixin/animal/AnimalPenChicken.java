//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.ChickenVariant;
import net.minecraft.world.entity.animal.ChickenVariants;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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


@Mixin(Chicken.class)
public abstract class AnimalPenChicken extends AnimalPenAnimal
{
    @Shadow
    public abstract Holder<ChickenVariant> getVariant();


    protected AnimalPenChicken(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        boolean value = super.animalPen$animalPenTick(blockEntity);

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
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        this.animalPen$eggCooldown = tag.getIntOr("egg_cooldown", 0);
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
            if (this.animalPen$eggCooldown > 0)
            {
                AnimalPen.sendDebug("Under cooldown for " + this.animalPen$eggCooldown);

                return false;
            }

            if (!(player.level() instanceof ServerLevel serverLevel))
            {
                // Next is processed only for server side.
                return true;
            }

            LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.CHICKEN_LAY);
            LootParams lootParams = new LootParams.Builder(serverLevel).
                withParameter(LootContextParams.ORIGIN, position.getCenter()).
                withParameter(LootContextParams.THIS_ENTITY, this).
                create(LootContextParamSets.GIFT);

            int dropLimits = AnimalPen.config().getDropLimits(Items.EGG);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            List<ItemStack> itemStackList = new ArrayList<>();

            int eggCount = (int) Math.min(this.animalPen$animalCount, dropLimits);

            while (eggCount > 0)
            {
                List<ItemStack> randomItems = lootTable.getRandomItems(lootParams);

                if (randomItems.isEmpty())
                {
                    // Just a stop on infinite loop
                    break;
                }

                eggCount -= randomItems.stream().mapToInt(ItemStack::getCount).sum();

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

            itemStackList.forEach(eggStack ->
                Block.popResource(player.level(), position.above(), eggStack));

            player.level().playSound(null,
                position,
                SoundEvents.CHICKEN_EGG,
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
        if (this.animalPen$eggCooldown > 0)
        {
            AnimalPen.sendDebug("Under cooldown for " + this.animalPen$eggCooldown);

            return ItemStack.EMPTY;
        }

        if (itemStack.is(Items.BUCKET))
        {
            int dropLimits = AnimalPen.config().getDropLimits(Items.EGG);

            if (dropLimits <= 0)
            {
                dropLimits = Integer.MAX_VALUE;
            }

            int eggCount = (int) Math.min(this.animalPen$animalCount, dropLimits);

            while (eggCount > 0)
            {
                ItemStack eggStack = new ItemStack(Items.EGG);

                if (eggCount > 16)
                {
                    eggStack.setCount(16);
                    eggCount -= 16;
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
                SoundEvents.CHICKEN_EGG,
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

        if (!AnimalPen.config().isShowAllInteractions() &&
            shortLine &&
            AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BUCKET,
                this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component = Component.literal("");

        if (this.animalPen$eggCooldown == 0)
        {
            component.append(Component.translatable(
                shortLine ? "display.animal_pen.ready" : "display.animal_pen.full_ready",
                    Component.literal("\uE000"),
                    Component.literal("\uE001")).
                withStyle(ChatFormatting.GREEN));
        }
        else
        {
            component.append(Component.translatable(
                shortLine ? "display.animal_pen.cooldown" : "display.animal_pen.egg_cooldown",
                Component.literal("\uE000"),
                Component.literal("\uE001"),
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$eggCooldown / 20).format(AnimalPen.DATE_FORMATTER)));
        }

        lines.add(Pair.of(
            new ItemStack[]{Items.BUCKET.getDefaultInstance(), pen$getEggItem().getDefaultInstance()},
            component));

        return lines;
    }


    /**
     * This method returns egg based on chicken type.
     * @return egg item.
     */
    @Unique
    private Item pen$getEggItem()
    {
        Holder<ChickenVariant> variant = this.getVariant();

        Item eggItem;

        if (variant.is(ChickenVariants.WARM))
        {
            eggItem = Items.BROWN_EGG;
        }
        else if (variant.is(ChickenVariants.COLD))
        {
            eggItem = Items.BLUE_EGG;
        }
        else
        {
            eggItem = Items.EGG;
        }

        return eggItem;
    }


    @Intrinsic
    public int animalPen$getRedStoneSignal()
    {
        if (this.animalPen$eggCooldown > 0)
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
    private int animalPen$eggCooldown;
}
