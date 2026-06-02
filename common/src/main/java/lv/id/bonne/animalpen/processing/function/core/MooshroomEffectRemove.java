//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.mixin.accessors.MushroomCowAccessor;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * This function removes effect from mooshroom.
 */
public class MooshroomEffectRemove implements EntityFunction
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
        if (mob instanceof MushroomCowAccessor mushroomCow)
        {
            mushroomCow.setStewEffects(null);

            CompoundTag animalTag = new CompoundTag();
            mob.save(animalTag);
            mobNBT.put(AnimalPenCompoundTags.TAG_ANIMAL, animalTag);

            return true;
        }

        return false;
    }


    @Override
    public boolean interactDispenser(ServerLevel serverLevel,
        Container dispenserInventory,
        int index,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        if (mob instanceof MushroomCowAccessor mushroomCow)
        {
            mushroomCow.setStewEffects(null);

            CompoundTag animalTag = new CompoundTag();
            mob.save(animalTag);
            mobNBT.put(AnimalPenCompoundTags.TAG_ANIMAL, animalTag);

            return true;
        }

        return false;
    }
}
