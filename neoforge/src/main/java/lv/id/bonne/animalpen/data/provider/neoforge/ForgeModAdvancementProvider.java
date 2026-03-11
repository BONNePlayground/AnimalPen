//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.data.provider.forge;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.data.provider.ModAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;


public class ForgeModAdvancementProvider extends ForgeAdvancementProvider
{
    public ForgeModAdvancementProvider(PackOutput output,
        CompletableFuture<HolderLookup.Provider> lookupProvider,
        ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, existingFileHelper, List.of(new ForgeModAdvancementSubProvider()));
    }


    private static class ForgeModAdvancementSubProvider implements ForgeAdvancementProvider.AdvancementGenerator, ModAdvancementProvider
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
            Consumer<AdvancementHolder> consumer,
            ExistingFileHelper existingFileHelper)
        {
            this.buildModAdvancements(consumer);
        }
    }
}
