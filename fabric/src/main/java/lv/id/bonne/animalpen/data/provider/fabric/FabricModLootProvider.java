//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.data.provider.fabric;


import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import lv.id.bonne.animalpen.data.provider.AnimalPenLootProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;


public final class FabricModLootProvider extends SimpleFabricLootTableSubProvider
{

    public FabricModLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup)
    {
        super(output, registryLookup, LootContextParamSets.GIFT);
        this.registryLookup = registryLookup;
    }


    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output)
    {
        AnimalPenLootProvider.generate(
            output,
            registryLookup.join()
        );
    }


    @Override
    public void run()
    {

    }


    private final CompletableFuture<HolderLookup.Provider> registryLookup;
}