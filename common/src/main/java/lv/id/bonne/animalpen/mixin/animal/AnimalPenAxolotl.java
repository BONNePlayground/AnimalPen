//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin.animal;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenFoodRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;


@Mixin(Axolotl.class)
public abstract class AnimalPenAxolotl extends AnimalPenAnimal
{
    protected AnimalPenAxolotl(EntityType<? extends Mob> entityType,
        Level level)
    {
        super(entityType, level);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenSaveTag(CompoundTag tag)
    {
        super.animalPen$animalPenSaveTag(tag);
        tag.putInt("stored_food", this.animalPen$storedFood);
    }


    @Intrinsic
    @Override
    public void animalPen$animalPenLoadTag(CompoundTag tag)
    {
        super.animalPen$animalPenLoadTag(tag);
        this.animalPen$storedFood = tag.getInt("stored_food");
    }


    @Intrinsic
    @Override
    public boolean animalPen$animalPenInteract(Player player, InteractionHand hand, BlockPos position)
    {
        ItemStack itemStack = player.getItemInHand(hand);

        if (AnimalPenFoodRegistry.isFood(this.getType().arch$registryName(), itemStack))
        {
            if (player.level().isClientSide())
            {
                // Next is processed only for server side.
                return true;
            }

            this.animalPen$storedFood++;

            if (!player.getAbilities().instabuild)
            {
                player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
            }

            if (this.animalPen$foodCooldown > 0 || this.animalPen$storedFood < 2)
            {
                return false;
            }

            long maxCount = AnimalPen.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

            if (maxCount > 0 && this.animalPen$animalCount >= maxCount)
            {
                return false;
            }

            int stackSize = (int) Math.min(this.animalPen$animalCount, this.animalPen$storedFood);

            if (stackSize < 2)
            {
                // Cannot feed 1 animal only for breeding.
                return false;
            }

            stackSize = (int) Math.min((maxCount - this.animalPen$animalCount) * 2, stackSize);

            this.animalPen$animalCount += stackSize / 2;
            this.animalPen$storedFood -= stackSize;

            if (player.level() instanceof ServerLevel serverLevel)
            {
                serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    position.getX() + 0.5f,
                    position.getY() + 1.5,
                    position.getZ() + 0.5f,
                    5,
                    0.2, 0.2, 0.2,
                    0.05);
            }

            player.level().playSound(null,
                position,
                this.getEatingSound(itemStack),
                SoundSource.NEUTRAL,
                1.0F,
                Mth.randomBetween(player.level().random, 0.8F, 1.2F));

            SoundEvent soundEvent = this.getAmbientSound();

            if (soundEvent != null)
            {
                player.level().playSound(null,
                    position,
                    soundEvent,
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F);
            }

            this.animalPen$foodCooldown = AnimalPen.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                this.getType(),
                Items.APPLE,
                stackSize);

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
            Items.APPLE,
            this.animalPen$animalCount) == 0)
        {
            // Nothing to return.
            return lines;
        }

        MutableComponent component =
            Component.translatable("display.animal_pen.stored_food", this.animalPen$storedFood);

        ItemStack[] food = this.animalPen$getFood();
        ItemStack foodItem;

        if (food == null || food.length == 0)
        {
            // No food item for this entity.
            return lines;
        }
        else if (food.length == 1)
        {
            foodItem = food[0];
        }
        else
        {
            int size = food.length;
            int index = (tick / 100) % size;

            foodItem = food[index];
        }

        lines.add(Pair.of(foodItem, component));

        return lines;
    }


    @Unique
    private int animalPen$storedFood = 0;
}
