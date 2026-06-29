//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import java.util.Map;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.items.component.StoredMobData;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * This function increases int data from animal_data object with key from input.
 */
public class IncrementIntegerDataKey implements EntityFunction.ProcessEntityFunction
{
    @Override
    public boolean processFunction(ServerLevel serverLevel,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        var storedMobData = componentHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());

        if (storedMobData == null)
        {
            AnimalPen.LOGGER.error("FAILED to increment data key as data is missing.");
            return false;
        }

        if (dataKey == null)
        {
            return false;
        }

        int value = dataValue == null ? 1 : dataValue.getAsInt();

        Map<String, Integer> properties = storedMobData.properties();
        properties.put(dataKey, properties.getOrDefault(dataKey, 0) + value);

        componentHolder.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
            StoredMobData.of(storedMobData.animalCount(), properties, storedMobData.cooldowns()));

        return true;
    }
}
