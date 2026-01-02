//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.mixin.accessors.EntityAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;


public class AquariumRenderer extends AbstractAnimalPenRenderer<AquariumTileEntity>
{
    @Override
    protected float getAnimalVerticalOffset()
    {
        return 12 / 16f;
    }


    @Override
    protected float getAnimalSize()
    {
        return AnimalPen.config().getWaterAnimalSize();
    }


    @Override
    protected boolean shouldGrowAnimals()
    {
        return AnimalPen.config().isGrowWaterAnimals();
    }


    @Override
    protected double getTextStartHeight()
    {
        return 1.75;
    }


    @Override
    protected void configureAnimalPose(Mob animal, AquariumTileEntity tileEntity)
    {
        super.configureAnimalPose(animal, tileEntity);

        animal.setPose(Pose.SWIMMING);
        animal.setSwimming(true);
        ((EntityAccessor) animal).setWasTouchingWater(true);
    }


    @Override
    public boolean shouldRenderOffScreen(AquariumTileEntity blockEntity)
    {
        return !blockEntity.getInventory().isEmpty();
    }


    @Override
    public boolean shouldRender(AquariumTileEntity blockEntity, Vec3 vec3)
    {
        return AnimalPen.config().isGrowWaterAnimals() ||
            super.shouldRender(blockEntity, vec3);
    }
}