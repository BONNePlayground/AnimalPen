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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeItem;
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
        CompoundTag mobNBT,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        if (mob instanceof Sheep sheep && itemConsumed.getItem() instanceof DyeItem dye)
        {
            sheep.setColor(dye.getDyeColor());

            CompoundTag animalTag = new CompoundTag();
            mob.save(animalTag);
            mobNBT.put(AnimalPenCompoundTags.TAG_ANIMAL, animalTag);

            return true;
        }

        return false;
    }


    @Override
    public boolean interactDispenser(ServerLevel serverLevel,
        Container container,
        int index,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        if (mob instanceof Sheep sheep && itemConsumed.getItem() instanceof DyeItem dye)
        {
            sheep.setColor(dye.getDyeColor());

            CompoundTag animalTag = new CompoundTag();
            mob.save(animalTag);
            mobNBT.put(AnimalPenCompoundTags.TAG_ANIMAL, animalTag);

            return true;
        }

        return false;
    }
}
