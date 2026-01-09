//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AquariumBlock;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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
}