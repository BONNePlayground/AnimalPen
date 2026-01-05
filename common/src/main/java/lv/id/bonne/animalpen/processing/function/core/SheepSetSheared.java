//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;


/**
 * This function sets sheep sheared status based on input data value.
 */
public class SheepSetSheared implements EntityFunction.ProcessEntityFunction
{
    @Override
    public boolean processFunction(ServerLevel serverLevel,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        if (mob instanceof Sheep sheep && dataValue != null)
        {
            sheep.setSheared(dataValue.getAsBoolean());

            CompoundTag animalTag = new CompoundTag();
            mob.save(animalTag);
            mobNBT.put(AnimalPenCompoundTags.TAG_ANIMAL, animalTag);

            return true;
        }

        return false;
    }
}
