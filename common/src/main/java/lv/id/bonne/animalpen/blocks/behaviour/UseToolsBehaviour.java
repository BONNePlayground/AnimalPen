//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.behaviour;


import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenBlockInterface;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
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
            BlockPos blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING));
            BlockState blockState = level.getBlockState(blockPos);

            if (!blockState.is(AnimalPenBlock.ANIMAL_PENS) && !blockState.is(AnimalPenBlockRegistry.AQUARIUM.get()))
            {
                // If not animal pen/aquarium then return to original output
                return this.originalBehaviour.dispense(blockSource, itemStack);
            }

            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            if (blockEntity instanceof AnimalPenBlockInterface<?> ani)
            {
                ItemStack output =
                    ((AnimalPenInterface) ani.getStoredAnimal()).animalPenInteract(level, itemStack, blockPos);
                ani.triggerUpdate();

                if (output.isEmpty())
                {
                    // if output is empty, do nothing
                    return itemStack;
                }

                if (!itemStack.isEmpty() && !blockSource.blockEntity().insertItem(output.copy()).isEmpty())
                {
                    // If it failed to insert into dispenser, use default dispense behaviour
                    this.defaultDispenseItemBehavior.dispense(blockSource, output.copy());
                }

                return itemStack.isEmpty() ? output : itemStack;
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
