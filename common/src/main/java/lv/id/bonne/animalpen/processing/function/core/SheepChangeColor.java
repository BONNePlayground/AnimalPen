//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import lv.id.bonne.animalpen.util.AnimalPenVariantHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;


/**
 * This method allows to change color of sheep based on item consumed.
 */
public class SheepChangeColor implements EntityFunction
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
        return this.changeSheepColor(itemConsumed, mob, componentHolder);
    }


    @Override
    public boolean interactDispenser(ServerLevel serverLevel,
        Container container,
        int index,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        return this.changeSheepColor(itemConsumed, mob, componentHolder);
    }


    private boolean changeSheepColor(ItemStack itemConsumed, Mob mob, ItemStack componentHolder)
    {
        if (mob instanceof Sheep sheep && itemConsumed.is(ItemTags.DYES) && itemConsumed.has(DataComponents.DYE))
        {
            sheep.setColor(itemConsumed.get(DataComponents.DYE));

            StoredMob storedMob = componentHolder.get(AnimalPenDataComponentRegistry.MOB_COMPONENT.get());
            CompoundTag animalTag = AnimalPenVariantHelper.saveMob(mob);
            componentHolder.set(AnimalPenDataComponentRegistry.MOB_COMPONENT.get(),
                StoredMob.of(storedMob.entityType(), animalTag));

            return true;
        }

        return false;
    }
}
