//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import java.util.HashMap;
import java.util.Map;

import lv.id.bonne.animalpen.blocks.renderer.MobDisplayAnimator;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Squid;


/**
 * This class makes new entity rendering registry that allows to define how entity movement
 * should be rendered. This is client-side only thing that is needed to correctly display
 * entity movement, especially in aquarium and aviary.
 */
public class AnimalPenMobAnimationsRegistry
{
    public static void init()
    {
        // Render default renderers

        // Squids have custom animation that depends on body rotation and tentacle angles/movement etc.
        MobDisplayAnimator squidAnimation = mob ->
        {
            if (mob instanceof Squid squid)
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
                squid.tentacleAngle = Mth.abs(Mth.sin(squid.tentacleMovement)) * (float) Math.PI * 0.25F;
                squid.xBodyRot = -45.0F + Mth.sin(squid.tickCount * 0.1F) * 2.0F;
            }
        };

        AQUARIUM_ANIMATIONS.put(EntityType.SQUID, squidAnimation);
        AQUARIUM_ANIMATIONS.put(EntityType.GLOW_SQUID, squidAnimation);

        // Parrots also have some animation requirements
        AVIARY_ANIMATIONS.put(EntityType.PARROT, mob ->
        {
            if (mob instanceof Parrot parrot)
            {
                parrot.oFlap = parrot.flap;
                parrot.oFlapSpeed = parrot.flapSpeed;

                parrot.flapSpeed += (float) 4 * 0.3F;
                parrot.flapSpeed = Mth.clamp(parrot.flapSpeed, 0.0F, 1.0F);

                parrot.flap += 1.8f;
            }
        });
    }


    /**
     * The mob animation for Animal Pens
     */
    public static final Map<EntityType<?>, MobDisplayAnimator> ANIMAL_PEN_ANIMATIONS = new HashMap<>();

    /**
     * The mob animation for Aquariums
     */
    public static final Map<EntityType<?>, MobDisplayAnimator> AQUARIUM_ANIMATIONS = new HashMap<>();

    /**
     * The mob animations for Aviary
     */
    public static final Map<EntityType<?>, MobDisplayAnimator> AVIARY_ANIMATIONS = new HashMap<>();
}
