//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider.forge;


import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lv.id.bonne.animalpen.data.provider.loottable.forge.ForgeModBlockLootProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;


public class ForgeModLootTableProvider extends LootTableProvider
{
    public ForgeModLootTableProvider(DataGenerator arg)
    {
        super(arg);
        this.lootTables = List.of(Pair.of(ForgeModBlockLootProvider::new, LootContextParamSets.BLOCK));
    }


    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables()
    {
        return List.of(Pair.of(ForgeModBlockLootProvider::new, LootContextParamSets.BLOCK));
    }


    protected void validate(Map<ResourceLocation, LootTable> tables, ValidationContext ctx)
    {
        tables.forEach((name, table) -> LootTables.validate(ctx, name, table));
    }


    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> lootTables;
}
