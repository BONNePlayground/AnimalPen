//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.processing.function.api.ShearStateAccessor;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * This function sets sheep sheared status based on input data value.
 */
public class MobSetSheared implements EntityFunction.ProcessEntityFunction
{
    @Override
    public boolean processFunction(ServerLevel serverLevel,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        if (dataValue != null)
        {
            if (mob instanceof ShearStateAccessor shearAccessor)
            {
                shearAccessor.setShearedState(dataValue.getAsBoolean());
                CompoundTag animalTag = new CompoundTag();
                mob.saveWithoutId(animalTag);

                animalTag.remove("UUID");
                animalTag.remove("Pos");

                componentHolder.set(AnimalPenDataComponentRegistry.MOB_COMPONENT.get(),
                    StoredMob.of(mob.getType(), animalTag));

                return true;
            }
            else
            {
                // ignored.
                return false;
            }
        }

        return false;
    }
}
