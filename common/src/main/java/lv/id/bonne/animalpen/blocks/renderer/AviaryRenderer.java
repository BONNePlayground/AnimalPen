//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AviaryTileEntity;
import lv.id.bonne.animalpen.registries.AnimalPenMobAnimationsRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.phys.Vec3;


public class AviaryRenderer extends AbstractAnimalPenRenderer<AviaryTileEntity>
{
    @Override
    protected float getAnimalVerticalOffset()
    {
        return 8f / 16f;
    }


    @Override
    protected float getAnimalSize()
    {
        return AnimalPen.config().getAviaryMobSize();
    }


    @Override
    protected boolean shouldGrowAnimals()
    {
        return AnimalPen.config().isGrowAviaryMob();
    }


    @Override
    protected double getTextStartHeight()
    {
        return 1.5;
    }


    @Override
    public boolean shouldRenderOffScreen()
    {
        return AnimalPen.config().isGrowAviaryMob();
    }


    @Override
    public boolean shouldRender(AviaryTileEntity blockEntity, Vec3 vec3)
    {
        return AnimalPen.config().isGrowAviaryMob() ||
            super.shouldRender(blockEntity, vec3);
    }


    @Override
    protected void configureAnimalPose(Mob animal, AviaryTileEntity tileEntity)
    {
        super.configureAnimalPose(animal, tileEntity);

        animal.setOnGround(false);

        if (animal.tickCount == tileEntity.getTickCounter())
        {
            return;
        }

        // The animation speed is required for some entities to display their swimming animation

        Optional.ofNullable(AnimalPenMobAnimationsRegistry.AVIARY_ANIMATIONS.get(animal.getType())).
            ifPresent(animator -> animator.animate(animal));
    }
}
