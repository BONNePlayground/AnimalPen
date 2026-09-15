//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import java.util.List;
import java.util.stream.Collectors;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.AquariumBlock;
import lv.id.bonne.animalpen.blocks.AviaryBlock;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


/**
 * Aquarium tile entity for flying animals
 */
public class AviaryTileEntity extends AbstractAnimalPenBlockEntity
{
    public AviaryTileEntity(
        BlockPos blockPos,
        BlockState blockState)
    {
        super(AnimalPenTileEntityRegistry.AVIARY_TILE_ENTITY.get(), blockPos, blockState);
    }


    @Override
    public boolean canGrowEntity()
    {
        return AnimalPen.config().isGrowAviaryMob();
    }


    @Override
    public boolean validateItemStack(ItemStack itemStack)
    {
        return itemStack.is(AnimalPensItemRegistry.BIRD_CATCHER.get());
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

        return AviaryBlock.DECORATION.getAllValues().
            map(value -> defaultBlockState.setValue(AviaryBlock.DECORATION, value.value())).
            collect(Collectors.toList());
    }


    @Override
    protected BlockState getBlockStateWithDecorationIndex(int index)
    {
        AviaryBlock.Decoration[] values = AviaryBlock.Decoration.values();

        if (index < 0 || index >= values.length)
        {
            return this.getBlockState();
        }

        return this.getBlockState().setValue(AviaryBlock.DECORATION, values[index]);
    }
}