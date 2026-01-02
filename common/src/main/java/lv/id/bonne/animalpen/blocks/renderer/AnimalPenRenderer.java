//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import net.minecraft.world.phys.Vec3;


public class AnimalPenRenderer extends AbstractAnimalPenRenderer<AnimalPenTileEntity>
{
    @Override
    protected float getAnimalVerticalOffset()
    {
        return 4f / 16f;
    }


    @Override
    protected float getAnimalSize()
    {
        return AnimalPen.config().getAnimalSize();
    }


    @Override
    protected boolean shouldGrowAnimals()
    {
        return AnimalPen.config().isGrowAnimals();
    }


    @Override
    protected double getTextStartHeight()
    {
        return 1.5;
    }


    @Override
    public boolean shouldRenderOffScreen(AnimalPenTileEntity blockEntity)
    {
        return !blockEntity.getInventory().isEmpty() &&
            AnimalPen.config().isGrowAnimals();
    }


    @Override
    public boolean shouldRender(AnimalPenTileEntity blockEntity, Vec3 vec3)
    {
        return AnimalPen.config().isGrowAnimals() ||
            super.shouldRender(blockEntity, vec3);
    }
}