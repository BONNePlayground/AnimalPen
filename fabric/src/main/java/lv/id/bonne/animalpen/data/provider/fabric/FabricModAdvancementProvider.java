//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.data.provider.fabric;


import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.data.provider.ModAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;


public class FabricModAdvancementProvider extends FabricAdvancementProvider implements ModAdvancementProvider
{
    protected FabricModAdvancementProvider(FabricDataOutput dataGenerator, CompletableFuture<HolderLookup.Provider> registryLookup)
    {
        super(dataGenerator, registryLookup);
    }


    @Override
    public void generateAdvancement(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer)
    {
        this.buildModAdvancements(consumer, provider);
    }


    public AdvancementHolder generatePlatformAdvancement(Consumer<AdvancementHolder> consumer,
        Advancement.Builder builder,
        ResourceLocation resourceLocation)
    {
        return builder.save(consumer, resourceLocation.toString());
    }
}
