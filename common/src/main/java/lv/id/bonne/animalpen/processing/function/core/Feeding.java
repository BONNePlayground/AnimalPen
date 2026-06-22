//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import java.util.Map;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.items.component.StoredMobData;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;


/**
 * This function manages animal feeding.
 */
public class Feeding implements EntityFunction
{
    /**
     * Indicates if increment should be half or full.
     * @param fullIncrement - indicates if it is full increment of  halfed
     */
    public Feeding(boolean fullIncrement)
    {
        this.fullIncrement = fullIncrement;
    }


    @Override
    public boolean interactPlayer(ServerPlayer player,
        InteractionHand interactionHand,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        this.processData(player.level(), itemConsumed, amount, mob, componentHolder, blockPos);

        if (mob instanceof Animal animal)
        {
            if (AnimalPen.config().isTriggerAdvancements())
            {
                // Trigger event and statistics for breeding.
                for (int i = 0; i < amount; i++)
                {
                    CriteriaTriggers.BRED_ANIMALS.trigger(player,
                        animal,
                        animal,
                        animal);
                }
            }

            if (AnimalPen.config().isIncreaseStatistics())
            {
                player.awardStat(Stats.ANIMALS_BRED, amount);
            }
        }

        return true;
    }


    @Override
    public boolean interactDispenser(ServerLevel serverLevel,
        Container dispenserInventory,
        int index,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        this.processData(serverLevel, itemConsumed, amount, mob, componentHolder, blockPos);
        return true;
    }


    private void processData(ServerLevel serverLevel,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos)
    {
        if (!componentHolder.has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()))
        {
            AnimalPen.LOGGER.error("FAILED to feed animal as data is missing.");
            return;
        }

        if (!this.fullIncrement)
        {
            amount = amount / 2;
        }

        StoredMobData storedMobData = componentHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());
        long animalCount = storedMobData.animalCount();

        long maximalAnimalCount = AnimalPen.config().getMaximalAnimalCountNormalized();

        if (animalCount >= maximalAnimalCount)
        {
            // Yeah, food is consumed but well... I do not know how to do it.
            return;
        }

        if (animalCount + amount > maximalAnimalCount)
        {
            // Yeah, food is consumed but well... I do not know how to do it.
            amount = (int) (maximalAnimalCount - animalCount);
            animalCount = maximalAnimalCount;
        }
        else
        {
            animalCount += amount;
        }

        // Save last increment into animal data
        Map<String, Integer> properties = storedMobData.properties();
        properties.put(AnimalPenCompoundTags.TAG_LAST_FEEDING_AMOUNT, amount);

        serverLevel.sendParticles(
            ParticleTypes.HEART,
            blockPos.getX() + 0.5f,
            blockPos.getY() + 1.5,
            blockPos.getZ() + 0.5f,
            5,
            0.2, 0.2, 0.2,
            0.05);

        componentHolder.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
            StoredMobData.of(animalCount, properties, storedMobData.cooldowns()));
    }


    /**
     * Used to decide how much breeding increments.
     */
    private final boolean fullIncrement;
}
