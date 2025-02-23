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
import java.util.List;
import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


@Mixin(MushroomCow.class)
public abstract class AnimalPenMushroomCow extends AnimalPenAnimal
{
    protected AnimalPenMushroomCow(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Shadow
    public abstract MushroomCow.MushroomType getVariant();


    @Shadow
    @Nullable
    private SuspiciousStewEffects stewEffects;


    @Shadow
    protected abstract Optional<SuspiciousStewEffects> getEffectsFromItemStack(ItemStack itemStack);


    @Intrinsic
    @Override
    public boolean animalPen$animalPenTick(BlockEntity blockEntity)
    {
        boolean value = super.animalPen$animalPenTick(blockEntity);

        if (this.animalPen$supCooldown > 0)
        {
            this.animalPen$supCooldown--;
            return true;
        }

        return value;
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        tag.putInt("sup_cooldown", this.animalPen$supCooldown);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        this.animalPen$supCooldown = tag.getInt("sup_cooldown");
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
            if (this.animalPen$supCooldown > 0)
            {
                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            ItemStack bowlStack;
            boolean suspicious = this.stewEffects != null;

            if (suspicious)
            {
                bowlStack = new ItemStack(Items.SUSPICIOUS_STEW);
                bowlStack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, this.stewEffects);
                this.stewEffects = null;
            }
            else
            {
                bowlStack = new ItemStack(Items.MUSHROOM_STEW);
            }

            ItemStack remainingStack = ItemUtils.createFilledResult(itemStack, player, bowlStack, false);
            player.setItemInHand(hand, remainingStack);
            SoundEvent soundEvent;

            if (suspicious)
            {
                soundEvent = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
            }
            else
            {
                soundEvent = SoundEvents.MOOSHROOM_MILK;
            }

            player.level().playSound(null,
                position,
                soundEvent,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$supCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.BOWL,
                this.animalPen$animalCount);

            return true;
        }
        else if (itemStack.is(ItemTags.SMALL_FLOWERS) &&
            this.getVariant() == MushroomCow.MushroomType.BROWN)
        {
            if (this.stewEffects != null)
            {
                if (player.level() instanceof ServerLevel serverLevel)
                {
                    serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        position.getX() + 0.5f,
                        position.getY() + 1.5,
                        position.getZ() + 0.5f,
                        2,
                        0.2, 0.2, 0.2,
                        0.05);
                }
            }
            else
            {
                Optional<SuspiciousStewEffects> optional = this.getEffectsFromItemStack(itemStack);

                if (optional.isEmpty())
                {
                    return false;
                }

                if (player.level().isClientSide())
                {
                    // Next is processed only for server side.
                    return true;
                }

                if (player.level() instanceof ServerLevel serverLevel)
                {
                    serverLevel.sendParticles(
                        ParticleTypes.EFFECT,
                        position.getX() + 0.5f,
                        position.getY() + 1.5,
                        position.getZ() + 0.5f,
                        4,
                        0.2, 0.2, 0.2,
                        0.05);
                }

                if (!player.getAbilities().instabuild)
                {
                    itemStack.shrink(1);
                    player.setItemInHand(hand, itemStack);
                }

                this.stewEffects = optional.get();

                if (player.level() instanceof ServerLevel serverLevel)
                {
                    serverLevel.playSound(null,
                        position,
                        SoundEvents.MOOSHROOM_EAT,
                        SoundSource.NEUTRAL,
                        2.0F,
                        1.0F);
                }

                return true;
            }
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
            Items.BOWL,
            this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component ;

        if (this.animalPen$supCooldown == 0)
        {
            component = Component.translatable("display.animal_pen.sup_ready").
                withStyle(ChatFormatting.GREEN);
        }
        else
        {
            component = Component.translatable("display.animal_pen.sup_cooldown",
                LocalTime.of(0, 0, 0).
                    plusSeconds(this.animalPen$supCooldown / 20).format(AnimalPen.DATE_FORMATTER));
        }

        ItemStack itemStack;

        if (this.stewEffects == null)
        {
            itemStack = Items.MUSHROOM_STEW.getDefaultInstance();
        }
        else
        {
            itemStack = Items.SUSPICIOUS_STEW.getDefaultInstance();
        }

        lines.add(Pair.of(itemStack, component));

        return lines;
    }


    @Unique
    private int animalPen$supCooldown;
}
