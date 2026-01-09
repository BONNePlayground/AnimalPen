//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider.neoforge;


import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.provider.loottable.neoforge.NeoForgeModBlockLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;


public class NeoForgeModLootTableProvider extends LootTableProvider
{
    public NeoForgeModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Set.of(), List.of(
            new SubProviderEntry(NeoForgeModBlockLootProvider::new, LootContextParamSets.BLOCK)
        ), lookupProvider);
    }
}
