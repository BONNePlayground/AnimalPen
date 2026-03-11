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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;


public class NeoForgeModAdvancementProvider extends AdvancementProvider
{
    public NeoForgeModAdvancementProvider(PackOutput output,
        CompletableFuture<HolderLookup.Provider> lookupProvider,
        ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, existingFileHelper, List.of(new NeoForgeModAdvancementSubProvider(existingFileHelper)));
    }


    private static class NeoForgeModAdvancementSubProvider
        implements AdvancementProvider.AdvancementGenerator, ModAdvancementProvider
    {
        public NeoForgeModAdvancementSubProvider(ExistingFileHelper existingFileHelper)
        {
            this.existingFileHelper = existingFileHelper;
        }


        @Override
        public AdvancementHolder generatePlatformAdvancement(Consumer<AdvancementHolder> consumer,
            Advancement.Builder builder,
            ResourceLocation resourceLocation)
        {
            return builder.save(consumer, resourceLocation, this.existingFileHelper);
        }


        @Override
        public void generate(HolderLookup.Provider arg,
            Consumer<AdvancementHolder> consumer,
            ExistingFileHelper existingFileHelper)
        {
            this.buildModAdvancements(consumer);
        }


        private final ExistingFileHelper existingFileHelper;
    }
}
