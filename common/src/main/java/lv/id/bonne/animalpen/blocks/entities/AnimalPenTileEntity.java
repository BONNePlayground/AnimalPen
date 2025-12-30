//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.entities;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
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
        return AnimalPen.config().isGrowAnimals();
    }


    @Override
    public boolean validateItemStack(ItemStack itemStack)
    {
        return itemStack.is(AnimalPensItemRegistry.ANIMAL_CAGE.get());
    }
}
