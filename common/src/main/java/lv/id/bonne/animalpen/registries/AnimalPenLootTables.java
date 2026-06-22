//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.registries;


import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;


public class AnimalPenLootTables
{
    public static final ResourceKey<LootTable> MUSHROOM_STEW =
        ResourceKey.create(Registries.LOOT_TABLE, AnimalPen.resourceOf("animal_interactions/bowl/mushroom_stew"));

    public static final ResourceKey<LootTable> SUSPICIOUS_STEW =
        ResourceKey.create(Registries.LOOT_TABLE, AnimalPen.resourceOf("animal_interactions/bowl/suspicious_stew"));

    public static final ResourceKey<LootTable> ARMADILLO_SCUTE =
        ResourceKey.create(Registries.LOOT_TABLE, AnimalPen.resourceOf("animal_interactions/brush/armadillo_scute"));

    public static final ResourceKey<LootTable> MILK_BUCKET =
        ResourceKey.create(Registries.LOOT_TABLE, AnimalPen.resourceOf("animal_interactions/bucket/milk_bucket"));

    public static final ResourceKey<LootTable> SNIFFER_EGG =
        ResourceKey.create(Registries.LOOT_TABLE, AnimalPen.resourceOf("animal_interactions/bucket/sniffer_egg"));

    public static final ResourceKey<LootTable> TURTLE_EGG =
        ResourceKey.create(Registries.LOOT_TABLE, AnimalPen.resourceOf("animal_interactions/bucket/turtle_egg"));

    public static final ResourceKey<LootTable> HONEY_BOTTLE =
        ResourceKey.create(Registries.LOOT_TABLE, AnimalPen.resourceOf("animal_interactions/glass_bottle/honey_bottle"));

    public static final ResourceKey<LootTable> FROGLIGHT =
        ResourceKey.create(Registries.LOOT_TABLE, AnimalPen.resourceOf("animal_interactions/magma_cube/froglight"));

    public static final ResourceKey<LootTable> HONEYCOMB =
        ResourceKey.create(Registries.LOOT_TABLE, AnimalPen.resourceOf("animal_interactions/shear/honeycomb"));

    public static final ResourceKey<LootTable> WOOL =
        ResourceKey.create(Registries.LOOT_TABLE, AnimalPen.resourceOf("animal_interactions/shear/wool"));
}
