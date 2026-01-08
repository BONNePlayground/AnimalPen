//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * This function spams particles on mooshroom.
 */
public class MooshroomEffectFail implements EntityFunction.PlayerEntityFunction
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
        player.serverLevel().sendParticles(
            ParticleTypes.SMOKE,
            blockPos.getX() + 0.5f,
            blockPos.getY() + 1.5,
            blockPos.getZ() + 0.5f,
            2,
            0.2, 0.2, 0.2,
            0.05);

        return false;
    }
}
