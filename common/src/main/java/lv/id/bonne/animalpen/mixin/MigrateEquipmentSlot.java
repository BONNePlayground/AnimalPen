//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;

import lv.id.bonne.animalpen.items.AbstractAnimalStorageItem;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;


@Mixin(EntityEquipment.class)
public class MigrateEquipmentSlot
{
    @Inject(method = "<init>(Ljava/util/EnumMap;)V", at = @At("HEAD"))
    private static void migrateOnCreating(EnumMap enumMap, CallbackInfo ci)
    {
        if (enumMap == null || enumMap.isEmpty())
        {
            return;
        }

        enumMap.values().forEach(val -> {
            if (val instanceof ItemStack stack && stack.getItem() instanceof AbstractAnimalStorageItem)
            {
                AbstractAnimalStorageItem.verifyComponentsAfterLoad(stack);
            }
        });
    }

    @Inject(method = "set", at = @At("HEAD"))
    private void migrateOnEquippingItemStack(EquipmentSlot equipmentSlot,
        ItemStack itemStack,
        CallbackInfoReturnable<ItemStack> cir)
    {
        if (itemStack != null && itemStack.getItem() instanceof AbstractAnimalStorageItem)
        {
            AbstractAnimalStorageItem.verifyComponentsAfterLoad(itemStack);
        }
    }
}
