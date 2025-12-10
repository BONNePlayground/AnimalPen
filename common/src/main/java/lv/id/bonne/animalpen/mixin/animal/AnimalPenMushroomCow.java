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
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;


@Mixin(MushroomCow.class)
public abstract class AnimalPenMushroomCow extends AnimalPenAnimal
{
    protected AnimalPenMushroomCow(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Shadow
    public abstract MushroomCow.Variant getVariant();


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
    public void animalPen$animalPenSaveTag(ValueOutput tag)
    {
        super.animalPen$animalPenSaveTag(tag);

        tag.putInt("sup_cooldown", this.animalPen$supCooldown);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(ValueInput tag)
    {
        super.animalPen$animalPenLoadTag(tag);

        this.animalPen$supCooldown = tag.getIntOr("sup_cooldown", 0);
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
                AnimalPen.sendDebug("Under cooldown for " + this.animalPen$supCooldown);

                return false;
            }

            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            AnimalPenInterface.triggerItemUse(this, (ServerPlayer) player, itemStack, 1);

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

            this.animalPen$supCooldown = AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BOWL,
                this.animalPen$animalCount);

            AnimalPen.sendDebug("Succeeded at using " + itemStack.getItem().arch$registryName());

            return true;
        }
        else if (itemStack.is(ItemTags.SMALL_FLOWERS) &&
            this.getVariant() == MushroomCow.Variant.BROWN)
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

                    AnimalPen.sendDebug("Effect already applied");
                }
            }
            else
            {
                Optional<SuspiciousStewEffects> optional = this.getEffectsFromItemStack(itemStack);

                if (optional.isEmpty())
                {
                    AnimalPen.sendDebug("No effect from flower");

                    return false;
                }

                if (player.level().isClientSide())
                {
                    // Next is processed only for server side.
                    return true;
                }

                if (player.level() instanceof ServerLevel serverLevel)
                {
                    SpellParticleOption spellParticleOption = SpellParticleOption.create(ParticleTypes.EFFECT, -1, 1.0F);

                    for(int j = 0; j < 4; ++j)
                    {
                        serverLevel.addParticle(spellParticleOption,
                            position.getX() + this.random.nextDouble() / 2.0D,
                            position.getY() + 1.5D,
                            position.getZ() + this.random.nextDouble() / 2.0D,
                            0.0D, this.random.nextDouble() / 5.0D,
                            0.0D);
                    }
                }
                AnimalPenInterface.triggerItemUse(this, (ServerPlayer) player, itemStack, 1);

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

                AnimalPen.sendDebug("Succeeded at using " + itemStack.getItem().arch$registryName());

                return true;
            }
        }

        return false;
    }


    @Intrinsic
    @Override
    public ItemStack animalPen$animalPenInteract(ServerLevel level, ItemStack itemStack, BlockPos position)
    {
        if (itemStack.is(Items.BOWL))
        {
            if (this.animalPen$supCooldown > 0)
            {
                AnimalPen.sendDebug("Under cooldown for " + this.animalPen$supCooldown);

                return ItemStack.EMPTY;
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

            SoundEvent soundEvent;

            if (suspicious)
            {
                soundEvent = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
            }
            else
            {
                soundEvent = SoundEvents.MOOSHROOM_MILK;
            }

           level.playSound(null,
                position,
                soundEvent,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F);

            this.animalPen$supCooldown = AnimalPen.config().getEntityCooldown(
                this.getType(),
                Items.BOWL,
                this.animalPen$animalCount);

            AnimalPen.sendDebug("Succeeded at using " + itemStack.getItem().arch$registryName());

            return bowlStack;
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
                Items.BOWL,
                this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component ;

        if (this.animalPen$supCooldown == 0)
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
                shortLine ? "display.animal_pen.cooldown" : "display.animal_pen.soup_cooldown",
                Component.literal("\uE000"),
                Component.literal("\uE001"),
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

        lines.add(Pair.of(
            new ItemStack[]{Items.BOWL.getDefaultInstance(), itemStack},
            component));

        if (this.getVariant() != MushroomCow.Variant.BROWN || this.stewEffects != null)
        {
            return lines;
        }

        if (!AnimalPen.config().isShowAllInteractions() && shortLine)
        {
            return lines;
        }

        Component text = Component.translatable(
            shortLine ? "display.animal_pen.ready" : "display.animal_pen.apply_ready",
                Component.literal("\uE000"),
                Component.literal("\uE001")).
            withStyle(ChatFormatting.GREEN);

        ItemStack flowerItem;

        if (animal_pen$SMALL_FLOWERS.isEmpty())
        {
            // No flowers.
            return lines;
        }
        else if (animal_pen$SMALL_FLOWERS.size() == 1)
        {
            flowerItem = animal_pen$SMALL_FLOWERS.get(0);
        }
        else
        {
            int size = animal_pen$SMALL_FLOWERS.size();
            int index = (tick / 100) % size;

            flowerItem = animal_pen$SMALL_FLOWERS.get(index);
        }

        ItemStack bowlStack = new ItemStack(Items.SUSPICIOUS_STEW);

        this.getEffectsFromItemStack(flowerItem).ifPresent(effects ->
        {
            bowlStack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, effects);
            lines.add(Pair.of(
                new ItemStack[]{flowerItem, bowlStack},
                text));
        });

        return lines;
    }


    @Intrinsic
    public int animalPen$getRedStoneSignal()
    {
        if (this.animalPen$supCooldown > 0)
        {
            return super.animalPen$getRedStoneSignal();
        }
        else
        {
            // signal | 8 as it is second interaction
            return super.animalPen$getRedStoneSignal() | 8;
        }
    }


    @Unique
    private int animalPen$supCooldown;

    @Unique
    private final static List<ItemStack> animal_pen$SMALL_FLOWERS;

    static
    {
        animal_pen$SMALL_FLOWERS = BuiltInRegistries.ITEM.stream().
            filter(item -> item instanceof BlockItem block && block.getBlock() instanceof FlowerBlock).
            map(Item::getDefaultInstance).
            toList();
    }
}
