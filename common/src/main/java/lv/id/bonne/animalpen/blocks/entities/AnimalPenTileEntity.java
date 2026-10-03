//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


/**
 * Animal Pen tile entity for land animals
 */
public class AnimalPenTileEntity extends AbstractAnimalPenBlockEntity
{
    public AnimalPenTileEntity(
        BlockPos blockPos,
        BlockState blockState)
    {
        super(AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get(), blockPos, blockState);
    }


    @Override
    public boolean canGrowEntity()
    {
        return AnimalPen.config().isGrowAnimalPenMob();
    }


    @Override
    public boolean validateItemStack(ItemStack itemStack)
    {
        return itemStack.is(AnimalPensItemRegistry.ANIMAL_CAGE.get());
    }


    @Override
    public BlockPos dropPosition()
    {
        return this.getBlockPos().above();
    }


    @Override
    public List<BlockState> getDecorations()
    {
        BlockState defaultBlockState = this.getBlockState().getBlock().defaultBlockState();

        return AnimalPenBlock.DECORATION.getAllValues().
            map(value -> defaultBlockState.setValue(AnimalPenBlock.DECORATION, value.value())).
            collect(Collectors.toList());
    }


    @Override
    protected BlockState getBlockStateWithDecorationIndex(int index)
    {
        AnimalPenBlock.Decoration[] values = AnimalPenBlock.Decoration.values();

        if (index < 0 || index >= values.length)
        {
            return this.getBlockState();
        }

        return this.getBlockState().setValue(AnimalPenBlock.DECORATION, values[index]);
    }
}
