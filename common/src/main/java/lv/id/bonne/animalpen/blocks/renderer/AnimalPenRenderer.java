//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import lv.id.bonne.animalpen.mixin.accessors.EntityAccessor;
import lv.id.bonne.animalpen.registries.AnimalPenMobAnimationsRegistry;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
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
        return AnimalPen.config().getAnimalPenMobSize();
    }


    @Override
    protected boolean shouldGrowAnimals()
    {
        return AnimalPen.config().isGrowAnimalPenMob();
    }


    @Override
    protected double getTextStartHeight()
    {
        return 1.5;
    }


    @Override
    public boolean shouldRenderOffScreen()
    {
        return AnimalPen.config().isGrowAnimalPenMob();
    }


    @Override
    public boolean shouldRender(AnimalPenTileEntity blockEntity, Vec3 vec3)
    {
        return AnimalPen.config().isGrowAnimalPenMob() ||
            super.shouldRender(blockEntity, vec3);
    }


    @Override
    protected void configureAnimalPose(Mob animal, AnimalPenTileEntity tileEntity)
    {
        super.configureAnimalPose(animal, tileEntity);

        if (animal.tickCount == tileEntity.getTickCounter())
        {
            return;
        }

        Optional.ofNullable(AnimalPenMobAnimationsRegistry.ANIMAL_PEN_ANIMATIONS.get(animal.getType())).
            ifPresent(animator -> animator.animate(animal));
    }
}