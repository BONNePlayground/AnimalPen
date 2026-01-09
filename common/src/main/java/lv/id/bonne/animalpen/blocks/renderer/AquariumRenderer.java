//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.mixin.accessors.EntityAccessor;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.frog.Frog;
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

        if (animal.tickCount == tileEntity.getTickCounter())
        {
            return;
        }

        super.configureAnimalPose(animal, tileEntity);

        // The animation speed is required for some entities to display their swimming animation

        // Squids have custom animation that depends on body rotation and tentacle angles/movement etc.
        if (animal instanceof Squid squid)
        {
            squid.xBodyRotO = squid.xBodyRot;

            if (squid.oldTentacleMovement == 0)
            {
                // squid tentacle speed is private. Abuse oldTentacleMovement for replacing it.
                squid.oldTentacleMovement = 1.0F / (squid.getRandom().nextFloat() + 1.0F) * 0.2F;
            }

            squid.tentacleMovement += squid.oldTentacleMovement;

            if (squid.tentacleMovement > (Math.PI * 2D))
            {
                squid.tentacleMovement -= ((float) Math.PI) * 2F;

                if (squid.getRandom().nextInt(10) == 0)
                {
                    squid.oldTentacleMovement = 1.0F / (squid.getRandom().nextFloat() + 1.0F) * 0.2F;
                }
            }

            squid.oldTentacleAngle = squid.tentacleAngle;
            squid.tentacleAngle = Mth.abs(Mth.sin(squid.tentacleMovement)) * (float)Math.PI * 0.25F;
            squid.xBodyRot = -45.0F + Mth.sin(squid.tickCount * 0.1F) * 2.0F;
        }

        // Frog animations are triggered by animation state.
        if (animal instanceof Frog frog)
        {
            frog.swimIdleAnimationState.startIfStopped(frog.tickCount);
        }
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