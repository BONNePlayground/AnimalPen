//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;


public class AnimalPenDataComponentRegistry
{
    public static void register()
    {
        REGISTRY.register();
    }

    public static final DeferredRegister<DataComponentType<?>> REGISTRY =
        DeferredRegister.create(AnimalPen.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    /**
     * The custom component that stores entity variants.
     */
    public static final RegistrySupplier<DataComponentType<CustomData>> ENTITY_VARIANTS =
        REGISTRY.register(new ResourceLocation(AnimalPen.MOD_ID, "variants"),
            () -> DataComponentType.<CustomData>builder().persistent(CustomData.CODEC).
                networkSynchronized(CustomData.STREAM_CODEC).build());
}
