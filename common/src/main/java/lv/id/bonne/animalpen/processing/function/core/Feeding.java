//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.items.component.StoredMobData;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
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
        this.processData(player.serverLevel(), itemConsumed, amount, mob, componentHolder, blockPos);

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

        StoredMobData storedMobData = componentHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());
        long animalCount = storedMobData.animalCount();
        animalCount += amount / 2;

        serverLevel.sendParticles(
            ParticleTypes.HEART,
            blockPos.getX() + 0.5f,
            blockPos.getY() + 1.5,
            blockPos.getZ() + 0.5f,
            5,
            0.2, 0.2, 0.2,
            0.05);

        serverLevel.playSound(null,
            blockPos,
            mob.getEatingSound(itemConsumed),
            SoundSource.NEUTRAL,
            1.0F,
            Mth.randomBetween(serverLevel.getRandom(), 0.8F, 1.2F));

        componentHolder.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
            StoredMobData.of(animalCount, storedMobData.properties(), storedMobData.cooldowns()));
    }
}
