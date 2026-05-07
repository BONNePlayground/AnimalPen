//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.registries;


import java.util.function.Supplier;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.items.component.StoredMob;
import lv.id.bonne.animalpen.items.component.StoredMobData;
import lv.id.bonne.animalpen.items.component.StoredMobVariants;
import lv.id.bonne.animalpen.platform.Services;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.CustomData;


public class AnimalPenDataComponentRegistry
{
    public static void register()
    {
    }


    /**
     * Stores captured entity type and NBT data
     */
    public static final Supplier<DataComponentType<StoredMob>> MOB_COMPONENT =
        Services.REGISTRY.registerDataComponent(AnimalPen.resourceOf("mob"),
            () -> DataComponentType.<StoredMob>builder().persistent(StoredMob.CODEC).
                networkSynchronized(StoredMob.STREAM_CODEC).build());

    /**
     * Stores captured entity amount, cooldowns and other properties
     */
    public static final Supplier<DataComponentType<StoredMobData>> MOB_DATA_COMPONENT =
        Services.REGISTRY.registerDataComponent(AnimalPen.resourceOf("mob_data"),
            () -> DataComponentType.<StoredMobData>builder().persistent(StoredMobData.CODEC).
                networkSynchronized(StoredMobData.STREAM_CODEC).build());

    /**
     * Stores captured entity variants
     */
    public static final Supplier<DataComponentType<StoredMobVariants>> MOB_VARIANT_COMPONENT =
        Services.REGISTRY.registerDataComponent(AnimalPen.resourceOf("mob_variants"),
            () -> DataComponentType.<StoredMobVariants>builder().persistent(StoredMobVariants.CODEC).
                networkSynchronized(StoredMobVariants.STREAM_CODEC).build());


    /**
     * @deprecated used in 1.6 and bellow.
     */
    @Deprecated
    public static final Supplier<DataComponentType<CustomData>> ENTITY_VARIANTS =
        Services.REGISTRY.registerDataComponent(AnimalPen.resourceOf("variants"),
            () -> DataComponentType.<CustomData>builder().persistent(CustomData.CODEC).
                networkSynchronized(CustomData.STREAM_CODEC).build());
}
