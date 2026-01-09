//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lv.id.bonne.animalpen.items.AbstractAnimalStorageItem;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;


@Mixin(ItemStack.class)
public abstract class MigrateItemStack
{
    @Shadow
    public abstract Item getItem();


    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
        at = @At("TAIL"))
    private void migrateOnCreation(ItemLike itemLike,
        int i,
        PatchedDataComponentMap patchedDataComponentMap,
        CallbackInfo ci)
    {
        if (itemLike.asItem() instanceof AbstractAnimalStorageItem)
        {
            AbstractAnimalStorageItem.verifyComponentsAfterLoad((ItemStack) (Object) this);
        }
    }


    @Inject(method = "applyComponentsAndValidate", at = @At("TAIL"))
    private void migrateOnValidate(DataComponentPatch dataComponentPatch, CallbackInfo ci)
    {
        if (this.getItem() instanceof AbstractAnimalStorageItem)
        {
            AbstractAnimalStorageItem.verifyComponentsAfterLoad((ItemStack) (Object) this);
        }
    }


    @Inject(method = "applyComponents(Lnet/minecraft/core/component/DataComponentMap;)V", at = @At("TAIL"))
    private void migrateOnApplyDataComponentMap(DataComponentMap dataComponentMap, CallbackInfo ci)
    {
        if (this.getItem() instanceof AbstractAnimalStorageItem)
        {
            AbstractAnimalStorageItem.verifyComponentsAfterLoad((ItemStack) (Object) this);
        }
    }


    @Inject(method = "applyComponents(Lnet/minecraft/core/component/DataComponentPatch;)V", at = @At("TAIL"))
    private void migrateOnApplyPatchedDataComponentMap(DataComponentPatch dataComponentPatch, CallbackInfo ci)
    {
        if (this.getItem() instanceof AbstractAnimalStorageItem)
        {
            AbstractAnimalStorageItem.verifyComponentsAfterLoad((ItemStack) (Object) this);
        }
    }
}
