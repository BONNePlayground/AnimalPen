//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.mixin.accessors.MushroomCowAccessor;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
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
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        return this.removeEffect(mob, componentHolder);
    }


    @Override
    public boolean interactDispenser(ServerLevel serverLevel,
        Container dispenserInventory,
        int index,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        return this.removeEffect(mob, componentHolder);
    }


    private boolean removeEffect(Mob mob, ItemStack componentHolder)
    {
        if (mob instanceof MushroomCowAccessor mushroomCow)
        {
            StoredMob storedMob = componentHolder.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());

            if (storedMob == null)
            {
                AnimalPen.LOGGER.error("FAILED to process mooshroom effect remove as data is missing.");
                return false;
            }

            mushroomCow.setStewEffects(null);

            CompoundTag animalTag = new CompoundTag();
            mob.saveWithoutId(animalTag);

            animalTag.remove("UUID");
            animalTag.remove("Pos");

            componentHolder.set(AnimalPenDataComponentRegistry.MOB_COMPONENT.get(),
                StoredMob.of(storedMob.entityType(), animalTag));

            return true;
        }

        return false;
    }
}
