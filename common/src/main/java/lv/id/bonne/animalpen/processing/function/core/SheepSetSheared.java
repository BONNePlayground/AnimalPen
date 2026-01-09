//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import lv.id.bonne.animalpen.util.AnimalPenVariantHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;


/**
 * This function sets sheep sheared status based on input data value.
 */
public class SheepSetSheared implements EntityFunction.ProcessEntityFunction
{
    @Override
    public boolean processFunction(ServerLevel serverLevel,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        if (mob instanceof Sheep sheep && dataValue != null)
        {
            sheep.setSheared(dataValue.getAsBoolean());

            StoredMob storedMob = componentHolder.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
            CompoundTag animalTag = AnimalPenVariantHelper.saveMob(mob);
            componentHolder.set(AnimalPenDataComponentRegistry.MOB_COMPONENT.get(),
                StoredMob.of(storedMob.entityType(), animalTag));

            return true;
        }

        return false;
    }
}
