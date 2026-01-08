//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.items.component.StoredMobData;
import lv.id.bonne.animalpen.items.component.StoredMobVariants;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
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
     * Stores captured entity type and NBT data
     */
    public static final RegistrySupplier<DataComponentType<StoredMob>> MOB_COMPONENT =
        REGISTRY.register(AnimalPen.resourceOf("mob"),
            () -> DataComponentType.<StoredMob>builder().persistent(StoredMob.CODEC).
                networkSynchronized(StoredMob.STREAM_CODEC).build());

    /**
     * Stores captured entity amount, cooldowns and other properties
     */
    public static final RegistrySupplier<DataComponentType<StoredMobData>> MOB_DATA_COMPONENT =
        REGISTRY.register(AnimalPen.resourceOf("mob_data"),
            () -> DataComponentType.<StoredMobData>builder().persistent(StoredMobData.CODEC).
                networkSynchronized(StoredMobData.STREAM_CODEC).build());

    /**
     * Stores captured entity variants
     */
    public static final RegistrySupplier<DataComponentType<StoredMobVariants>> MOB_VARIANT_COMPONENT =
        REGISTRY.register(AnimalPen.resourceOf("mob_variants"),
            () -> DataComponentType.<StoredMobVariants>builder().persistent(StoredMobVariants.CODEC).
                networkSynchronized(StoredMobVariants.STREAM_CODEC).build());


    /**
     * @deprecated used in 1.6 and bellow.
     */
    @Deprecated
    public static final RegistrySupplier<DataComponentType<CustomData>> ENTITY_VARIANTS =
        REGISTRY.register(AnimalPen.resourceOf("variants"),
            () -> DataComponentType.<CustomData>builder().persistent(CustomData.CODEC).
                networkSynchronized(CustomData.STREAM_CODEC).build());
}
