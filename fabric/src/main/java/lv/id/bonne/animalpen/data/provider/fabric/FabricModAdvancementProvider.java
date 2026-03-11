//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.data.provider.fabric;


import java.util.function.Consumer;

import lv.id.bonne.animalpen.data.provider.ModAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;


public class FabricModAdvancementProvider extends FabricAdvancementProvider implements ModAdvancementProvider
{
    protected FabricModAdvancementProvider(FabricDataOutput dataGenerator)
    {
        super(dataGenerator);
    }


    @Override
    public void generateAdvancement(Consumer<AdvancementHolder> consumer)
    {
        this.buildModAdvancements(consumer);
    }


    public AdvancementHolder generatePlatformAdvancement(Consumer<AdvancementHolder> consumer,
        Advancement.Builder builder,
        ResourceLocation resourceLocation)
    {
        return builder.save(consumer, resourceLocation.toString());
    }
}
