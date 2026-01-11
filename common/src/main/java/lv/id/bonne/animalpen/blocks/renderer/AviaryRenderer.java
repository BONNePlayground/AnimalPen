//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AviaryTileEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.phys.Vec3;


public class AviaryRenderer extends AbstractAnimalPenRenderer<AviaryTileEntity>
{
    public AviaryRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }


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
        animal.setOnGround(false);

        if (animal.tickCount == tileEntity.getTickCounter())
        {
            return;
        }

        super.configureAnimalPose(animal, tileEntity);

        // The animation speed is required for some entities to display their swimming animation

        if (animal instanceof Parrot parrot)
        {
            parrot.oFlap = parrot.flap;
            parrot.oFlapSpeed = parrot.flapSpeed;

            parrot.flapSpeed += (float) 4 * 0.3F;
            parrot.flapSpeed = Mth.clamp(parrot.flapSpeed, 0.0F, 1.0F);

            parrot.flap += 1.8f;
        }
        else if (animal instanceof Bat bat)
        {
            bat.flyAnimationState.startIfStopped(bat.tickCount);
        }
    }
}
