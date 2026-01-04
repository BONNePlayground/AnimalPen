//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider.forge;


import java.util.List;
import java.util.Set;

import lv.id.bonne.animalpen.data.provider.loottable.forge.ForgeModBlockLootProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;


public class ForgeModLootTableProvider extends LootTableProvider
{
    public ForgeModLootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(
            new SubProviderEntry(ForgeModBlockLootProvider::new, LootContextParamSets.BLOCK)
        ));
    }
}
