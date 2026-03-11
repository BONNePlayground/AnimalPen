//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.data.provider.neoforge;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.data.provider.ModAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.ResourceLocation;


public class NeoForgeModAdvancementProvider extends AdvancementProvider
{
    public NeoForgeModAdvancementProvider(PackOutput output,
        CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, lookupProvider, List.of(new NeoForgeModAdvancementSubProvider()));
    }


    private static class NeoForgeModAdvancementSubProvider
        implements AdvancementSubProvider, ModAdvancementProvider
    {
        @Override
        public AdvancementHolder generatePlatformAdvancement(Consumer<AdvancementHolder> consumer,
            Advancement.Builder builder,
            ResourceLocation resourceLocation)
        {
            return builder.save(consumer, resourceLocation);
        }


        @Override
        public void generate(HolderLookup.Provider arg,
            Consumer<AdvancementHolder> consumer)
        {
            this.buildModAdvancements(consumer, arg);
        }
    }
}
