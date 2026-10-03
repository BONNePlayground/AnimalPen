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
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


/**
 * Aquarium tile entity for water animals
 */
public class AquariumTileEntity extends AbstractAnimalPenBlockEntity
{
    public AquariumTileEntity(
        BlockPos blockPos,
        BlockState blockState)
    {
        super(AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get(), blockPos, blockState);
    }


    @Override
    public boolean canGrowEntity()
    {
        return AnimalPen.config().isGrowAquariumMob();
    }


    @Override
    public boolean validateItemStack(ItemStack itemStack)
    {
        return itemStack.is(AnimalPensItemRegistry.ANIMAL_CONTAINER.get());
    }


    @Override
    public void triggerUpdate()
    {
        super.triggerUpdate();

        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        BlockState oldState = this.getBlockState();
        BlockState newState = this.getBlockState();

        if (oldState.getValue(AquariumBlock.FILLED) == this.getInventory().isEmpty())
        {
            newState = oldState.setValue(AquariumBlock.FILLED, !this.getInventory().isEmpty());
            this.level.setBlock(this.getBlockPos(), newState, Block.UPDATE_CLIENTS);
            this.setChanged();
        }

        this.level.sendBlockUpdated(this.getBlockPos(),
            oldState,
            newState,
            Block.UPDATE_CLIENTS);
    }


    @Override
    public BlockPos dropPosition()
    {
        return this.getBlockPos().above(2);
    }


    @Override
    public List<BlockState> getDecorations()
    {
        BlockState defaultBlockState = this.getBlockState().getBlock().defaultBlockState();

        return AquariumBlock.DECORATION.getAllValues().
            map(value -> defaultBlockState.setValue(AquariumBlock.DECORATION, value.value())).
            collect(Collectors.toList());
    }


    @Override
    protected BlockState getBlockStateWithDecorationIndex(int index)
    {
        AquariumBlock.Decoration[] values = AquariumBlock.Decoration.values();

        if (index < 0 || index >= values.length)
        {
            return this.getBlockState();
        }

        return this.getBlockState().setValue(AquariumBlock.DECORATION, values[index]);
    }


    @Override
    public BlockState getDecorationBlockState()
    {
        return super.getDecorationBlockState().setValue(AquariumBlock.FILLED, Boolean.FALSE);
    }
}