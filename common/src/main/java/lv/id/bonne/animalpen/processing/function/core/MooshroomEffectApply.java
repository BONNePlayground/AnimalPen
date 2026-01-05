//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import org.apache.commons.lang3.tuple.Pair;
import java.util.Optional;

import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.mixin.accessors.MushroomCowAccessor;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import lv.id.bonne.animalpen.util.AnimalPenItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * This function applies effect on mooshroom based on consumed item.
 */
public class MooshroomEffectApply implements EntityFunction.PlayerEntityFunction
{
    @Override
    public boolean interactPlayer(ServerPlayer player,
        InteractionHand interactionHand,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        Optional<Pair<MobEffect, Integer>> effectFromItemStack =
            AnimalPenItemHelper.getEffectFromItemStack(itemConsumed);

        if (effectFromItemStack.isEmpty())
        {
            return false;
        }

        if (!(mob instanceof MushroomCowAccessor mushroomCow))
        {
            return false;
        }

        player.serverLevel().sendParticles(
            ParticleTypes.EFFECT,
            blockPos.getX() + 0.5f,
            blockPos.getY() + 1.5,
            blockPos.getZ() + 0.5f,
            4,
            0.2, 0.2, 0.2,
            0.05);

        mushroomCow.setEffect(effectFromItemStack.get().getLeft());
        mushroomCow.setEffectDuration(effectFromItemStack.get().getRight());

        CompoundTag animalTag = new CompoundTag();
        mob.save(animalTag);
        mobNBT.put(AnimalPenCompoundTags.TAG_ANIMAL, animalTag);

        return true;
    }
}
