//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import java.util.Optional;

import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.mixin.accessors.MushroomCowAccessor;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import lv.id.bonne.animalpen.util.AnimalPenItemHelper;
import lv.id.bonne.animalpen.util.AnimalPenVariantHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;


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
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        Optional<SuspiciousStewEffects> effectFromItemStack =
            AnimalPenItemHelper.getEffectFromItemStack(itemConsumed);

        if (effectFromItemStack.isEmpty())
        {
            return false;
        }

        if (!(mob instanceof MushroomCowAccessor mushroomCow))
        {
            return false;
        }

        if (!componentHolder.has(AnimalPenDataComponentRegistry.MOB_COMPONENT.get()))
        {
            return false;
        }

        SpellParticleOption spellParticleOption = SpellParticleOption.create(ParticleTypes.EFFECT, -1, 1.0F);

        player.level().sendParticles(
            spellParticleOption,
            blockPos.getX() + 0.5f,
            blockPos.getY() + 1.5,
            blockPos.getZ() + 0.5f,
            4,
            0.2, 0.2, 0.2,
            0.05);

        mushroomCow.setStewEffects(effectFromItemStack.get());

        StoredMob storedMob = componentHolder.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
        CompoundTag animalTag = AnimalPenVariantHelper.saveMob(mob);
        componentHolder.set(AnimalPenDataComponentRegistry.MOB_COMPONENT.get(),
            StoredMob.of(storedMob.entityType(), animalTag));

        return true;
    }
}
