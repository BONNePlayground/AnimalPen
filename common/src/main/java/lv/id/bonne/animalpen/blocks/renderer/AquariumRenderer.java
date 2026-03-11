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
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;


public class AquariumRenderer extends AbstractAnimalPenRenderer<AquariumTileEntity>
{
    public AquariumRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }


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
        animal.setPose(Pose.SWIMMING);
        animal.setSwimming(true);
        ((EntityAccessor) animal).setWasTouchingWater(true);
        animal.walkAnimation.update(0.2f, 0.4f, 1f);

        if (animal.tickCount == tileEntity.getTickCounter())
        {
            return;
        }

        super.configureAnimalPose(animal, tileEntity);

        // The animation speed is required for some entities to display their swimming animation

        Optional.ofNullable(AnimalPenMobAnimationsRegistry.AQUARIUM_ANIMATIONS.get(animal.getType())).
            ifPresent(animator -> animator.animate(animal));
    }


    @Override
    public boolean shouldRenderOffScreen()
    {
        return true;
    }


    @Override
    public boolean shouldRender(AquariumTileEntity blockEntity, Vec3 vec3)
    {
        return AnimalPen.config().isGrowAquariumMob() ||
            super.shouldRender(blockEntity, vec3);
    }
}