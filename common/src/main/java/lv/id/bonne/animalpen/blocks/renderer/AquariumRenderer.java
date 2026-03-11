//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.mixin.accessors.EntityAccessor;
import lv.id.bonne.animalpen.registries.AnimalPenMobAnimationsRegistry;
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
        return AnimalPen.config().getAquariumMobSize();
    }


    @Override
    protected boolean shouldGrowAnimals()
    {
        return AnimalPen.config().isGrowAquariumMob();
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

        if (animal.tickCount == tileEntity.getTickCounter())
        {
            return;
        }

        // The animation speed is required for some entities to display their swimming animation

        Optional.ofNullable(AnimalPenMobAnimationsRegistry.AQUARIUM_ANIMATIONS.get(animal.getType())).
            ifPresent(animator -> animator.animate(animal));
    }


    @Override
    public boolean shouldRenderOffScreen(AquariumTileEntity blockEntity)
    {
        return !blockEntity.getInventory().isEmpty();
    }


    @Override
    public boolean shouldRender(AquariumTileEntity blockEntity, Vec3 vec3)
    {
        return AnimalPen.config().isGrowAquariumMob() ||
            super.shouldRender(blockEntity, vec3);
    }
}