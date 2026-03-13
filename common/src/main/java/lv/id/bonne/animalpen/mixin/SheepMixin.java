package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import lv.id.bonne.animalpen.processing.function.api.ShearStateAccessor;
import net.minecraft.world.entity.animal.sheep.Sheep;


@Mixin(Sheep.class)
public abstract class SheepMixin implements ShearStateAccessor
{
    @Shadow
    public abstract void setSheared(boolean bl);


    @Override
    public void setShearedState(boolean sheared)
    {
        this.setSheared(sheared);
    }
}