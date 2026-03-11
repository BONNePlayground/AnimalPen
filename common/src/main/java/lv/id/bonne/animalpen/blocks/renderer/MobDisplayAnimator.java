//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.blocks.renderer;


import net.minecraft.world.entity.Mob;


/**
 * Simple interface that allows to change given mob animation properties
 */
public interface MobDisplayAnimator
{
    /**
     * This method is triggered when entity is animated in tile entity renderer on client side.
     * @param mob The mob that is rendered.
     */
    public void animate(Mob mob);
}
