//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;


/**
 * This function sets sheep sheared status based on input data value.
 */
public class MobSetSheared implements EntityFunction.ProcessEntityFunction
{
    @Override
    public boolean processFunction(ServerLevel serverLevel,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        if (dataValue != null)
        {
            try
            {
                MethodHandle setSheared = MethodHandles.publicLookup().findVirtual(
                    mob.getClass(),
                    "setSheared",
                    MethodType.methodType(void.class, boolean.class));

                setSheared.invoke(mob, dataValue.getAsBoolean());

                CompoundTag animalTag = new CompoundTag();
                mob.save(animalTag);
                mobNBT.put(AnimalPenCompoundTags.TAG_ANIMAL, animalTag);

                return true;
            }
            catch (Throwable e)
            {
                // ignored.
                return false;
            }
        }

        return false;
    }
}
