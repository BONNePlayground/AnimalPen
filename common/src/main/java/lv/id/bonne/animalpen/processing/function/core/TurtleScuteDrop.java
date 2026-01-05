//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import java.util.ArrayList;
import java.util.List;

import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import lv.id.bonne.animalpen.util.ItemTransferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;


/**
 * This function drops scutes for turtles based on values.
 */
public class TurtleScuteDrop implements EntityFunction.ProcessEntityFunction
{
    @Override
    public boolean processFunction(ServerLevel serverLevel,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        if (!(mob instanceof Turtle))
        {
            return false;
        }

        CompoundTag animalData = mobNBT.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);

        int count;

        if (animalData.contains(AnimalPenCompoundTags.TAG_TURTLE_SCUTE, Tag.TAG_INT))
        {
            // Get and remove value from data.
            count = animalData.getInt(AnimalPenCompoundTags.TAG_TURTLE_SCUTE);
            animalData.remove(AnimalPenCompoundTags.TAG_TURTLE_SCUTE);
        }
        else
        {
            // Exit as drop amount not specified.
            return false;
        }

        List<ItemStack> scuteList = new ArrayList<>();

        do
        {
            ItemStack stack = Items.SCUTE.getDefaultInstance();
            stack.setCount(Math.min(count, stack.getMaxStackSize()));
            scuteList.add(stack);
            count -= stack.getCount();
        }
        while (count > 0);

        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);

        if (blockEntity instanceof AbstractAnimalPenBlockEntity animalPenBlockEntity)
        {
            scuteList.forEach(stack ->
                ItemTransferUtil.insertBellowOrDrop(serverLevel,
                    stack,
                    animalPenBlockEntity.getBlockPos(),
                    animalPenBlockEntity.dropPosition()));
        }
        else
        {
            // This should never happen
            scuteList.forEach(stack ->
                Block.popResource(mob.level(), blockPos.above(), stack));
        }

        return true;
    }
}
