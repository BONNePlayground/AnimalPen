//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.behaviour;


import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


/**
 * This class allows to define custom interactions between some tools and Animal Pens/Aquariums
 */
public class UseToolsBehaviour implements DispenseItemBehavior
{
    /**
     * The default constructor
     *
     * @param dispenseItemBehavior Original item behaviour.
     */
    public UseToolsBehaviour(DispenseItemBehavior dispenseItemBehavior)
    {
        this.originalBehaviour = dispenseItemBehavior;
    }


    @Override
    @NotNull
    public ItemStack dispense(BlockSource blockSource, ItemStack itemStack)
    {
        ServerLevel level = blockSource.level();

        if (!level.isClientSide())
        {
            BlockPos blockPos =
                blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
            BlockState blockState = level.getBlockState(blockPos);

            if (!blockState.is(AnimalPenTags.ANIMAL_PEN_BLOCKS) &&
                !blockState.is(AnimalPenBlockRegistry.AQUARIUM.get()) &&
                !blockState.is(AnimalPenBlockRegistry.AVIARY.get()))
            {
                // If not animal pen/aquarium then return to original output
                return this.originalBehaviour.dispense(blockSource, itemStack);
            }

            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            if (blockEntity instanceof AbstractAnimalPenBlockEntity ani && level instanceof ServerLevel serverLevel)
            {
                AbstractAnimalPenBlockEntity.InteractionResult result =
                    ani.interactWithPen(serverLevel, blockSource, itemStack);
                ani.triggerUpdate();

                if (result.success())
                {
                    return result.result();
                }

                return itemStack;
            }
        }

        return itemStack;
    }


    /**
     * The original item behaviour
     */
    private final DispenseItemBehavior originalBehaviour;

    /**
     * The default item behaviour
     */
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
}
